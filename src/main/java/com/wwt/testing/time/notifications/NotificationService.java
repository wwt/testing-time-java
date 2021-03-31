package com.wwt.testing.time.notifications;

import com.wwt.testing.time.Employee;

import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class NotificationService {
    private final List<NotificationGenerator<Employee>> generators = new ArrayList<>();

    public NotificationService(Clock clock) {
        generators.add(new EmployeeBirthdayNotification(clock));
    }

    public List<Notification> generate(Employee employee) {
        return generators.stream()
                .flatMap(generator -> generator.generate(employee).stream())
                .collect(Collectors.toList());
    }
}
