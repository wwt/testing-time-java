package com.wwt.testing.time.notifications;

import com.wwt.testing.time.Employee;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.threeten.extra.MutableClock;

import java.time.*;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.not;

class NotificationServiceTest {
    private final MutableClock clock = MutableClock.epochUTC();
    private final NotificationService notificationService = new NotificationService(clock);

    @AfterEach
    void after() {
        clock.setInstant(Instant.EPOCH);
    }

    @Test
    void shouldGenerateBirthdayNotificationOnBirthday() {
        clock.setInstant(midnightUtc(LocalDate.of(2021, 3, 14)));

        Employee employee = new Employee("Al Einstein",
                LocalDate.of(1879, 3, 14)
        );

        List<Notification> notifications = notificationService.generate(employee);

        assertThat(notifications)
                .containsExactly(new Notification("Happy Birthday!", "Have a fabulous birthday Al Einstein!"));
    }

    @Test
    void shouldNotGenerateBirthdayNotificationWhenNotYourSpecialDay() {
        clock.setInstant(midnightUtc(LocalDate.of(2021, 3, 15)));

        Employee employee = new Employee("Tom Hermann",
                LocalDate.of(1980, 2, 29)
        );

        List<Notification> notifications = notificationService.generate(employee);

        assertThat(notifications).isEmpty();
    }

    @Test
    void leapYearBirthdayIsHandledOnLeapYear() {
        clock.setInstant(midnightUtc(LocalDate.of(2024, 2, 29)));

        Employee employee = new Employee("Simone Biles",
                LocalDate.of(1997, 3, 14)
        );

        List<Notification> notifications = notificationService.generate(employee);

        assertThat(notifications)
                .containsExactly(new Notification("Happy Birthday!", "Happy birthday Tom Hermann!"));
    }

    @Test
    void leapYearBirthdayIsHandledOnNonLeapYear() {
        clock.setInstant(midnightUtc(LocalDate.of(2021, 3, 1)));

        Employee employee = new Employee("Tom Hermann",
                LocalDate.of(1980, 2, 29)
        );

        List<Notification> notifications = notificationService.generate(employee);

        assertThat(notifications)
                .containsExactly(new Notification("Happy Birthday!", "Happy birthday Tom Hermann!"));
    }

    private static Instant midnightUtc(LocalDate localDate) {
        return LocalDateTime.of(localDate, LocalTime.MIDNIGHT)
                .atZone(ZoneId.of("UTC"))
                .toInstant();
    }
}