package com.ezra.task.notification;

import org.springframework.stereotype.Component;

@Component
public final class ConsoleNotificationChannel implements NotificationChannel {
    @Override
    public void send(String message) {
        IO.println(message);
    }
}
