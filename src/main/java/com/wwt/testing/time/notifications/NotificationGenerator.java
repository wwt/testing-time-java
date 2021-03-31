package com.wwt.testing.time.notifications;

import java.util.Optional;

@FunctionalInterface
public interface NotificationGenerator<T> {
    Optional<Notification> generate(T input);
}
