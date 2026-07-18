package com.tlu.hrm.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class NotificationWebSocketListener {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onNotificationCreated(NotificationCreatedEvent event) {
        if (event.isGlobal()) {
            // Đẩy realtime cho toàn công ty qua topic chung
            messagingTemplate.convertAndSend("/topic/notifications", event.getNotification());
        } else {
            // Đẩy realtime cho từng người nhận riêng lẻ qua queue cá nhân
            if (event.getRecipientUsernames() != null) {
                for (String username : event.getRecipientUsernames()) {
                    if (username != null && !username.isBlank()) {
                        messagingTemplate.convertAndSendToUser(
                            username, 
                            "/queue/notifications", 
                            event.getNotification()
                        );
                    }
                }
            }
        }
    }
}
