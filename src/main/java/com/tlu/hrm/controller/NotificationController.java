package com.tlu.hrm.controller;

import com.tlu.hrm.dto.request.NotificationDto;
import com.tlu.hrm.dto.response.ApiResponse;
import com.tlu.hrm.dto.search.SearchDto;
import com.tlu.hrm.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    private String getCurrentUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    @PostMapping("/paging")
    public ResponseEntity<ApiResponse<Page<NotificationDto>>> getMyNotifications(@RequestBody(required = false) SearchDto searchDto) {
        if (searchDto == null) {
            searchDto = new SearchDto();
        }
        Page<NotificationDto> result = notificationService.pagingReadableNotifications(getCurrentUsername(), searchDto);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách thông báo thành công", result));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount() {
        long count = notificationService.countUnreadNotifications(getCurrentUsername());
        return ResponseEntity.ok(ApiResponse.success("Lấy số lượng thông báo chưa đọc thành công", count));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(@PathVariable UUID id) {
        notificationService.markAsRead(id, getCurrentUsername());
        return ResponseEntity.ok(ApiResponse.success("Đã đánh dấu đọc thông báo", null));
    }

    @PutMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead() {
        notificationService.markAllAsRead(getCurrentUsername());
        return ResponseEntity.ok(ApiResponse.success("Đã đánh dấu đọc tất cả thông báo", null));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<NotificationDto>> createNotification(@RequestBody NotificationDto notiDto) {
        NotificationDto result = notificationService.saveOrUpdate(notiDto);
        if (result != null) {
            return ResponseEntity.ok(ApiResponse.success("Tạo thông báo thành công", result));
        }
        return ResponseEntity.badRequest().body(ApiResponse.error("Tạo thông báo thất bại"));
    }
}
