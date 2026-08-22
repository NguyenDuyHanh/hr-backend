package com.tlu.hrm.service.impl;

import com.tlu.hrm.dto.request.AnnouncementDto;
import com.tlu.hrm.dto.request.NotificationDto;
import com.tlu.hrm.dto.search.SearchDto;
import com.tlu.hrm.enums.AnnouncementCategory;
import com.tlu.hrm.enums.AnnouncementStatus;
import com.tlu.hrm.enums.NotificationType;
import com.tlu.hrm.model.Announcement;
import com.tlu.hrm.model.User;
import com.tlu.hrm.model.Notification;
import com.tlu.hrm.model.NotificationRecipient;
import com.tlu.hrm.repository.AnnouncementRepository;
import com.tlu.hrm.repository.UserRepository;
import com.tlu.hrm.repository.NotificationRecipientRepository;
import com.tlu.hrm.repository.NotificationRepository;
import com.tlu.hrm.service.AnnouncementService;
import com.tlu.hrm.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
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
public class AnnouncementServiceImpl implements AnnouncementService {

    @Autowired
    private AnnouncementRepository announcementRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private NotificationRecipientRepository recipientRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<AnnouncementDto> search(String username, String keyword, AnnouncementCategory category, AnnouncementStatus status, UUID deptId, SearchDto searchDto) {
        int pageIndex = searchDto.getPageIndex() >= 1 ? searchDto.getPageIndex() - 1 : 0;
        int pageSize = searchDto.getPageSize() > 0 ? searchDto.getPageSize() : 10;
        Pageable pageable = PageRequest.of(pageIndex, pageSize);

        Page<Announcement> page = announcementRepository.searchAnnouncements(keyword, category, status, deptId, pageable);

        if (page.isEmpty()) {
            return Page.empty(pageable);
        }

        // Tối ưu hóa: lấy danh sách recipient của user hiện tại
        List<NotificationRecipient> recipients = recipientRepository.findByUserUsernameAndIsDeletedFalse(username);
        Map<UUID, NotificationRecipient> recipientMap = new HashMap<>();
        for (NotificationRecipient r : recipients) {
            if (r.getNotification() != null) {
                recipientMap.put(r.getNotification().getId(), r);
            }
        }

        // Lấy danh sách ID của các announcement trong trang hiện tại
        List<UUID> announcementIds = page.getContent().stream()
                .map(Announcement::getId)
                .collect(Collectors.toList());

        // Lấy tất cả notifications tương ứng với các announcements này
        List<Notification> notifications = notificationRepository.findByTargetObjectIdInAndIsDeletedFalse(announcementIds);
        Map<UUID, Notification> notiMap = new HashMap<>();
        for (Notification n : notifications) {
            if (n.getTargetObjectId() != null) {
                notiMap.put(n.getTargetObjectId(), n);
            }
        }

        List<AnnouncementDto> dtoList = page.getContent().stream()
                .map(ann -> {
                    AnnouncementDto dto = convertToDto(ann);
                    Notification n = notiMap.get(ann.getId());
                    if (n != null) {
                        NotificationRecipient r = recipientMap.get(n.getId());
                        dto.setIsRead(r != null && r.getReadAt() != null);
                    } else {
                        // Nếu không có notification tương ứng (ví dụ: đang là bản nháp) -> mặc định là đã đọc
                        dto.setIsRead(true);
                    }
                    return dto;
                })
                .collect(Collectors.toList());

        return new PageImpl<>(dtoList, pageable, page.getTotalElements());
    }

