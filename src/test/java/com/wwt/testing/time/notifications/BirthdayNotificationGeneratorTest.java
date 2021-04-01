package com.wwt.testing.time.notifications;

import com.wwt.testing.time.Person;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.threeten.extra.MutableClock;

import java.time.*;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class BirthdayNotificationGeneratorTest {
    private final MutableClock clock = MutableClock.epochUTC();
    private final BirthdayNotificationGenerator testObject = new BirthdayNotificationGenerator(clock);

    @Test
    @DisplayName("Should generate notification on birthday")
    void shouldGenerateNotificationOnBirthday() {
        Person person = new Person("Al Einstein", LocalDate.of(1879, 3, 14));
        clock.set(LocalDate.of(2021, 3, 14));

        Optional<Notification> notification = testObject.generate(person);

        assertThat(notification)
                .contains(new Notification("Happy Birthday!", "Have a fabulous birthday Al Einstein!"));
    }

    @Test
    @DisplayName("Should not generate notification when not birthday")
    void shouldNotGenerateNotificationWhenNotBirthday() {
        Person person = new Person("Tom Hermann", LocalDate.of(1980, 3, 14));
        clock.set(LocalDate.of(2021, 3, 15));

        Optional<Notification> notifications = testObject.generate(person);

        assertThat(notifications).isEmpty();
    }

    @Nested
    @DisplayName("Should handle leap birthdays")
    class LeapYearTests {

        @Test
        @DisplayName("On normal year, notify Feb 29th birthday on March 1st")
        void leapBirthdayNotifiedOnMarchFirstOnNormalYear() {
            Person person = new Person("Saul Williams", LocalDate.of(1972, 2, 29));
            clock.set(LocalDate.of(2021, 3, 1));

            Optional<Notification> notifications = testObject.generate(person);

            assertThat(notifications)
                    .contains(new Notification("Happy Birthday!", "Have a fabulous birthday Saul Williams!"));
        }

        @Test
        @DisplayName("On leap year, notify Feb 29th birthday on exact day")
        void leapBirthdayIsHandledOnExactDayDuringLeapYear() {
            Person person = new Person("Ja Rule", LocalDate.of(1976, 2, 29));
            clock.set(LocalDate.of(2024, 2, 29));

            Optional<Notification> notification = testObject.generate(person);

            assertThat(notification)
                    .contains(new Notification("Happy Birthday!", "Have a fabulous birthday Ja Rule!"));
        }

        @Test
        @DisplayName("On leap year, do not notify Feb 29th birthday on March 1")
        void doNotNotifyTwiceOnLeapYear() {
            Person person = new Person("Saul Williams", LocalDate.of(1972, 2, 29));
            clock.set(LocalDate.of(2020, 3, 1));

            Optional<Notification> notifications = testObject.generate(person);

            assertThat(notifications).isEmpty();
        }
    }
}
