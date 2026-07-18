package com.tlu.hrm.repository;

import com.tlu.hrm.model.NotificationRecipient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NotificationRecipientRepository extends JpaRepository<NotificationRecipient, UUID> {

    Optional<NotificationRecipient> findByNotificationIdAndUserUsernameAndIsDeletedFalse(UUID notificationId, String username);

    Optional<NotificationRecipient> findByNotificationIdAndUserIdAndIsDeletedFalse(UUID notificationId, UUID userId);

    List<NotificationRecipient> findByUserUsernameAndIsDeletedFalse(String username);

    List<NotificationRecipient> findByUserUsernameAndReadAtIsNullAndIsDeletedFalse(String username);
}
