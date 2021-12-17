package com.wwt.testing.time.notifications;

import com.wwt.testing.time.Person;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BirthdayNotificationGeneratorMockTest {
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
