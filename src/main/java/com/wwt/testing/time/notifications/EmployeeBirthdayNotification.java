package com.wwt.testing.time.notifications;

import com.wwt.testing.time.Employee;

import java.time.Clock;
import java.time.LocalDate;
import java.time.MonthDay;
import java.util.Optional;

public class EmployeeBirthdayNotification implements NotificationGenerator<Employee> {
    private final Clock clock;

    public EmployeeBirthdayNotification(Clock clock) {
        this.clock = clock;
    }

    @Override
    public Optional<Notification> generate(Employee employee) {
        return Optional.of(employee)
                .filter(this::isBirthday)
                .map(this::createNotification);
    }

    private Notification createNotification(Employee employee) {
        return Notification.of(
                "Happy Birthday!",
                String.format("Have a fabulous birthday %s!", employee.name())
        );
    }

    private boolean isBirthday(Employee employee) {
        LocalDate today = LocalDate.now(clock);
        MonthDay monthDayToday = MonthDay.from(today);
        MonthDay birthday = MonthDay.from(employee.birthday());
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
