package com.github.brickwall2900.birthdays.systray.awt;

import com.github.brickwall2900.birthdays.systray.TrayCheckboxMenuItem;

import java.awt.*;

public class AWTTrayCheckboxMenuItem extends AWTTrayMenuItem implements TrayCheckboxMenuItem {
    public AWTTrayCheckboxMenuItem() {
        super(new CheckboxMenuItem());

        addCallback(this::onItemChecked);
    }

    private void onItemChecked() {
        fireCallbacks();
    }

    @Override
    public boolean isChecked() {
        return ((CheckboxMenuItem) menuItem).getState();
    }

    @Override
    public void setChecked(boolean state) {
        ((CheckboxMenuItem) menuItem).setState(state);
    }
}
