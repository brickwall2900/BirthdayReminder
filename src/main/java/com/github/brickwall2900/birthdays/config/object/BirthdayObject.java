package com.github.brickwall2900.birthdays.config.object;

import com.github.brickwall2900.birthdays.BirthdaysManager;
import com.github.brickwall2900.birthdays.config.BirthdayNotifierConfig;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public record BirthdayObject(String name, boolean enabled, LocalDate date, String customMessage,
                             BirthdayNotifierConfig override) {
    public static final DateTimeFormatter DATE_TO_STING_FORMATTER = DateTimeFormatter.ofPattern("MMMM dd, yyyy");

    public BirthdayObject() {
        this("???", true, LocalDate.now(), null, null);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        BirthdayObject that = (BirthdayObject) o;
        return enabled == that.enabled && Objects.equals(name, that.name) && Objects.equals(date, that.date) && Objects.equals(customMessage, that.customMessage) && Objects.equals(override, that.override);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, enabled, date, customMessage, override);
    }

    @Override
    public String toString() {
        return String.format("%s, at %s; %d days left, %d y/o",
                name,
                date.format(DATE_TO_STING_FORMATTER),
                BirthdaysManager.getDaysBeforeBirthday(this),
                BirthdaysManager.getAgeInYears(this)) + (!enabled ? " <DISABLED>" : "");
    }
}