    private AnnouncementDto convertToDto(Announcement ann) {
        if (ann == null) {
            return null;
        }
        AnnouncementDto dto = new AnnouncementDto(ann);
        if (ann.getCreatedBy() != null) {
            Optional<User> userOpt = userRepository.findByUsername(ann.getCreatedBy());
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                if (user.getStaff() != null && user.getStaff().getDisplayName() != null) {
                    dto.setCreatedBy(user.getStaff().getDisplayName());
                }
            }
        }
        return dto;
    }

    @Override
    public String generateAnnouncementCode() {
        int currentYear = java.time.LocalDate.now().getYear();
        String prefix = "TB_" + currentYear + "_";
        String maxCode = announcementRepository.findMaxCodeByPrefix(prefix);
        int nextSeq = 1;
        if (maxCode != null && maxCode.startsWith(prefix)) {
            try {
                String suffix = maxCode.substring(prefix.length());
                nextSeq = Integer.parseInt(suffix) + 1;
            } catch (NumberFormatException e) {
                // Ignore
            }
        }
        return String.format("%s%04d", prefix, nextSeq);
    }

    @Override
    public AnnouncementDto saveOrUpdate(AnnouncementDto dto) {
        if (dto == null || dto.getTitle() == null || dto.getContent() == null) {
            throw new IllegalArgumentException("Tiêu đề và nội dung thông báo không được để trống");
        }

        Announcement entity;
        boolean isNew = (dto.getId() == null);
        AnnouncementStatus oldStatus = null;

        if (!isNew) {
            Optional<Announcement> opt = announcementRepository.findById(dto.getId());
            if (opt.isPresent()) {
                entity = opt.get();
                oldStatus = entity.getStatus();
                if (entity.getCode() == null) {
                    entity.setCode(generateAnnouncementCode());
                }
            } else {
                throw new IllegalArgumentException("Không tìm thấy thông báo có ID: " + dto.getId());
            }
        } else {
            entity = new Announcement();
            entity.setCode(generateAnnouncementCode());
        }

        entity.setTitle(dto.getTitle());
        entity.setContent(dto.getContent());
        entity.setTitleImageUrl(dto.getTitleImageUrl());
        entity.setCategory(dto.getCategory());
        entity.setStatus(dto.getStatus());
        entity.setAttachments(dto.getAttachments());
        entity.setTargetDeptId(dto.getTargetDeptId());

        boolean isPublishing = (dto.getStatus() == AnnouncementStatus.PUBLISHED && (isNew || oldStatus == AnnouncementStatus.DRAFT));
        if (isPublishing) {
            entity.setPublishDate(LocalDateTime.now());
        }

        Announcement saved = announcementRepository.save(entity);
        AnnouncementDto result = convertToDto(saved);

        // Đẩy thông báo hệ thống và realtime khi được ban hành
        if (isPublishing) {
            String rawText = saved.getContent().replaceAll("<[^>]*>", " ").trim();
            rawText = rawText.replaceAll("\\s+", " ");
            String previewText = rawText.length() > 200 ? rawText.substring(0, 197) + "..." : rawText;

            NotificationDto noti = new NotificationDto();
            noti.setTitle("Thông báo mới: " + saved.getTitle());
            noti.setContent(previewText);
            noti.setNotificationType(NotificationType.ANNOUNCEMENT);
            noti.setTargetObjectId(saved.getId());
            noti.setLinkUrl("/announcements");
            noti.setIsGlobal(saved.getTargetDeptId() == null);

            if (saved.getTargetDeptId() != null) {
                List<User> targetUsers = userRepository.findUsersByDepartmentId(saved.getTargetDeptId());
                List<String> usernames = targetUsers.stream()
                        .map(User::getUsername)
                        .collect(Collectors.toList());
                if (!usernames.isEmpty()) {
                    notificationService.sendToUsers(noti, usernames);
                }
            } else {
                notificationService.saveOrUpdate(noti);
            }
        }

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public AnnouncementDto getById(UUID id) {
        return announcementRepository.findById(id)
                .filter(a -> a.getIsDeleted() == null || !a.getIsDeleted())
                .map(this::convertToDto)
                .orElse(null);
    }

    @Override
    public void delete(UUID id) {
        Optional<Announcement> opt = announcementRepository.findById(id);
        if (opt.isPresent()) {
            Announcement entity = opt.get();
            entity.setIsDeleted(true);
            announcementRepository.save(entity);
        }
    }
}
