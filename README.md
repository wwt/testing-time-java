### Testing Time in Java

> Time isn’t the main thing. It’s the only thing - Miles Davis

Birthdays are the best! Today we're going to write some tested code that creates birthday notifications for people.
Let's explore how we test time based classes with the `java.time` API.

### Dates and Times in Java

The original `java.util.Date` and `java.util.Calendar` classes still exist in modern Java, but should not be used 
unless you have to interoperate with legacy Java libraries. As of JDK 1.8, the ThreeTen project has been integrated into 
the JDK as the [java.time](https://docs.oracle.com/en/java/javase/16/docs/api/java.base/java/time/package-summary.html) package, and is the preferred way to work with dates and time. The API is much cleaner and its classes are immutable and 
thread-safe. 


### Set Up
#### The Person
Let's start with a simple person definition, for our purposes, we just need a name and birthday. 

We can model birthday as a `LocalDate`, which represents a year, month, and day without time-zone or offset information.
`LocalDate` is perfect for a birthday, since it describes a date. To eliminate boilerplate, let's take advantage of Java
16's new record classes:

```java
public record Person(
    String name,
    LocalDate birthday
) {}
```

#### The Notification

A notification consists of a title and a message. Think of it as a message displayed when you log into a website, or a push 
notification destined for your mobile device.

```java
public record Notification(
    String title,
    String message
) {}
```

Given the following interface definition, we want to create a class that is a `NotificationGenerator<Person>`, 
which will produce a notification when it is called on the day of the person's birthday. On any other day the 
generator will produce `Optional.empty()`.

```java
package com.wwt.testing.time.notifications;

import java.util.Optional;

@FunctionalInterface
public interface NotificationGenerator<T> {
    Optional<Notification> generate(T input);
}
```

### What day is it?

In order to test our new `BirthdayNotificationGenerator` we'll need to be able to control time itself. Namely,
we want to be able to externally provide the current date, rather than using a static call like `LocalDate.now()`. This 
enables us to verify the notification is only sent when appropriate.

#### Enter the java.time.Clock

The `java.time.Clock` is responsible for supplying the current instant using a time-zone. Many of the 
date-time constructs in `java.time.*` have a factory method named `now()` that takes a clock as a parameter. This will
create the date/time at the instant provided by the clock in the clock's time-zone.

### The Class Under Test

It feels like we know enough to start building the class under test. Let's make a class that implements `NotificationGenerator<Person>`
and takes a `java.time.Clock` as a constructor parameter, so we can provide the time.

```java
package com.wwt.testing.time.notifications;

import com.wwt.testing.time.Person;
import java.time.Clock;
import java.util.Optional;

public class BirthdayNotificationGenerator implements NotificationGenerator<Person> {

    public BirthdayNotificationGenerator(Clock clock) {
    }
    
    @Override
    public Optional<Notification> generate(Person person) {
        return Optional.empty();
    }
}
```

### The First Tests

Now we need a `BirthdayNotificationGeneratorTest` that has an instance of the `BirthdayNotificationGenerator`. We'll 
need a `Clock` to create our notification generator, but what clock should we use? The `java.time.Clock` comes with a [fixed implementation](https://docs.oracle.com/javase/8/docs/api/java/time/Clock.html#fixed-java.time.Instant-java.time.ZoneId-), 
that will always produce the same instant once configured, but since we went with constructor injection, we don't want 
to be required to recreate the class under test in each test case.

#### Mock Clock?

Let's look at how the now method of `java.time.LocalDate` works:
```java
public static LocalDate now(Clock clock) {
    Objects.requireNonNull(clock, "clock");
    final Instant now = clock.instant();  // called once
    return ofInstant(now, clock.getZone());
}
```

We could use Mockito to mock `java.time.Clock` and specify what zone and instant the clock provides. In order to
do that we'll need to mock out the instant and time zone. 

Our test set up would require something like this with mocks:
```java
package com.wwt.testing.time.notifications;

import com.wwt.testing.time.Person;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.*;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BirthdayNotificationGeneratorMockTest {
    @Mock
    private Clock clock;
    @InjectMocks
    private BirthdayNotificationGenerator testObject;

    @DisplayName("Should generate notification on birthday")
    @Test
    void shouldGenerateNotificationOnBirthday() {
        ZonedDateTime dateTime = LocalDate.of(2021, 3, 14)
                .atStartOfDay()
                .atZone(ZoneId.of("America/Chicago"));
        when(clock.instant()).thenReturn(dateTime.toInstant());
        when(clock.getZone()).thenReturn(dateTime.getZone());
        Person person = new Person("Al Einstein", LocalDate.of(1879, 3, 14));

        Optional<Notification> notification = testObject.generate(person);

        assertThat(notification)
                .contains(new Notification("Happy Birthday!", "Have a fabulous birthday Al Einstein!"));
    }
}
```

Seems like there is lots of ceremony for a partially working clock. Let's keep exploring our options.

### ThreeTen-Extra to the Rescue!
[ThreeTen-Extra](https://www.threeten.org/threeten-extra/), an optional part of the ThreeTen project, provides a [mutable clock](https://www.threeten.org/threeten-extra/apidocs/org.threeten.extra/org/threeten/extra/MutableClock.html),
which does not advance time on its own. `MutableClock` can be initialized through static [factory methods](https://www.threeten.org/threeten-extra/apidocs/org.threeten.extra/org/threeten/extra/MutableClock.html#epochUTC())
that start the clock off at a specific instant in a time zone. The configured instance is mutable, so you can change the
date and time using its `set` methods or time can be advanced using its `add` methods.

### It's your birthday!

For our first tests, we should cover the basic cases:
1. A notification should be generated on a person's birthday.
1. A notification should not be generated when it isn't a person's birthday.

```java
package com.wwt.testing.time.notifications;

import com.wwt.testing.time.Person;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.threeten.extra.MutableClock;

import java.time.LocalDate;
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
}
```

Most of these tests will have a similar setup: create a person with a specific birthday, set the date for the scenario,
generate an optional notification, and verify the results. 

In this example we want to prove that on your birthday, you get a notification. We set Albert's birthday to 3/14/1879, 
the clock to 3/14/2021, and assert a notification is generated with the expected content.

If we wanted to be more specific, the clock also has a `setInstant(...)` method that allows you to set the clock to an
exact point in time. We only care about the day, so we'll just [set](https://www.threeten.org/threeten-extra/apidocs/org.threeten.extra/org/threeten/extra/MutableClock.html#set(java.time.temporal.TemporalAdjuster)) 
the date.  

Once we pass that test, we'll want to verify that you don't get notified when it isn't your birthday:

```java
@Test
@DisplayName("Should not generate notification when not birthday")
void shouldNotGenerateNotificationWhenNotBirthday() {
    Person person = new Person("Ted Fitzgerald", LocalDate.of(1984, 6, 22));
    clock.set(LocalDate.of(2021, 3, 15));

    Optional<Notification> notifications = testObject.generate(person);

    assertThat(notifications).isEmpty();
}
```

An implementation that passes those two tests might look something like this:

```java
package com.wwt.testing.time.notifications;

import com.wwt.testing.time.Person;

import java.time.Clock;
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
        var message = String.format("Have a fabulous birthday %s!", person.name());
        return Notification.of("Happy Birthday!", message);
    }

    private boolean isBirthday(Person person) {
        var now = MonthDay.now(clock);
        return MonthDay.from(person.birthday()).equals(now);
    }
}
```

Let's dive into the `isBirthday(Person person)` method. In `java.time` there is a [MonthDay](https://docs.oracle.com/javase/8/docs/api/java/time/MonthDay.html) 
class that represents a month day combination. `MonthDay` has a `now(...)` factory method that takes a clock to provide the 
current month and day. Using the injected clock, we determine the current `MonthDay`, and compare that to the month and
day of the person's birthday. When this method returns true, a notification is generated with an uplifting message.

### Testing edge cases

Looks great, ship it; right? We have one more consideration: people born Feb 29th on a leap year will only be notified 
once every four years with the current implementation. We don't want to forget about them!

```java
@Test
@DisplayName("On non-leap year, notify Feb 29th birthday on March 1st")
void leapBirthdayNotifiedOnMarchFirstOnNormalYear() {
    Person person = new Person("Ja Rule", LocalDate.of(1976, 2, 29));
    clock.set(LocalDate.of(2021, 3, 1));

    Optional<Notification> notifications = testObject.generate(person);

    assertThat(notifications)
            .contains(new Notification("Happy Birthday!", "Have a fabulous birthday Ja Rule!"));
}
```

The procedure is the same, but in this case we'll create a person with a birthday on February 29th, and verify that on a
non-leap year they will be notified on March 1st instead.

We'll wrap up testing with the following two cases, and _then_ we'll be ready to ship:
- On leap year, notify Feb 29th birthday on exact day
- So that we don't notify twice on leap year, on leap year, do not notify Feb 29th birthday on March 1

If you want to see the completed solution, check out the [repo on GitHub](https://github.com/wwt/testing-time-java)!

### Additional Resources
- [Project GitHub](https://github.com/wwt/testing-time-java)
- [ThreeTen Homepage](https://www.threeten.org/)
- [ThreeTen Extra](https://www.threeten.org/threeten-extra/)
- [Mutable Clock](https://www.threeten.org/threeten-extra/apidocs/org.threeten.extra/org/threeten/extra/MutableClock.html)
