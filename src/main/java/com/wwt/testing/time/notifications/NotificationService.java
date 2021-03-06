package com.wwt.testing.time.notifications;

import com.wwt.testing.time.Person;

import java.util.List;
import java.util.stream.Stream;

public class NotificationService {
    private final List<NotificationGenerator<Person>> generators;

    public NotificationService(List<NotificationGenerator<Person>> generators) {
        this.generators = generators;
    }

    public Stream<Notification> generate(Person person) {
        return generators.stream()
                .flatMap(generator -> generator.generate(person).stream());
    }
}
