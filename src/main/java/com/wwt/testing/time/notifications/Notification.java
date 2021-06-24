package com.wwt.testing.time.notifications;

import java.util.Objects;

public class Notification {
    private final String title;
    private final String message;

    public Notification(String title, String message) {
        this.title = title;
        this.message = message;
    }

    public String title() {
        return title;
    }

    public String message() {
        return message;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Notification) obj;
        return Objects.equals(this.title, that.title) &&
                Objects.equals(this.message, that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, message);
    }

    @Override
    public String toString() {
        return "Notification[" +
                "title=" + title + ", " +
                "message=" + message + ']';
    }

    public static Notification of(String title, String message) {
        return new Notification(title, message);
    }
}