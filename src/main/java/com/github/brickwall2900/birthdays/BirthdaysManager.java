package com.github.brickwall2900.birthdays;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public class BirthdaysManager {
    static {
        try {
            BirthdaysConfig.load();
        } catch (IOException e) {
            throw new RuntimeException("Cannot load birthdays!", e);
        }
    }

    public static BirthdayObject[] getBirthdaysToday() {
        List<BirthdayObject> birthdayList = BirthdaysConfig.BIRTHDAY_LIST;
        LocalDate today = LocalDate.now();
        List<BirthdayObject> birthdaysToday = birthdayList.stream()
                .filter(obj -> isMonthAndDayMatching(obj.date, today))
                .toList();
        return birthdaysToday.toArray(new BirthdayObject[0]);
    }

    public static boolean isMonthAndDayMatching(LocalDate date, LocalDate today) {
        return date.getMonth() == today.getMonth() && date.getDayOfMonth() == today.getDayOfMonth();
    }
}
