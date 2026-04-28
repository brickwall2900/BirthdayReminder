package com.github.brickwall2900.birthdays.systray;

import java.awt.*;

public interface TrayIcon {
    void setImage(Image image);
    void setTooltip(String tooltip);
    void setTrayMenu(TrayMenu menu);

    void destroy();
}
