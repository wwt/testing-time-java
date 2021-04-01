package com.wwt.testing.time.notifications;

import com.wwt.testing.time.Person;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationServiceTest {
    private final NotificationService notificationService = new NotificationService(List.of(
            person -> Optional.of(Notification.of("Howdy", "Hello " + person.name())),
            person -> Optional.empty(),
            person -> Optional.of(Notification.of("Hi Again!", "Yo, " + person.name() + "!")))
    );

    @Test
    void generatesNotificationsUsingRegisteredGenerators() {
        Person person = new Person("Bob Ross", LocalDate.of(1942, 10, 29));

        Stream<Notification> notifications = notificationService.generate(person);

        assertThat(notifications)
                .hasSize(2)
                .containsExactly(
                        Notification.of("Howdy", "Hello Bob Ross"),
                        Notification.of("Hi Again!", "Yo, Bob Ross!")
                );

    }
}