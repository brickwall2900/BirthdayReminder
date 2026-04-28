package com.github.brickwall2900.birthdays.systray.awt;

import com.github.brickwall2900.birthdays.systray.TrayMenuItem;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

public class AWTTrayMenuItem implements TrayMenuItem {
    private final List<Runnable> callbacks = new ArrayList<>();
    protected final MenuItem menuItem;

    public AWTTrayMenuItem(MenuItem menuItem) {
        this.menuItem = menuItem;
        this.menuItem.addActionListener(this::onActionPerformed);
    }

    public AWTTrayMenuItem() {
        this(new MenuItem());
    }

    protected void fireCallbacks() {
        callbacks.forEach(Runnable::run);
    }

    protected void onActionPerformed(ActionEvent e) {
        fireCallbacks();
    }

    @Override
    public String getText() {
        return menuItem.getLabel();
    }

    @Override
    public void setText(String text) {
        menuItem.setLabel(text);
    }

    @Override
    public int getMnemonic() {
        return menuItem.getShortcut().getKey();
    }

    @Override
    public void setMnemonic(int mnemonic) {
        menuItem.setShortcut(new MenuShortcut(mnemonic));
    }

    @Override
    public void addCallback(Runnable onSelected) {
        callbacks.add(onSelected);
    }

    @Override
    public void removeCallback(Runnable onSelected) {
        callbacks.remove(onSelected);
    }
}
