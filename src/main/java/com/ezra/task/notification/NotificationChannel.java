package com.ezra.task.notification;

//  this will be implemented will all the notification channels eg Email notification channel, SMS notification channel etc
// For now I am printing to the console for simplicity
public sealed interface NotificationChannel permits ConsoleNotificationChannel {
    void send(String message);
}
