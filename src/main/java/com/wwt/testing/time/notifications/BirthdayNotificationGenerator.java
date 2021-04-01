package com.wwt.testing.time.notifications;

import com.wwt.testing.time.Person;

import java.time.Clock;
import java.time.LocalDate;
import java.time.MonthDay;
import java.util.Optional;

public class BirthdayNotificationGenerator implements NotificationGenerator<Person> {
    private final Clock clock;

    public BirthdayNotificationGenerator(Clock clock) {
        this.clock = clock;
    }

    @Override
    public Optional<Notification> generate(Person person) {
        return Optional.of(person)
                .filter(this::isBirthday)
                .map(this::createNotification);
    }

    private Notification createNotification(Person person) {
        return Notification.of(
                "Happy Birthday!",
                String.format("Have a fabulous birthday %s!", person.name())
        );
    }

    private boolean isBirthday(Person person) {
        LocalDate today = LocalDate.now(clock);
        MonthDay monthDayToday = MonthDay.from(today);
        MonthDay birthday = MonthDay.from(person.birthday());
        return isBirthdayToday(birthday, monthDayToday) || isLeapBirthdayObserved(birthday, today);
    }

    private boolean isBirthdayToday(MonthDay birthday, MonthDay today) {
        return birthday.equals(today);
    }

    private boolean isLeapBirthdayObserved(MonthDay birthday, LocalDate today) {
        return !today.isLeapYear() &&
                MonthDay.of(2, 29).equals(birthday) &&
                MonthDay.of(3, 1).equals(MonthDay.from(today));
    }
}
