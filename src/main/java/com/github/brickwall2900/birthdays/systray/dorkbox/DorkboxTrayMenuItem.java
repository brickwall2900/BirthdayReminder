package com.github.brickwall2900.birthdays.systray.dorkbox;

import com.github.brickwall2900.birthdays.systray.TrayMenuItem;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

public class DorkboxTrayMenuItem implements TrayMenuItem {
    private final List<Runnable> callbacks = new ArrayList<>();
    protected final JMenuItem menuItem;

    public DorkboxTrayMenuItem(JMenuItem menuItem) {
        this.menuItem = menuItem;
        this.menuItem.addActionListener(this::onActionPerformed);
    }

    public DorkboxTrayMenuItem() {
        this(new JMenuItem());
    }

    private void onActionPerformed(ActionEvent e) {
        callbacks.forEach(Runnable::run);
    }

    @Override
    public String getText() {
        return menuItem.getText();
    }

    @Override
    public void setText(String text) {
        menuItem.setText(text);
    }

    @Override
    public int getMnemonic() {
        return menuItem.getMnemonic();
    }

    @Override
    public void setMnemonic(int mnemonic) {
        menuItem.setMnemonic(mnemonic);
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
