package com.github.brickwall2900.birthdays.config.object;

import com.github.brickwall2900.birthdays.BirthdaysManager;
import com.github.brickwall2900.birthdays.config.BirthdayNotifierConfig;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;


public class BirthdayObject {
    public static final DateTimeFormatter DATE_TO_STING_FORMATTER = DateTimeFormatter.ofPattern("MMMM dd, yyyy");
    public final String name;
    public final boolean enabled;
    public final LocalDate date;

    public final String customMessage;

    public final BirthdayNotifierConfig override;

    public BirthdayObject() {
        this.name = "???";
        this.enabled = true;
        this.date = LocalDate.now();
        this.customMessage = null;
        this.override = null;
    }

    public BirthdayObject(String name, boolean enabled, LocalDate date, String customMessage) {
        this.name = name;
        this.enabled = enabled;
        this.date = date;
        this.customMessage = customMessage;
        this.override = null;
    }

    public BirthdayObject(String name, boolean enabled, LocalDate date, String customMessage, BirthdayNotifierConfig override) {
        this.name = name;
        this.enabled = enabled;
        this.date = date;
        this.customMessage = customMessage;
        this.override = override;
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
