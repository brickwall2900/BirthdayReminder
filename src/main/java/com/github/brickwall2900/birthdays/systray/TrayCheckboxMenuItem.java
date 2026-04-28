package com.github.brickwall2900.birthdays.systray;

public interface TrayCheckboxMenuItem extends TrayMenuItem {
    boolean isChecked();
    void setChecked(boolean state);
}
