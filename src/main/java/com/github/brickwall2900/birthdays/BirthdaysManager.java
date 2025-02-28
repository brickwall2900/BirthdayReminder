package com.github.brickwall2900.birthdays;

import com.github.brickwall2900.birthdays.config.BirthdayNotifierConfig;
import com.github.brickwall2900.birthdays.config.object.BirthdayObject;
import com.github.brickwall2900.birthdays.config.object.BirthdaysConfig;

import java.io.IOException;
import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class BirthdaysManager {
    public static void loadEverything() {
        try {
            BirthdayNotifierConfig.loadGlobalConfig();
            BirthdayNotifierConfig.loadApplicationConfig();
            BirthdaysConfig.load();
        } catch (IOException e) {
            throw new RuntimeException("Cannot load birthdays!", e);
        }
    }

    public static BirthdayObject[] getBirthdaysToday() {
        return getBirthdaysOffset(0);
    }

    public static BirthdayObject[] getBirthdaysSince(LocalDate date) {
        List<BirthdayObject> birthdayList = BirthdaysConfig.BIRTHDAY_LIST;
        List<BirthdayObject> birthdaysToday = birthdayList.stream()
                .filter(obj -> obj.enabled && isBirthdaySince(obj, date))
                .toList();
        return birthdaysToday.toArray(new BirthdayObject[0]);
    }

    public static BirthdayObject[] getBirthdaysOffset(int days) {
        List<BirthdayObject> birthdayList = BirthdaysConfig.BIRTHDAY_LIST;
        LocalDate day = LocalDate.now().plusDays(days);
        List<BirthdayObject> birthdaysToday = birthdayList.stream()
                .filter(obj -> obj.enabled && isMonthAndDayMatching(obj.date, day))
                .toList();
        return birthdaysToday.toArray(new BirthdayObject[0]);
    }

    public static boolean shouldRemind(BirthdayObject birthday) {
        int daysBeforeReminder = birthday.override != null
                ? birthday.override.daysBeforeReminder
                : BirthdayNotifierConfig.globalConfig.daysBeforeReminder;

        // Calculate days until the next birthday
        long daysUntilBirthday = getDaysBeforeBirthday(birthday);

        // Determine if we should remind
        return daysUntilBirthday <= daysBeforeReminder;
    }

    public static long getDaysBeforeBirthday(BirthdayObject birthday) {
        LocalDate today = LocalDate.now();
        LocalDate birthdayThisYear = birthday.date.withYear(today.getYear());

        // Check if the birthday has already occurred this year
        LocalDate nextBirthday;
        if (birthdayThisYear.isBefore(today) || birthdayThisYear.isEqual(today)) {
            nextBirthday = birthdayThisYear.plusYears(1); // Next birthday is next year
        } else {
            nextBirthday = birthdayThisYear; // Next birthday is this year
        }

        return ChronoUnit.DAYS.between(today, nextBirthday);
    }

    public static boolean isBirthdayToday(BirthdayObject birthday) {
        LocalDate day = LocalDate.now();
        return isMonthAndDayMatching(birthday.date, day);
    }

    public static boolean isBirthdaySince(BirthdayObject birthday, LocalDate since) {
        LocalDate today = LocalDate.now();

        LocalDate birthdayThisYear = birthday.date.withYear(today.getYear());
        LocalDate birthdayLastYear = birthday.date.withYear(today.getYear() - 1);

        // Check if the birthday was between lastAlive and today
        if (birthdayThisYear.isAfter(since) && birthdayThisYear.isBefore(today.plusDays(1))) {
            return true;
        }

        // Check if lastAlive is in a different year and birthday last year was between
        return since.getYear() < today.getYear() &&
                birthdayLastYear.isAfter(since) && birthdayLastYear.isBefore(today.plusDays(1));
    }

    public static long getDaysSinceBirthday(BirthdayObject birthday) {
        LocalDate today = LocalDate.now();

        // Adjust the birthday to the most recent occurrence
        LocalDate lastBirthday = birthday.date.withYear(today.getYear());
        if (lastBirthday.isAfter(today)) {
            lastBirthday = lastBirthday.minusYears(1); // Use the birthday from the previous year
        }

        // Calculate days passed since the last birthday
        return ChronoUnit.DAYS.between(lastBirthday, today);
    }

    public static BirthdayObject[] getAllBirthdays() {
        return BirthdaysConfig.BIRTHDAY_LIST.toArray(new BirthdayObject[0]);
    }

    public static int getAgeInYears(BirthdayObject object) {
        return getAge(object).getYears();
    }

    public static Period getAge(BirthdayObject object) {
        return Period.between(object.date, LocalDate.now());
    }

    public static boolean isMonthAndDayMatching(LocalDate date, LocalDate today) {
        return date.getMonth() == today.getMonth() && date.getDayOfMonth() == today.getDayOfMonth();
    }
}
