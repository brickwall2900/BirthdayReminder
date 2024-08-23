package com.github.brickwall2900.birthdays;

import com.formdev.flatlaf.FlatLightLaf;
import com.github.brickwall2900.birthdays.config.BirthdayNotifierConfig;
import com.github.brickwall2900.birthdays.config.object.BirthdayObject;
import com.github.brickwall2900.birthdays.config.object.BirthdaysConfig;
import com.github.brickwall2900.birthdays.gui.BirthdayEditorGui;
import com.github.brickwall2900.birthdays.gui.BirthdayNotifyGui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.time.LocalDate;
import java.util.List;

import static com.github.brickwall2900.birthdays.TranslatableText.text;

public class Main {
    public static void main(String[] args)  {
        SwingUtilities.invokeLater(Main::swingContext);
    }

    private static BirthdayEditorGui editorGui;
    private static TrayIcon trayIcon;
    private static void swingContext() {
        FlatLightLaf.setup();

        editorGui = new BirthdayEditorGui(BirthdaysManager.getAllBirthdays());
        Runtime.getRuntime().addShutdownHook(new Thread(Main::save));

        buildTrayIcon();

        Timer updater = new Timer(1000, Main::tickUpdate);
        updater.setRepeats(true);
        updater.start();

        performChecks();
    }

    public static void buildTrayIcon() {
        SystemTray systemTray = SystemTray.getSystemTray();
        trayIcon = new TrayIcon(BirthdayNotifyGui.IMAGE_ICON);
        trayIcon.setImageAutoSize(true);

        PopupMenu popupMenu = new PopupMenu();
        MenuItem openItem = new MenuItem(text("popup.open"), new MenuShortcut(KeyEvent.VK_O));
        MenuItem exitItem = new MenuItem(text("popup.exit"), new MenuShortcut(KeyEvent.VK_E));
        openItem.addActionListener(e -> editorGui.setVisible(true));
        exitItem.addActionListener(e -> System.exit(0));
        popupMenu.add(openItem);
        popupMenu.add(exitItem);
        trayIcon.setPopupMenu(popupMenu);

        try {
            systemTray.add(trayIcon);
        } catch (AWTException e) {
            throw new RuntimeException("Cannot add tray icon!");
        }
    }

    public static void save() {
        try {
            BirthdaysConfig.BIRTHDAY_LIST.clear();
            BirthdaysConfig.BIRTHDAY_LIST.addAll(List.of(editorGui.getBirthdays()));
            BirthdaysConfig.save();
            BirthdayNotifierConfig.save();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, text("errors.cannotSave", e), text("errors.title"), JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private static LocalDate today = LocalDate.now();
    public static void tickUpdate(ActionEvent e) {
        LocalDate today = LocalDate.now();
        if (!Main.today.equals(today)) { // day has changed, go update lol
            Main.today = today;
            performChecks();
        }
    }

    public static void performChecks() {
        BirthdayObject[] birthdaysToday = BirthdaysManager.getBirthdaysToday();
        BirthdayObject[] allBirthdays = BirthdaysManager.getAllBirthdays();
        // notify them
        for (BirthdayObject birthday : birthdaysToday) {
            BirthdayNotifyGui notifyGui = new BirthdayNotifyGui(birthday);
            notifyGui.open();
        }

        // remind them
        for (BirthdayObject birthday : allBirthdays) {
            if (BirthdaysManager.shouldRemind(birthday) && !BirthdaysManager.isBirthdayToday(birthday)) {
                notifyBirthday(birthday);
            }
        }
    }

    public static void notifyBirthday(BirthdayObject birthday) {
        if (!birthday.enabled) return;

        long daysBeforeBirthday = BirthdaysManager.getDaysBeforeBirthday(birthday);
        String text = daysBeforeBirthday > 1
                ? text("notify.before.text.more", birthday.name, daysBeforeBirthday)
                : text("notify.before.text.tomorrow", birthday.name);
        trayIcon.displayMessage(text("notify.before.caption"), text, TrayIcon.MessageType.INFO);
    }
}