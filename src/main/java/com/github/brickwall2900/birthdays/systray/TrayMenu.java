package com.github.brickwall2900.birthdays.systray;

public interface TrayMenu {
    void add(TrayMenuItem item);
    void remove(TrayMenuItem item);

    void addSeparator();
}
