package com.github.brickwall2900.birthdays;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.github.brickwall2900.birthdays.config.BirthdayNotifierConfig;
import com.github.brickwall2900.birthdays.config.object.BirthdayObject;
import com.github.brickwall2900.birthdays.config.object.BirthdaysConfig;
import com.github.brickwall2900.birthdays.gui.BirthdayListEditorGui;

import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import static com.github.brickwall2900.birthdays.TranslatableText.getArray;
import static com.github.brickwall2900.birthdays.TranslatableText.text;

public class Main {
    private static final String UNIQUE_APP_ID = "PlayerScripts_BirthdayManager0001";

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::swingContext);
    }

    private static BirthdayListEditorGui editorGui;
    private static TrayIcon trayIcon;
    private static InstanceLock lock;
    private static Timer lockTimer;

    private static void swingContext() {
        if (Boolean.getBoolean("dark.mode")) {
            FlatDarkLaf.setup();
        } else {
            FlatLightLaf.setup();
        }

        lock = new InstanceLock(UNIQUE_APP_ID);
        if (!lock.lock()) {
            // maybe we use another title other than editor.title?
            JOptionPane.showMessageDialog(null, text("errors.instanceAlreadyRunning"), text("editor.title"), JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }

        editorGui = new BirthdayListEditorGui(BirthdaysManager.getAllBirthdays());
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            save();
            lock.unlock();
        }));

        buildTrayIcon();

        Timer updater = new Timer(1000, Main::tickUpdate);
        updater.setRepeats(true);
        updater.start();

        Timer lockTheDamnInstance = new Timer(500, Main::tryLockingTheDamnInstance);
        lockTheDamnInstance.setRepeats(true);
        lockTheDamnInstance.start();
        lockTimer = lockTheDamnInstance;

        performChecks();
    }

    public static void buildTrayIcon() {
        SystemTray systemTray = SystemTray.getSystemTray();
        trayIcon = new TrayIcon(IMAGE_ICON);
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

    private static void tryLockingTheDamnInstance(ActionEvent e) {
        if (lock.isLocked()) {
            lock.lock();
        }
        lockTimer.stop();
    }

    public static void performChecks() {
        BirthdayObject[] birthdaysToday = BirthdaysManager.getBirthdaysToday();
        BirthdayObject[] allBirthdays = BirthdaysManager.getAllBirthdays();
        // notify them
        for (BirthdayObject birthday : birthdaysToday) {
            notifyBirthday(birthday);
        }

        // remind them
        for (BirthdayObject birthday : allBirthdays) {
            if (BirthdaysManager.shouldRemind(birthday) && !BirthdaysManager.isBirthdayToday(birthday)) {
                remindBirthday(birthday);
            }
        }
    }

    public static final Image IMAGE_ICON;
    public static final ImageIcon ICON;

    public static final String[] MESSAGES = Objects.requireNonNull(getArray("messages"), "Messages are missing??");

    static {
        try {
            IMAGE_ICON = ImageIO.read(Objects.requireNonNull(Main.class.getResourceAsStream("/icon.png")));
            ICON = new ImageIcon(IMAGE_ICON.getScaledInstance(32, 32, Image.SCALE_DEFAULT));
        } catch (IOException | NullPointerException e) {
            throw new RuntimeException("Cannot read birthday icon!");
        }
    }

    public static void notifyBirthday(BirthdayObject birthday) {
        Clip clip = playSound(birthday);
        String labelContent = text("notify.content",
                birthday.name,
                BirthdaysManager.getAgeInDays(birthday),
                birthday.customMessage != null
                        ? birthday.customMessage
                        : MESSAGES[(int) (Math.random() * MESSAGES.length)]);
        String title = text("notify.title", birthday.name);
        JOptionPane optionPane = new JOptionPane(labelContent, JOptionPane.PLAIN_MESSAGE, JOptionPane.DEFAULT_OPTION, ICON);
        JDialog dialog = optionPane.createDialog(title);
        dialog.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setIconImage(IMAGE_ICON);
        dialog.setAlwaysOnTop(true);
        dialog.setVisible(true);
        dialog.requestFocus();
        // wait for user
        if (clip != null) {
            clip.stop();
            clip.close();
        }
    }

    private static Clip playSound(BirthdayObject birthday) {
        String soundLocation = birthday.override != null
                ? birthday.override.birthdaySoundPath
                : BirthdayNotifierConfig.globalConfig.birthdaySoundPath;

        if (soundLocation != null && !soundLocation.isBlank()) {
            Path soundPath = Paths.get(soundLocation);
            try {
                DataLine.Info info = new DataLine.Info(Clip.class, null);
                Clip clip = (Clip) AudioSystem.getLine(info);
                clip.open(AudioSystem.getAudioInputStream(soundPath.toFile()));
                clip.loop(Clip.LOOP_CONTINUOUSLY);
                return clip;
            } catch (LineUnavailableException | IOException | UnsupportedAudioFileException e) {
                System.err.println("Cannot open a line for " + soundLocation);
                e.printStackTrace();
            }
        }
        return null;
    }

    public static void remindBirthday(BirthdayObject birthday) {
        if (!birthday.enabled) return;

        long daysBeforeBirthday = BirthdaysManager.getDaysBeforeBirthday(birthday);
        String text = daysBeforeBirthday > 1
                ? text("notify.before.text.more", birthday.name, daysBeforeBirthday)
                : text("notify.before.text.tomorrow", birthday.name);
        trayIcon.displayMessage(text("notify.before.caption"), text, TrayIcon.MessageType.INFO);
    }
}