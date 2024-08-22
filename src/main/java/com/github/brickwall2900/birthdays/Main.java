package com.github.brickwall2900.birthdays;

import com.formdev.flatlaf.FlatLightLaf;
import com.github.brickwall2900.birthdays.gui.BirthdayEditDialogBox;
import com.github.brickwall2900.birthdays.gui.BirthdayEditorGui;
import com.github.brickwall2900.birthdays.gui.BirthdayNotifyGui;

import java.awt.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static com.github.brickwall2900.birthdays.TranslatableText.text;

public class Main {
    public static void main(String[] args) throws IOException {
        FlatLightLaf.setup();

        boolean edit = false;
        for (String s : args) {
            if (s.equals("-edit")) {
                edit = true;
                break;
            }
        }

        if (edit) {
            BirthdayEditorGui editorGui = new BirthdayEditorGui(BirthdaysManager.getAllBirthdays());
            editorGui.setVisible(true);

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    BirthdaysConfig.BIRTHDAY_LIST.clear();
                    BirthdaysConfig.BIRTHDAY_LIST.addAll(List.of(editorGui.getBirthdays()));
                    BirthdaysConfig.save();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }));
        }

        BirthdayObject[] oneDayBefore = BirthdaysManager.getBirthdaysOffset(-1);
        BirthdayObject[] birthdaysToday = BirthdaysManager.getBirthdaysToday();

        for (BirthdayObject object : oneDayBefore) {
            notifyBirthdayOneDayBefore(object);
        }

        for (BirthdayObject object : birthdaysToday) {
            if (object.enabled) {
                BirthdayNotifyGui notifyGui = new BirthdayNotifyGui(object);
                notifyGui.setVisible(true);
            }
        }
    }

    private static TrayIcon trayIcon;

    public static void notifyBirthdayOneDayBefore(BirthdayObject birthday) {
        if (!birthday.enabled) return;
        if (trayIcon == null) {
            SystemTray systemTray = SystemTray.getSystemTray();
            trayIcon = new TrayIcon(BirthdayNotifyGui.IMAGE_ICON);
            try {
                systemTray.add(trayIcon);
            } catch (AWTException e) {
                throw new RuntimeException("Cannot add tray icon!");
            }
        }

        trayIcon.displayMessage(text("notify.before.caption"), text("notify.before.text", birthday.name), TrayIcon.MessageType.INFO);
    }
}