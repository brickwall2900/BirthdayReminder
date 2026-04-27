package com.github.brickwall2900.birthdays;

import com.github.brickwall2900.birthdays.config.ConfigHolder;
import com.github.brickwall2900.birthdays.config.object.BirthdayObject;

import java.io.IOException;
import java.time.LocalDate;
import java.time.MonthDay;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class BirthdaysManager {
    public static void loadEverything() {
        try {
            // yo wtf
            // sure everything is in global state
            ConfigHolder.loadGlobalConfig();
            ConfigHolder.loadApplicationConfig();
            ConfigHolder.loadBirthdays();
        } catch (IOException e) {
            throw new IllegalStateException("Cannot load birthdays!", e);
        }
    }

    public static BirthdayObject[] getBirthdaysToday() {
        return getBirthdaysOffset(0);
    }

    public static BirthdayObject[] getBirthdaysSince(LocalDate date) {
        List<BirthdayObject> birthdayList = ConfigHolder.BIRTHDAY_LIST;
        List<BirthdayObject> birthdaysToday = birthdayList.stream()
                .filter(obj -> obj.enabled)
                .filter(obj -> isBirthdaySince(obj, date))
                .toList();
        return birthdaysToday.toArray(BirthdayObject[]::new);
    }

    public static BirthdayObject[] getBirthdaysOffset(int days) {
        List<BirthdayObject> birthdayList = ConfigHolder.BIRTHDAY_LIST;
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
                : ConfigHolder.notifierConfig.daysBeforeReminder;

        // Calculate days until the next birthday
        long daysBeforeBirthday = getDaysBeforeBirthday(birthday);

        // Determine if we should remind
        return daysBeforeReminder != 0 && daysBeforeBirthday <= daysBeforeReminder;
    }

    public static long getDaysBeforeBirthday(BirthdayObject birthday) {
        LocalDate today = LocalDate.now();

        MonthDay birthMonthDay = MonthDay.from(birthday.date);
        int year = today.getYear();

        LocalDate birthdayThisYear = fixLeapYear(birthMonthDay, year);

        // Check if the birthday has already occurred this year
        LocalDate nextBirthday;
        if (birthdayThisYear.isBefore(today) || birthdayThisYear.isEqual(today)) {
            // Next birthday is next year so we increment it by one
            nextBirthday = fixLeapYear(birthMonthDay, year + 1);
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

    /// this determines if a person’s birthday has occurred within a specific window of time
    /// specifically, between a "start date" `since`, and today.
    public static boolean isBirthdaySince(BirthdayObject birthdayObject, LocalDate since) {
        LocalDate today = LocalDate.now();

        if (since.isAfter(today)) {
            // no birthday happened in the future
            // who are you? doctor strange?
            return false;
        }

        // Check if the birthday was between [since, today]
        // So we iterate through the years from 'since' to 'today'
        for (int year = since.getYear(); year <= today.getYear(); year++) {
            LocalDate occurrence = fixLeapYear(MonthDay.from(birthdayObject.date), year);

            // Check if this specific occurrence falls within the [since, today] range
            if (!occurrence.isBefore(since) && !occurrence.isAfter(today)) {
                return true;
            }
        }

        return false;
    }

    /// returns the number of days from today since the last birthday passed
    public static long getDaysSinceBirthday(BirthdayObject birthdayObject) {
        LocalDate today = LocalDate.now();
        MonthDay birthMonthDay = MonthDay.from(birthdayObject.date);

        // get this year's occurrence, adjusting for leap year if necessary
        int year = today.getYear();
        LocalDate lastBirthday = fixLeapYear(birthMonthDay, year);

        // is the birthday this year after today? did it pass?
        if (lastBirthday.isAfter(today)) {
            // then use the birthday from the previous year
            lastBirthday = fixLeapYear(birthMonthDay, year - 1);
        }

        // Calculate days passed since the last birthday
        return ChronoUnit.DAYS.between(lastBirthday, today);
    }

    /// returns a copy of the internal birthday list as an array
    public static BirthdayObject[] getAllBirthdays() {
        return ConfigHolder.BIRTHDAY_LIST.toArray(BirthdayObject[]::new);
    }

    public static LocalDate fixLeapYear(MonthDay monthDay /* DAMN i didn't know this exists */, int year) {
        return monthDay.isValidYear(year)
                ? monthDay.atYear(year)
                : LocalDate.of(year, 2, 28);
    }

    public static int getAgeInYears(BirthdayObject object) {
        return getAge(object).getYears();
    }

    public static Period getAge(BirthdayObject object) {
        return Period.between(object.date, LocalDate.now());
    }

    public static boolean isMonthAndDayMatching(LocalDate date, LocalDate today) {
        return fixLeapYear(MonthDay.from(date), today.getYear()).equals(today);
    }
}
