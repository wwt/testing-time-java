package com.wwt.testing.time.notifications;

import com.wwt.testing.time.Employee;

import java.util.List;
import java.util.stream.Collectors;

public class NotificationService {
    private final List<NotificationGenerator<Employee>> generators;

    public NotificationService(List<NotificationGenerator<Employee>> generators) {
        this.generators = generators;
    }

    public List<Notification> generate(Employee employee) {
        return generators.stream()
                .flatMap(generator -> generator.generate(employee).stream())
                .collect(Collectors.toList());
    }
}
