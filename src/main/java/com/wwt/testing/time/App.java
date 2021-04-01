package com.wwt.testing.time;

import com.wwt.testing.time.notifications.BirthdayNotificationGenerator;
import com.wwt.testing.time.notifications.NotificationService;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

public class App implements Runnable {
    private final NotificationService notificationService;
    private final Stream<Person> people;

    private App(Stream<Person> people) {
        this.people = people;
        this.notificationService = new NotificationService(List.of(
            new BirthdayNotificationGenerator(Clock.systemDefaultZone())
        ));
    }

    @Override
    public void run() {
        people
            .map(notificationService::generate)
            .flatMap(Collection::stream)
            .map(notification -> String.format("%s\t%s", notification.title(), notification.message()))
            .forEach(System.out::println);
    }

    public static void main(String... args) {
        Stream<Person> people = Stream.of(
                new Person("Party Animal", LocalDate.now().minusYears(21)),
                new Person("Margaret Hamilton", LocalDate.of(1936, 8, 17)),
                new Person("James Gosling", LocalDate.of(1955, 5, 19)));

        new App(people).run();
    }
}
