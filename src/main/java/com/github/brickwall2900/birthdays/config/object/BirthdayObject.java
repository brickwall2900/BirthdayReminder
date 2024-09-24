package com.github.brickwall2900.birthdays.config.object;

import com.github.brickwall2900.birthdays.BirthdaysManager;
import com.github.brickwall2900.birthdays.config.BirthdayNotifierConfig;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static com.github.brickwall2900.birthdays.TranslatableText.text;

public class BirthdayObject {
    public static final DateTimeFormatter DATE_TO_STING_FORMATTER = DateTimeFormatter.ofPattern("MMMM dd, yyyy");
    public String name;
    public boolean enabled;
    public LocalDate date;

    public String customMessage;

    public BirthdayNotifierConfig.Config override;

    public BirthdayObject() {
    }

    public BirthdayObject(String name, boolean enabled, LocalDate date, String customMessage) {
        this.name = name;
        this.enabled = enabled;
        this.date = date;
        this.customMessage = customMessage;
    }

    public BirthdayObject(String name, boolean enabled, LocalDate date, String customMessage, BirthdayNotifierConfig.Config override) {
        this.name = name;
        this.enabled = enabled;
        this.date = date;
        this.customMessage = customMessage;
        this.override = override;
    }

    @Override
    public String toString() {
        return String.format(text("editor.object"),
                name,
                date.format(DATE_TO_STING_FORMATTER),
                BirthdaysManager.getDaysBeforeBirthday(this),
                BirthdaysManager.getAgeInYears(this)) + (!enabled ? " <DISABLED>" : "");
    }
}
