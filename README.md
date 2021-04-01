### Testing Time in Java

> Time isn’t the main thing. It’s the only thing - Miles Davis

Birthdays are the best! Today we're going to write some tested code that creates birthday notifications for people. Let's explore how we test time based classes with the
`java.time` API.

### Set Up

#### The Person
Let's start with a simple person definition, for our purposes, we just need a name and birthday. Since Java 8, 
the standard date/time API is JSR-310, so we'll go ahead and model birthday as a `LocalDate`. Local date represents
a year, month, and day without information about the time-zone or offset, which is perfect for a birthday.

```java
public record Person(
    String name,
    LocalDate birthday
) {}
```

#### The Notification

A notification consists of a title and a message. Think of it as something you see when you log into a website, or a push 
notification destined for your mobile device.

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

The `java.time.Clock` is responsible for supplying the current instant using a time-zone. Each of the 
date-time constructs in `java.time.*` have a factory method named `now()` that takes the clock as a parameter, which
will create the date/time at the instant provided by the clock.

### The Class Under Test

It feels like we know enough to start building the class under test. Let's make a class that implements `NotificationGenerator<Person>`
and takes a `java.time.Clock` as a constructor parameter, so we can set the time.

```java
package com.wwt.testing.time.notifications;

import com.wwt.testing.time.Person;

import java.time.Clock;
import java.time.LocalDate;
import java.time.MonthDay;
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

As always, lets create a test class for our `BirthdayNotificationGenerator` and instantiate the class under test. The first
decision we're going to need to make is which `Clock` to use? The `java.time.Clock` comes with a [fixed implementation](https://docs.oracle.com/javase/8/docs/api/java/time/Clock.html#fixed-java.time.Instant-java.time.ZoneId-), 
that will always produce the same instant once configured, but since we went with constructor injection, we don't want 
to have to recreate the test instance in each test case. 

[ThreeTen-Extra](https://www.threeten.org/threeten-extra/), an optional part of the JSR-310 project, provides a [mutable clock](https://www.threeten.org/threeten-extra/apidocs/org.threeten.extra/org/threeten/extra/MutableClock.html)
implementation that lets you set the current time. That seems like a good fit for our needs, so we can add a test dependency in our `build.gradle` on `"org.threeten:threeten-extra:1.6.0"` and get testing!

```java
class BirthdayNotificationTest {
    private final MutableClock clock = MutableClock.epochUTC();
    private final BirthdayNotificationGenerator testObject = new BirthdayNotificationGenerator(clock);
}
```

For our first tests, we should verify: 
1. A notification is generated on a person's birthday
1. A notification is not generated when it isn't a person's birthday

### It's your birthday!
```java
@Test
@DisplayName("Should generate notification on birthday")
void shouldGenerateNotificationOnBirthday() {
    Person person = new Person("Al Einstein", LocalDate.of(1879, 3, 14));
    clock.set(LocalDate.of(2021, 3, 14));

    Optional<Notification> notification = testObject.generate(person);

    assertThat(notification)
            .contains(new Notification("Happy Birthday!", "Have a fabulous birthday Al Einstein!"));
}
```

Most of these tests will have a similar setup: create a person with a specific birthday, set the clock for the scenario,
generate an optional notification, and verify the results. 

In this example we want to prove that on your birthday, you get a notification. We set Albert's birthday to 3/14/1879, 
the clock to 3/14/2021, and asserted a notification was generated with the expected content.

If we wanted to be more specific, the clock also has a `setInstant(...)` method that allows you to set the clock to an
exact point in time. We only care about the day, so we'll just [set](https://www.threeten.org/threeten-extra/apidocs/org.threeten.extra/org/threeten/extra/MutableClock.html#set(java.time.temporal.TemporalAdjuster)) the date.  

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

The implementation I came up with to pass those two basic tests looked something like this: 

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

Let's take a quick look at the `isBirthday(Person person)` method. In JSR-310 there is a MonthDay class that represents 
a month day combo. `MonthDay` has a `now(...)` factory method that takes a clock to provide the current month and day. 
We use injected clock, so the time set by our `MutableClock` in our test configuration gets used.

### Testing edge cases

Looks great, ship it; right? We have one more consideration, people born Feb 29th on a leap year will only be notified 
once every four years with the current implementation. We don't want to forget about them!

```java
@Test
@DisplayName("On normal year, notify Feb 29th birthday on March 1st")
void leapBirthdayNotifiedOnMarchFirstOnNormalYear() {
    Person person = new Person("Ja Rule", LocalDate.of(1976, 2, 29));
    clock.set(LocalDate.of(2021, 3, 1));

    Optional<Notification> notifications = testObject.generate(person);

    assertThat(notifications)
            .contains(new Notification("Happy Birthday!", "Have a fabulous birthday Ja Rule!"));
}
```

The procedure is the same, but in this case we'll create a person with a birthday on February 29th, and verify that on a
non-leap year they get notified on March 1st instead.

We'll wrap up testing with the following two cases, and we'll be ready to ship:
- On leap year, you should be notified of a Feb 29th birthday on the exact day
- On leap year, you should be not be notified on March 1st

If you want to see the completed solution, check out the [repo on GitHub](https://github.com/wwt/testing-time-java)!

### Additional Resources
- [Project GitHub](https://github.com/wwt/testing-time-java)
- [ThreeTen Homepage](https://www.threeten.org/)
- [ThreeTen Extra](https://www.threeten.org/threeten-extra/)
- [Mutable Clock](https://www.threeten.org/threeten-extra/apidocs/org.threeten.extra/org/threeten/extra/MutableClock.html)
