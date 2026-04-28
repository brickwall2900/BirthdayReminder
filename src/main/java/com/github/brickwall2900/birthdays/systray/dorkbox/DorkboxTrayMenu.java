package com.github.brickwall2900.birthdays.systray.dorkbox;

import com.github.brickwall2900.birthdays.systray.TrayMenu;
import com.github.brickwall2900.birthdays.systray.TrayMenuItem;

import javax.swing.*;

public class DorkboxTrayMenu implements TrayMenu {
    final JMenu menu;

    public DorkboxTrayMenu() {
        menu = new JMenu();
    }

    @Override
    public void add(TrayMenuItem item) {
        menu.add(((DorkboxTrayMenuItem) item).menuItem);
    }

    @Override
    public void remove(TrayMenuItem item) {
        menu.remove(((DorkboxTrayMenuItem) item).menuItem);
    }

    @Override
    public void addSeparator() {
        menu.addSeparator();
    }
}
