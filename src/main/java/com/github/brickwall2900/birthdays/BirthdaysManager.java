package com.github.brickwall2900.birthdays;

import com.github.brickwall2900.birthdays.config.BirthdayNotifierConfig;
import com.github.brickwall2900.birthdays.config.object.BirthdayObject;
import com.github.brickwall2900.birthdays.config.object.BirthdaysConfig;

import java.io.IOException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class BirthdaysManager {
    static {
        try {
            BirthdayNotifierConfig.load();
            BirthdaysConfig.load();
        } catch (IOException e) {
            throw new RuntimeException("Cannot load birthdays!", e);
        }
    }

    public static BirthdayObject[] getBirthdaysToday() {
        return getBirthdaysOffset(0);
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

    public static BirthdayObject[] getAllBirthdays() {
        return BirthdaysConfig.BIRTHDAY_LIST.toArray(new BirthdayObject[0]);
    }

    public static int getAgeInDays(BirthdayObject object) {
        return LocalDate.now().minusYears(object.date.getYear()).getYear();
    }

    public static boolean isMonthAndDayMatching(LocalDate date, LocalDate today) {
        return date.getMonth() == today.getMonth() && date.getDayOfMonth() == today.getDayOfMonth();
    }
}
