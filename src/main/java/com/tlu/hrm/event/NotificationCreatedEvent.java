package com.tlu.hrm.event;

import com.tlu.hrm.dto.request.NotificationDto;
import org.springframework.context.ApplicationEvent;

import java.util.List;

public class NotificationCreatedEvent extends ApplicationEvent {

    private final NotificationDto notification;
    private final List<String> recipientUsernames;
    private final boolean isGlobal;

    public NotificationCreatedEvent(Object source, NotificationDto notification, List<String> recipientUsernames, boolean isGlobal) {
        super(source);
        this.notification = notification;
        this.recipientUsernames = recipientUsernames;
        this.isGlobal = isGlobal;
    }

    public NotificationDto getNotification() {
        return notification;
    }

    public List<String> getRecipientUsernames() {
        return recipientUsernames;
    }

    public boolean isGlobal() {
        return isGlobal;
    }
}
