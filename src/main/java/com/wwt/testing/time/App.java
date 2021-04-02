package com.wwt.testing.time;

import com.wwt.testing.time.notifications.BirthdayNotificationGenerator;
import com.wwt.testing.time.notifications.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Stream;

public class App implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(App.class);
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
        String formattedDay = ZonedDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        logger.info("Generating notifications for today, {}", formattedDay);
        logger.info("-".repeat(47));

        people
            .flatMap(notificationService::generate)
            .map(notification -> String.format("* %s %s", notification.title(), notification.message()))
            .forEach(logger::info);
    }

    public static void main(String... args) {
        Stream<Person> people = Stream.of(
            new Person("Party Animal", LocalDate.now().minusYears(21)),
            new Person("Margaret Hamilton", LocalDate.of(1936, 8, 17)),
            new Person("James Gosling", LocalDate.of(1955, 5, 19)),
            new Person("Joshua J. Bloch", LocalDate.of(1961, 8, 28))
        );

        new App(people).run();
    }
}
