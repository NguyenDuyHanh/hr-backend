package com.tlu.hrm.service;

import com.tlu.hrm.dto.request.NotificationDto;
import com.tlu.hrm.dto.search.SearchDto;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.UUID;

public interface NotificationService {

    Page<NotificationDto> pagingReadableNotifications(String username, SearchDto searchDto);

    NotificationDto saveOrUpdate(NotificationDto notiDto);

    void sendToUsers(NotificationDto notiDto, List<String> usernames);

    long countUnreadNotifications(String username);

    void markAsRead(UUID notificationId, String username);

    void markAllAsRead(String username);
}
