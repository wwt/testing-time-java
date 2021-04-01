package com.wwt.testing.time;

import java.time.LocalDate;

public record Person(
    String name,
    LocalDate birthday
) {
}
