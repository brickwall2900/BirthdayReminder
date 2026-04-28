package com.github.brickwall2900.birthdays.systray;

public interface TrayMenuItem {
    String getText();
    void setText(String text);

    int getMnemonic();
    void setMnemonic(int mnemonic);

    void addCallback(Runnable onSelected);
    void removeCallback(Runnable onSelected);
}
