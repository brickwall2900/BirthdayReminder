package com.github.brickwall2900.birthdays.config;

import java.util.Objects;

public class BirthdayNotifierConfig {
    public int daysBeforeReminder = 1;
    public String birthdaySoundPath;

    public BirthdayNotifierConfig(int daysBeforeReminder, String birthdaySoundPath) {
        this.daysBeforeReminder = daysBeforeReminder;
        this.birthdaySoundPath = !birthdaySoundPath.isBlank() ? birthdaySoundPath : null;
    }

    public BirthdayNotifierConfig() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BirthdayNotifierConfig config)) return false;
        return daysBeforeReminder == config.daysBeforeReminder && Objects.equals(birthdaySoundPath, config.birthdaySoundPath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(daysBeforeReminder, birthdaySoundPath);
    }

    @Override
    public String toString() {
        return "Config{" +
                "daysBeforeReminder=" + daysBeforeReminder +
                ", birthdaySoundPath=" + birthdaySoundPath +
                '}';
    }
}
