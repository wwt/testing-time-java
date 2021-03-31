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
        MonthDay today = MonthDay.now(clock);
        LocalDate birthday = employee.birthday();
        return isExactBirthday(birthday, today) || isLeapYearBirthday(birthday, today);
    }

    private boolean isExactBirthday(LocalDate birthday, MonthDay today) {
        return MonthDay.from(birthday).equals(today);
    }

    private boolean isLeapYearBirthday(LocalDate birthday, MonthDay today) {
        return MonthDay.from(birthday).equals(MonthDay.of(2, 29))
                && today.equals(MonthDay.of(3, 1));
    }
}
