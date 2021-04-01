package com.wwt.testing.time.notifications;

import com.wwt.testing.time.Employee;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationServiceTest {
    private final NotificationService notificationService = new NotificationService(List.of(
        employee -> Optional.of(Notification.of("Howdy", "Hello " + employee.name())),
        employee -> Optional.empty(),
        employee -> Optional.of(Notification.of("Hi Again!", "Yo, " + employee.name() + "!")))
    );

    @Test
    void generatesNotificationsUsingRegisteredGenerators() {
        Employee employee = new Employee("Bob Ross", LocalDate.of(1942, 10, 29));

        List<Notification> notifications = notificationService.generate(employee);

        assertThat(notifications)
                .hasSize(2)
                .containsExactly(
                        Notification.of("Howdy", "Hello Bob Ross"),
                        Notification.of("Hi Again!", "Yo, Bob Ross!")
                );

    }
}