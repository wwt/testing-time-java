package com.wwt.testing.time.notifications;

import com.wwt.testing.time.Employee;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.threeten.extra.MutableClock;

import java.time.*;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class EmployeeBirthdayNotificationTest {
    private final MutableClock clock = MutableClock.epochUTC();
    private final EmployeeBirthdayNotification birthdayNotification = new EmployeeBirthdayNotification(clock);

    @AfterEach
    void after() {
        clock.setInstant(Instant.EPOCH);
    }

    @Test
    void shouldGenerateBirthdayNotificationOnBirthday() {
        clock.setInstant(midnightUtc(LocalDate.of(2021, 3, 14)));

        Employee employee = new Employee("Al Einstein", LocalDate.of(1879, 3, 14));

        Optional<Notification> notification = birthdayNotification.generate(employee);

        assertThat(notification)
                .contains(new Notification("Happy Birthday!", "Have a fabulous birthday Al Einstein!"));
    }

    @Test
    void shouldNotGenerateBirthdayNotificationWhenNotYourSpecialDay() {
        clock.setInstant(midnightUtc(LocalDate.of(2021, 3, 15)));
        Employee employee = new Employee("Tom Hermann", LocalDate.of(1980, 3, 14));

        Optional<Notification> notifications = birthdayNotification.generate(employee);

        assertThat(notifications).isEmpty();
    }

    @Test
    void leapYearBirthdayIsHandledOnDayOfBirthdayDuringLeapYear() {
        clock.setInstant(midnightUtc(LocalDate.of(2024, 2, 29)));
        Employee employee = new Employee("Ja Rule", LocalDate.of(1976, 2, 29));

        Optional<Notification> notification = birthdayNotification.generate(employee);

        assertThat(notification)
                .contains(new Notification("Happy Birthday!", "Have a fabulous birthday Ja Rule!"));
    }

    @Test
    void leapYearBirthdayIsHandledOnNonLeapYear() {
        clock.setInstant(midnightUtc(LocalDate.of(2021, 3, 1)));

        Employee employee = new Employee("Saul Williams", LocalDate.of(1972, 2, 29));

        Optional<Notification> notifications = birthdayNotification.generate(employee);

        assertThat(notifications)
                .contains(new Notification("Happy Birthday!", "Have a fabulous birthday Saul Williams!"));
    }

    private static Instant midnightUtc(LocalDate localDate) {
        return LocalDateTime.of(localDate, LocalTime.MIDNIGHT)
                .atZone(ZoneId.of("UTC"))
                .toInstant();
    }
}