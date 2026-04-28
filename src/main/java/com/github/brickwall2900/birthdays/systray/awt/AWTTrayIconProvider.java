package com.github.brickwall2900.birthdays.systray.awt;

import com.github.brickwall2900.birthdays.systray.*;
import com.github.brickwall2900.birthdays.systray.TrayIcon;

import java.awt.*;

public final class AWTTrayIconProvider implements TrayIconProvider {
    @Override
    public TrayIcon create(String appId) {
        if (!SystemTray.isSupported()) {
            throw new UnsupportedOperationException("System tray isn't supported");
        }
        return new AWTTrayIcon();
    }

    @Override
    public TrayMenu createMenu() {
        return new AWTTrayMenu();
    }

    @Override
    public TrayMenuItem createMenuItem() {
        return new AWTTrayMenuItem();
    }

    @Override
    public TrayCheckboxMenuItem createCheckboxMenuItem() {
        return new AWTTrayCheckboxMenuItem();
    }
}
