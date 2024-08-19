package com.github.brickwall2900.birthdays;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class BirthdayObject {
    public String name;
    public boolean enabled;
    public LocalDate date;

    public String customMessage;

    public BirthdayObject() {
    }

    public BirthdayObject(String name, boolean enabled, LocalDate date, String customMessage) {
        this.name = name;
        this.enabled = enabled;
        this.date = date;
        this.customMessage = customMessage;
    }

    @Override
    public String toString() {
        return String.format("%s, at %s", name, date.format(DateTimeFormatter.ISO_LOCAL_DATE)) + (!enabled ? " <DISABLED>" : "");
    }
}
