package com.github.brickwall2900.birthdays;

import java.time.LocalDate;

public class BirthdayObject {
    public String name;
    public boolean enabled;
    public LocalDate date;

    public String customMessage;

    @Override
    public String toString() {
        return "BirthdayObject{" +
                "name='" + name + '\'' +
                ", enabled=" + enabled +
                ", date=" + date +
                ", customMessage='" + customMessage + '\'' +
                '}';
    }
}
