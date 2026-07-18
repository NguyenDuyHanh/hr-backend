package com.tlu.hrm.service.impl;

import com.tlu.hrm.dto.request.NotificationDto;
import com.tlu.hrm.dto.search.SearchDto;
import com.tlu.hrm.event.NotificationCreatedEvent;
import com.tlu.hrm.model.Notification;
import com.tlu.hrm.model.NotificationRecipient;
import com.tlu.hrm.model.User;
import com.tlu.hrm.repository.NotificationRecipientRepository;
import com.tlu.hrm.repository.NotificationRepository;
import com.tlu.hrm.repository.UserRepository;
import com.tlu.hrm.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private NotificationRecipientRepository recipientRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationDto> pagingReadableNotifications(String username, SearchDto searchDto) {
        int pageIndex = searchDto.getPageIndex() >= 1 ? searchDto.getPageIndex() - 1 : 0;
        int pageSize = searchDto.getPageSize() > 0 ? searchDto.getPageSize() : 10;
        Pageable pageable = PageRequest.of(pageIndex, pageSize);

        Page<Notification> page = notificationRepository.findReadableNotifications(username, searchDto.getNotificationType(), pageable);

        if (page.isEmpty()) {
            return Page.empty(pageable);
        }

        // Tối ưu hóa: lấy tất cả các bản ghi recipient của trang hiện tại để tránh N+1 query
        List<UUID> notiIds = page.getContent().stream().map(Notification::getId).collect(Collectors.toList());
        List<NotificationRecipient> recipients = recipientRepository.findByUserUsernameAndIsDeletedFalse(username);
        
        Map<UUID, NotificationRecipient> recipientMap = new HashMap<>();
        for (NotificationRecipient r : recipients) {
            if (r.getNotification() != null) {
                recipientMap.put(r.getNotification().getId(), r);
            }
        }

        List<NotificationDto> dtoList = page.getContent().stream().map(noti -> {
            NotificationDto dto = new NotificationDto(noti);
            NotificationRecipient r = recipientMap.get(noti.getId());
            if (noti.getIsGlobal()) {
                // Đối với thông báo global, nếu có bản ghi và đã có ngày đọc -> đã đọc
                dto.setIsRead(r != null && r.getReadAt() != null);
            } else {
                // Đối với thông báo cá nhân, dựa vào trường readAt của recipient
                dto.setIsRead(r != null && r.getReadAt() != null);
            }
            return dto;
        }).collect(Collectors.toList());

        return new PageImpl<>(dtoList, pageable, page.getTotalElements());
    }

    @Override
    public NotificationDto saveOrUpdate(NotificationDto notiDto) {
        if (notiDto == null || notiDto.getTitle() == null || notiDto.getContent() == null) {
            return null;
        }
        Notification entity;
        if (notiDto.getId() != null) {
            entity = notificationRepository.findById(notiDto.getId()).orElse(new Notification());
        } else {
            entity = new Notification();
        }

        entity.setTitle(notiDto.getTitle());
        entity.setContent(notiDto.getContent());
        entity.setNotificationType(notiDto.getNotificationType());
        entity.setTargetObjectId(notiDto.getTargetObjectId());
        entity.setLinkUrl(notiDto.getLinkUrl());
        entity.setIsGlobal(notiDto.getIsGlobal() != null && notiDto.getIsGlobal());

        Notification saved = notificationRepository.save(entity);
        NotificationDto result = new NotificationDto(saved);

        // Nếu là thông báo global (toàn hệ thống), kích hoạt đẩy tin realtime ngay lập tức
        if (saved.getIsGlobal()) {
            eventPublisher.publishEvent(new NotificationCreatedEvent(this, result, null, true));
        }

        return result;
    }

    @Override
    public void sendToUsers(NotificationDto notiDto, List<String> usernames) {
        if (notiDto == null || usernames == null || usernames.isEmpty()) {
            return;
        }

        // 1. Lưu nội dung thông báo trước
        NotificationDto savedNoti = saveOrUpdate(notiDto);
        if (savedNoti == null || savedNoti.getId() == null) {
            return;
        }

        Notification notificationEntity = notificationRepository.findById(savedNoti.getId()).orElse(null);
        if (notificationEntity == null) {
            return;
        }

        // 2. Tạo danh sách người nhận (batch insert)
        List<NotificationRecipient> recipients = new ArrayList<>();
        List<String> validUsernames = new ArrayList<>();

        for (String username : usernames) {
            if (username == null || username.isBlank()) continue;
            Optional<User> userOpt = userRepository.findByUsername(username);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                
                // Tránh tạo trùng lặp
                Optional<NotificationRecipient> existing = recipientRepository
                        .findByNotificationIdAndUserIdAndIsDeletedFalse(savedNoti.getId(), user.getId());
                
                if (existing.isEmpty()) {
                    NotificationRecipient recipient = new NotificationRecipient();
                    recipient.setNotification(notificationEntity);
                    recipient.setUser(user);
                    recipient.setReceivedAt(LocalDateTime.now());
                    recipient.setReadAt(null);
                    recipients.add(recipient);
                }
                validUsernames.add(username);
            }
        }

        if (!recipients.isEmpty()) {
            recipientRepository.saveAll(recipients);
        }

        // 3. Phát sự kiện Spring để gửi tin qua WebSocket
        eventPublisher.publishEvent(new NotificationCreatedEvent(this, savedNoti, validUsernames, false));
    }

    @Override
    @Transactional(readOnly = true)
    public long countUnreadNotifications(String username) {
        return notificationRepository.countUnreadNotifications(username);
    }

    @Override
    public void markAsRead(UUID notificationId, String username) {
        Optional<Notification> notiOpt = notificationRepository.findById(notificationId);
        if (notiOpt.isEmpty()) {
            return;
        }
        Notification noti = notiOpt.get();
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            return;
        }
        User user = userOpt.get();

        Optional<NotificationRecipient> recipientOpt = recipientRepository
                .findByNotificationIdAndUserUsernameAndIsDeletedFalse(notificationId, username);

        if (recipientOpt.isPresent()) {
            NotificationRecipient r = recipientOpt.get();
            if (r.getReadAt() == null) {
                r.setReadAt(LocalDateTime.now());
                recipientRepository.save(r);
            }
        } else if (noti.getIsGlobal()) {
            // Đối với thông báo global, nếu chưa có bản ghi recipient tức là user chưa đọc
            // Tạo bản ghi mới với trạng thái đã đọc
            NotificationRecipient r = new NotificationRecipient();
            r.setNotification(noti);
            r.setUser(user);
            r.setReceivedAt(noti.getCreateDate() != null ? noti.getCreateDate() : LocalDateTime.now());
            r.setReadAt(LocalDateTime.now());
            recipientRepository.save(r);
        }
    }

    @Override
    public void markAllAsRead(String username) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            return;
        }
        User user = userOpt.get();

        // 1. Đánh dấu đã đọc các thông báo cá nhân chưa đọc
        List<NotificationRecipient> unreadRecipients = recipientRepository
                .findByUserUsernameAndReadAtIsNullAndIsDeletedFalse(username);
        
        for (NotificationRecipient r : unreadRecipients) {
            r.setReadAt(LocalDateTime.now());
        }
        if (!unreadRecipients.isEmpty()) {
            recipientRepository.saveAll(unreadRecipients);
        }

        // 2. Tìm tất cả các thông báo global mà user này CHƯA tạo bản ghi đã đọc
        // Lấy tất cả thông báo global hiện có
        // (Trong thực tế có thể giới hạn số lượng thông báo global gần nhất, ví dụ trong vòng 6 tháng)
        List<Notification> globalNotifications = notificationRepository.findAll().stream()
                .filter(n -> n.getIsGlobal() != null && n.getIsGlobal() && (n.getIsDeleted() == null || !n.getIsDeleted()))
                .collect(Collectors.toList());

        List<NotificationRecipient> newGlobalReadRecipients = new ArrayList<>();
        for (Notification declineNoti : globalNotifications) {
            Optional<NotificationRecipient> rOpt = recipientRepository
                    .findByNotificationIdAndUserUsernameAndIsDeletedFalse(declineNoti.getId(), username);
            if (rOpt.isEmpty()) {
                NotificationRecipient r = new NotificationRecipient();
                r.setNotification(declineNoti);
                r.setUser(user);
                r.setReceivedAt(declineNoti.getCreateDate() != null ? declineNoti.getCreateDate() : LocalDateTime.now());
                r.setReadAt(LocalDateTime.now());
                newGlobalReadRecipients.add(r);
            }
        }
        if (!newGlobalReadRecipients.isEmpty()) {
            recipientRepository.saveAll(newGlobalReadRecipients);
        }
    }
}
