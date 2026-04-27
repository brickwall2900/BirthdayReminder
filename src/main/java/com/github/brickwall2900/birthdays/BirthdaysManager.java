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
            // yo wtf
            // sure everything is in global state
            BirthdayNotifierConfig.loadGlobalConfig();
            BirthdayNotifierConfig.loadApplicationConfig();
            BirthdaysConfig.load();
        } catch (IOException e) {
            throw new IllegalStateException("Cannot load birthdays!", e);
        }
    }

    public static BirthdayObject[] getBirthdaysToday() {
        return getBirthdaysOffset(0);
    }

    public static BirthdayObject[] getBirthdaysSince(LocalDate date) {
        List<BirthdayObject> birthdayList = BirthdaysConfig.BIRTHDAY_LIST;
        List<BirthdayObject> birthdaysToday = birthdayList.stream()
                .filter(obj -> obj.enabled)
                .filter(obj -> isBirthdaySince(obj, date))
                .toList();
        return birthdaysToday.toArray(BirthdayObject[]::new);
    }

    public static BirthdayObject[] getBirthdaysOffset(int days) {
        List<BirthdayObject> birthdayList = BirthdaysConfig.BIRTHDAY_LIST;
        LocalDate day = LocalDate.now().plusDays(days);
        List<BirthdayObject> birthdaysToday = birthdayList.stream()
                .filter(obj -> obj.enabled)
                .filter(obj -> isMonthAndDayMatching(obj.date, day))
                .toList();
        return birthdaysToday.toArray(BirthdayObject[]::new);
    }

    public static boolean shouldRemind(BirthdayObject birthday) {
        int daysBeforeReminder = birthday.override != null
                ? birthday.override.daysBeforeReminder
                : BirthdayNotifierConfig.globalConfig.daysBeforeReminder;

        // Calculate days until the next birthday
        long daysBeforeBirthday = getDaysBeforeBirthday(birthday);

        // Determine if we should remind
        return daysBeforeReminder != 0 && daysBeforeBirthday <= daysBeforeReminder;
    }

    public static long getDaysBeforeBirthday(BirthdayObject birthday) {
        LocalDate today = LocalDate.now();
        LocalDate birthdayThisYear = birthday.date.withYear(today.getYear());

        // Check if the birthday has already occurred this year
        LocalDate nextBirthday;
        if (birthdayThisYear.isBefore(today) || birthdayThisYear.isEqual(today)) {
            // Next birthday is next year so we increment it by one
            nextBirthday = birthdayThisYear.plusYears(1);
        } else {
            // Next birthday is this year!!
            nextBirthday = birthdayThisYear;
        }

        // and yes just count the days
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
        // birthdayThisYear < since AND birthdayThisYear > tomorrow
        // is birthdayThisYear before tomorrow AND is birthdayThisYear after the given date?
        if (birthdayThisYear.isAfter(since) && birthdayThisYear.isBefore(today.plusDays(1))) {
            return true;
        }

        // Check if lastAlive is in a different year and birthday last year was between
        // what the fuck

        // check if that year is older than today's year
        // AND
        // check if birthday last year is after the given date
        // AND
        // check if the birthday last year is before tomorrow (okay wait what the actual fuck)
        return since.getYear() < today.getYear() &&
                birthdayLastYear.isAfter(since) && birthdayLastYear.isBefore(today.plusDays(1));
    }

    public static long getDaysSinceBirthday(BirthdayObject birthday) {
        LocalDate today = LocalDate.now();

        // Adjust the birthday to the most recent occurrence
        // okay so that means put it to this year's birthday
        LocalDate lastBirthday = birthday.date.withYear(today.getYear());

        // is the birthday this year after today? did it pass?
        if (lastBirthday.isAfter(today)) {
            // then use the birthday from the previous year
            lastBirthday = lastBirthday.minusYears(1);
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
