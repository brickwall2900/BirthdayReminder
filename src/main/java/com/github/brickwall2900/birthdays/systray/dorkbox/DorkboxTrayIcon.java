package com.github.brickwall2900.birthdays.systray.dorkbox;

import com.github.brickwall2900.birthdays.systray.TrayIcon;
import com.github.brickwall2900.birthdays.systray.TrayMenu;
import dorkbox.systemTray.SystemTray;

import java.awt.*;

public final class DorkboxTrayIcon implements TrayIcon {
    private final SystemTray systemTray;

    public DorkboxTrayIcon(String appId) {
        systemTray = SystemTray.get(appId);
    }

    @Override
    public void setImage(Image image) {
        systemTray.setImage(image);
    }

    @Override
    public void setTooltip(String tooltip) {
        systemTray.setTooltip(tooltip);
    }

    @Override
    public void setTrayMenu(TrayMenu menu) {
        systemTray.setMenu(((DorkboxTrayMenu) menu).menu);
    }

    @Override
    public void destroy() {
        /* TODO: oops, who wants to destroy all memory
           relating to the system tray? */
        systemTray.shutdown();
    }
}
