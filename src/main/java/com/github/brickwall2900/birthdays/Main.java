package com.github.brickwall2900.birthdays;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.github.brickwall2900.birthdays.config.ConfigHolder;
import com.github.brickwall2900.birthdays.config.object.BirthdayObject;
import com.github.brickwall2900.birthdays.gui.BaseDialog;
import com.github.brickwall2900.birthdays.gui.BirthdayListEditorGui;
import dorkbox.systemTray.SystemTray;

import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;
import java.util.List;

public class Main {
    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(Main.class.getName());
    protected static final String UNIQUE_APP_ID = "PlayerScripts_BirthdayManager0001";

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::swingContext);
    }

    private static BirthdayListEditorGui editorGui;
    private static SystemTray systemTray;
    private static InstanceLock lock;
    private static String version;

    private static void swingContext() {
        Thread.currentThread().setPriority(Thread.NORM_PRIORITY - 2);
        BirthdaysManager.loadEverything();
        loadVersion();

        System.out.println("Version: " + version);
        setDarkMode(ConfigHolder.getApplicationConfig().darkMode);

        ToolTipManager toolTipManager = ToolTipManager.sharedInstance();
        toolTipManager.setDismissDelay(15 * 1000);

        lock = new InstanceLock(UNIQUE_APP_ID);
        if (!lock.lock()) {
            JOptionPane.showMessageDialog(null,
                    BUNDLE.getString("errors.instanceAlreadyRunning"),
                    BUNDLE.getString("trayIcon.title"),
                    JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }

        editorGui = new BirthdayListEditorGui(BirthdaysManager.getAllBirthdays());
        buildTrayIcon();

        Timer updater = new Timer(2000, Main::tickUpdate);
        updater.setRepeats(true);
        updater.start();

        performChecks();
        ConfigHolder.getApplicationConfig().lastAlive = today;
    }

    private static void loadVersion() {
        try (InputStream stream = Main.class.getResourceAsStream("/version.properties")) {
            Properties versionProperties = new Properties();
            versionProperties.load(stream);
            version = versionProperties.getProperty("app.version");
        } catch (IOException | NullPointerException e) {
            System.err.println("Version cannot be retrieved!");
            e.printStackTrace();
            version = "Unknown";
        }
    }

    private static void onShutdown() {
        save();
        editorGui = null;
        lock.unlock();
        if (systemTray != null) {
            systemTray.shutdown();
        }
        System.exit(0);
    }

    private static JMenu popupMenu;

    public static void buildTrayIcon() {
        systemTray = SystemTray.get(UNIQUE_APP_ID);
        systemTray.setImage(IMAGE_ICON);
        systemTray.setTooltip(BUNDLE.getString("trayIcon.title"));

        popupMenu = new JMenu();
        JMenuItem openItem = new JMenuItem(BUNDLE.getString("popup.open"), KeyEvent.VK_O);
        JMenuItem exitItem = new JMenuItem(BUNDLE.getString("popup.exit"), KeyEvent.VK_E);
        openItem.addActionListener(e -> SwingUtilities.invokeLater(Main::openEditorGui));
        exitItem.addActionListener(e -> SwingUtilities.invokeLater(Main::onShutdown));
        popupMenu.add(openItem);
        popupMenu.add(exitItem);
        systemTray.setMenu(popupMenu);
    }

    private static void openEditorGui() {
        if (systemTray != null) {
            systemTray.shutdown();
            systemTray = null;
            Main.destroyContainer(popupMenu);
            Main.removeComponentListeners(popupMenu);
        }
        if (editorGui == null) {
            editorGui = new BirthdayListEditorGui(BirthdaysManager.getAllBirthdays());
        }
        editorGui.setVisible(true);
    }

    public static void save() {
        try {
            if (editorGui != null) {
                ConfigHolder.setBirthdayList(List.of(editorGui.getBirthdays()));
            }
            ConfigHolder.saveBirthdays();
            ConfigHolder.saveGlobalConfig();
            ConfigHolder.saveApplicationConfig();

            SwingUtilities.invokeLater(() -> {
                editorGui.destroy();
                editorGui = null;
                buildTrayIcon();
            });
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    BUNDLE.getString("errors.cannotSave").formatted(e),
                    BUNDLE.getString("errors.title"),
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private static LocalDate today = LocalDate.now();

    public static void tickUpdate(ActionEvent e) {
        LocalDate today = LocalDate.now();
        if (!Main.today.equals(today)) { // day has changed, go update lol
            performChecks();
            ConfigHolder.getApplicationConfig().lastAlive = today;
            Main.today = today;
        }
    }

    public static void performChecks() {
        LocalDate lastAlive = ConfigHolder.getApplicationConfig().lastAlive;
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

    public static final String[] MESSAGES = Objects.requireNonNull(
            BUNDLE.getString("messages").split("\\|"),
            "Messages are missing??");

    static {
        try {
            IMAGE_ICON = ImageIO.read(Objects.requireNonNull(BaseDialog.class.getResourceAsStream("icon.png")));
            ICON = new ImageIcon(IMAGE_ICON.getScaledInstance(32, 32, Image.SCALE_DEFAULT));
        } catch (IOException | NullPointerException e) {
            throw new RuntimeException("Cannot read birthday icon!");
        }
    }

    public static void notifyBirthday(BirthdayObject birthday) {
        long daysApart = BirthdaysManager.getDaysSinceBirthday(birthday);
        Clip clip = playSound(birthday);
        String labelContent;
        if (daysApart == 0) {
            labelContent = BUNDLE.getString("notify.content").formatted(
                    birthday.name,
                    BirthdaysManager.getAgeInYears(birthday),
                    birthday.customMessage != null
                            ? birthday.customMessage
                            : MESSAGES[(int) (Math.random() * MESSAGES.length)]);
        } else if (daysApart == 1) {
            labelContent = BUNDLE.getString("notify.late.content.yesterday").formatted(
                    birthday.name,
                    BirthdaysManager.getAgeInYears(birthday));
        } else {
            labelContent = BUNDLE.getString("notify.late.content.more").formatted(
                    birthday.name,
                    daysApart,
                    BirthdaysManager.getAgeInYears(birthday));
        }
        String title = BUNDLE.getString("notify.title").formatted(birthday.name);
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
                : ConfigHolder.getNotifierConfig().birthdaySoundPath;

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
                ? BUNDLE.getString("notify.before.text.more").formatted(birthday.name, daysBeforeBirthday)
                : BUNDLE.getString("notify.before.text.tomorrow").formatted(birthday.name);
        displayMessage(BUNDLE.getString("notify.before.caption"), text);
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

    public static void setDarkMode(boolean toggle) {
        if (toggle) {
            FlatDarkLaf.setup();
        } else {
            FlatLightLaf.setup();
        }
        Arrays.stream(Window.getWindows()).forEach(SwingUtilities::updateComponentTreeUI);
        ConfigHolder.getApplicationConfig().darkMode = toggle;
    }

    // ANYTHING to reduce memory usage ;-;
    // i would lwky put the charlie kirk picture here if i want to
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

    public static String getVersion() {
        return version;
    }
}