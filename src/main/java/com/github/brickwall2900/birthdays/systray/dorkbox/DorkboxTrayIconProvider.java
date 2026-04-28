package com.github.brickwall2900.birthdays.systray.dorkbox;

import com.github.brickwall2900.birthdays.systray.*;

public final class DorkboxTrayIconProvider implements TrayIconProvider {
    public DorkboxTrayIconProvider() {
        // The SystemTray and its framework and libraries and whatnot has an auto-updater.
        // We disable it...
        System.setProperty("dorkbox.updates.Updates.ENABLE", "false");
        System.setProperty("dorkbox.Updates.ENABLE", "false");
    }

    @Override
    public TrayIcon create(String appId) {
        return new DorkboxTrayIcon(appId);
    }

    @Override
    public TrayMenu createMenu() {
        return new DorkboxTrayMenu();
    }

    @Override
    public TrayMenuItem createMenuItem() {
        return new DorkboxTrayMenuItem();
    }

    @Override
    public TrayCheckboxMenuItem createCheckboxMenuItem() {
        return new DorkboxTrayCheckboxMenuitem();
    }
}
