package com.tlu.hrm.repository;

import com.tlu.hrm.model.Notification;
import com.tlu.hrm.enums.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    List<Notification> findByTargetObjectIdInAndIsDeletedFalse(List<UUID> targetObjectIds);


    @Query("SELECT n FROM Notification n WHERE n.isDeleted = false AND (" +
           "n.isGlobal = true OR EXISTS (" +
           "SELECT r FROM NotificationRecipient r WHERE r.notification = n AND r.user.username = :username AND r.isDeleted = false" +
           ")) AND (:notificationType IS NULL OR n.notificationType = :notificationType) ORDER BY n.createDate DESC")
    Page<Notification> findReadableNotifications(
            @Param("username") String username, 
            @Param("notificationType") NotificationType notificationType, 
            Pageable pageable);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.isDeleted = false AND (" +
           "(n.isGlobal = false AND EXISTS (SELECT r FROM NotificationRecipient r WHERE r.notification = n AND r.user.username = :username AND r.readAt IS NULL AND r.isDeleted = false)) OR " +
           "(n.isGlobal = true AND NOT EXISTS (SELECT r FROM NotificationRecipient r WHERE r.notification = n AND r.user.username = :username AND r.isDeleted = false))" +
           ")")
    long countUnreadNotifications(@Param("username") String username);
}
