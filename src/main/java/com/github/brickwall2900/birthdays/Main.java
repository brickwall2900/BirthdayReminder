package com.github.brickwall2900.birthdays;

import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.github.brickwall2900.birthdays.config.BirthdayNotifierConfig;
import com.github.brickwall2900.birthdays.config.object.BirthdayObject;
import com.github.brickwall2900.birthdays.config.object.BirthdaysConfig;
import com.github.brickwall2900.birthdays.gui.BirthdayListEditorGui;
import dorkbox.systemTray.SystemTray;

import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
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
    private static SystemTray systemTray;
    private static InstanceLock lock;

    private static void swingContext() {
        Thread.currentThread().setPriority(Thread.NORM_PRIORITY - 2);
        BirthdaysManager.loadEverything();
        if (BirthdayNotifierConfig.applicationConfig.darkMode) {
            FlatDarculaLaf.setup();
        } else {
            FlatLightLaf.setup();
        }

        lock = new InstanceLock(UNIQUE_APP_ID);
        if (!lock.lock()) {
            JOptionPane.showMessageDialog(null, text("errors.instanceAlreadyRunning"), text("trayIcon.title"), JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }

        editorGui = new BirthdayListEditorGui(BirthdaysManager.getAllBirthdays());
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            save();
            editorGui = null;
            lock.unlock();
            if (systemTray != null) {
                systemTray.shutdown();
            }
        }));

        buildTrayIcon();

        Timer updater = new Timer(2000, Main::tickUpdate);
        updater.setRepeats(true);
        updater.start();

        performChecks();
        BirthdayNotifierConfig.applicationConfig.lastAlive = today;
    }

    public static void buildTrayIcon() {
        systemTray = SystemTray.get();
        systemTray.setImage(IMAGE_ICON);
        systemTray.setTooltip(text("trayIcon.title"));

        JMenu popupMenu = new JMenu();
        JMenuItem openItem = new JMenuItem(text("popup.open"), KeyEvent.VK_O);
        JMenuItem exitItem = new JMenuItem(text("popup.exit"), KeyEvent.VK_E);
        openItem.addActionListener(e -> SwingUtilities.invokeLater(Main::openEditorGui));
        exitItem.addActionListener(e -> SwingUtilities.invokeLater(() -> System.exit(0)));
        popupMenu.add(openItem);
        popupMenu.add(exitItem);
        systemTray.setMenu(popupMenu);
    }

    private static void openEditorGui() {
        if (editorGui == null) {
            editorGui = new BirthdayListEditorGui(BirthdaysManager.getAllBirthdays());
        }
        editorGui.setVisible(true);
    }

    public static void save() {
        try {
            BirthdaysConfig.BIRTHDAY_LIST.clear();
            BirthdaysConfig.BIRTHDAY_LIST.addAll(List.of(editorGui.getBirthdays()));
            BirthdaysConfig.save();
            BirthdayNotifierConfig.saveGlobalConfig();
            BirthdayNotifierConfig.saveApplicationConfig();

            SwingUtilities.invokeLater(() -> {
                editorGui.destroy();
                editorGui = null;
            });
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, text("errors.cannotSave", e), text("errors.title"), JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
        System.gc();
    }

    private static LocalDate today = LocalDate.now();

    public static void tickUpdate(ActionEvent e) {
        LocalDate today = LocalDate.now();
        if (!Main.today.equals(today)) { // day has changed, go update lol
            performChecks();
            BirthdayNotifierConfig.applicationConfig.lastAlive = today;
            Main.today = today;
        }
    }

    public static void performChecks() {
        LocalDate lastAlive = BirthdayNotifierConfig.applicationConfig.lastAlive;
        BirthdayObject[] birthdaysToday = BirthdaysManager.getBirthdaysSince(lastAlive);
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
        long daysApart = BirthdaysManager.getDaysSinceBirthday(birthday);
        Clip clip = playSound(birthday);
        String labelContent;
        System.out.println(daysApart);
        if (daysApart == 0) {
            labelContent = text("notify.content",
                    birthday.name,
                    BirthdaysManager.getAgeInYears(birthday),
                    birthday.customMessage != null
                            ? birthday.customMessage
                            : MESSAGES[(int) (Math.random() * MESSAGES.length)]);
        } else if (daysApart == 1) {
            labelContent = text("notify.late.content.yesterday",
                    birthday.name,
                    BirthdaysManager.getAgeInYears(birthday));
        } else {
            labelContent = text("notify.late.content.more",
                    birthday.name,
                    daysApart,
                    BirthdaysManager.getAgeInYears(birthday));
        }
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
        dialog.dispose();
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
                Toolkit.getDefaultToolkit().beep();
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
        displayMessage(text("notify.before.caption"), text);
    }

    private static void displayMessage(String caption, String text) {
        JOptionPane optionPane = new JOptionPane(text, JOptionPane.PLAIN_MESSAGE, JOptionPane.DEFAULT_OPTION, ICON);
        JDialog dialog = optionPane.createDialog(caption);
        dialog.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setIconImage(IMAGE_ICON);
        dialog.setAlwaysOnTop(true);
        dialog.setVisible(true);
        dialog.requestFocus();
    }

    public static void destroyContainer(Container container) {
        if (container == null) return;

        removeComponentListeners(container);
        for (Component component : container.getComponents()) {
            removeComponentListeners(component);
            if (component instanceof Container) {
                destroyContainer((Container) component);
            }
        }
    }

    private static void removeComponentListeners(Component component) {
        if (component instanceof AbstractButton btn) {
            for (ActionListener al : btn.getActionListeners()) {
                btn.removeActionListener(al);
            }
        }
        if (component instanceof Container c) {
            for (ContainerListener cl : c.getContainerListeners()) {
                c.removeContainerListener(cl);
            }
        }
        for (MouseListener ml : component.getMouseListeners()) {
            component.removeMouseListener(ml);
        }
        for (KeyListener kl : component.getKeyListeners()) {
            component.removeKeyListener(kl);
        }
        for (FocusListener fl : component.getFocusListeners()) {
            component.removeFocusListener(fl);
        }
        for (ComponentListener cl : component.getComponentListeners()) {
            component.removeComponentListener(cl);
        }
        for (HierarchyListener hl : component.getHierarchyListeners()) {
            component.removeHierarchyListener(hl);
        }
        for (InputMethodListener il : component.getInputMethodListeners()) {
            component.removeInputMethodListener(il);
        }
    }
}