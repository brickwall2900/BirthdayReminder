package com.github.brickwall2900.birthdays.gui;

import com.github.brickwall2900.birthdays.Main;
import com.github.brickwall2900.birthdays.config.BirthdayNotifierConfig;
import org.httprpc.sierra.UILoader;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.Locale;

import static com.github.brickwall2900.birthdays.Main.IMAGE_ICON;
import static com.github.brickwall2900.birthdays.TranslatableText.BUNDLE;
import static com.github.brickwall2900.birthdays.TranslatableText.text;

public class BirthdayNotifierEditorGui extends JDialog {
    public static final String TITLE = text("notify.editor.dialog.title");
    public static final Dimension SIZE = new Dimension(400, 150);

    public BirthdayNotifierEditorGui(Window owner) {
        super(owner);

        setContentPane(UILoader.load(this, "/ui/birthdayNotifyEdit.xml", BUNDLE));

        daysBeforeReminderSpinner.setModel(new SpinnerNumberModel(1, 0, 30, 1));
        closeButton.addActionListener(this::onCloseButtonPressed);
        birthdaySoundChooserButton.addActionListener(this::onChooseButtonPressed);

        daysBeforeReminderSpinner.setToolTipText(text("notify.editor.dialog.daysBeforeReminder.tip"));
        birthdaySoundPath.setToolTipText(text("notify.editor.dialog.birthdaySound.tip"));

        getRootPane().registerKeyboardAction(this::onEscapePressed,
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        setIconImage(IMAGE_ICON);
        setTitle(TITLE);
        setSize(SIZE);
        setModalityType(ModalityType.APPLICATION_MODAL);
        setLocationRelativeTo(owner);
    }

    public BirthdayNotifierEditorGui(Window owner, BirthdayNotifierConfig.Config config) {
        this(owner);

        daysBeforeReminderSpinner.setValue(config.daysBeforeReminder);
        birthdaySoundPath.setText(config.birthdaySoundPath != null ? config.birthdaySoundPath : null);
    }

    private void onEscapePressed(ActionEvent e) {
        dispose();
    }

    private void onCloseButtonPressed(ActionEvent e) {
        dispose();
    }

    private void onChooseButtonPressed(ActionEvent e) {
        if (birthdaySoundChooser == null) {
            birthdaySoundChooser = new JFileChooser(System.getProperty("user.dir"));
            birthdaySoundChooser.setMultiSelectionEnabled(false);
            birthdaySoundChooser.addChoosableFileFilter(new WavFileFilter());
            birthdaySoundChooser.setAcceptAllFileFilterUsed(true);
        }

        String location = birthdaySoundPath.getText();
        File file = new File(!location.isBlank() ? location : System.getProperty("user.dir"));
        birthdaySoundChooser.setCurrentDirectory(file);
        int returnVal = birthdaySoundChooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            birthdaySoundPath.setText(birthdaySoundChooser.getSelectedFile().getAbsolutePath());
        }
    }

    public BirthdayNotifierConfig.Config toConfig() {
        int daysBeforeReminder = (int) daysBeforeReminderSpinner.getValue();
        String birthdaySound = birthdaySoundPath.getText();
        return new BirthdayNotifierConfig.Config(daysBeforeReminder, birthdaySound);
    }

    void destroy() {
        Main.destroyContainer(this);
        Main.destroyContainer(getContentPane());
        daysBeforeReminderSpinner = null;
        birthdaySoundPath = null;
        birthdaySoundChooser = null;
        birthdaySoundChooserButton = null;
        closeButton = null;
        getRootPane().unregisterKeyboardAction(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0));
    }

    public JSpinner daysBeforeReminderSpinner;
    public JTextField birthdaySoundPath;
    public JButton birthdaySoundChooserButton;
    public JFileChooser birthdaySoundChooser;
    public JButton closeButton;

    private static class WavFileFilter extends FileFilter {

        @Override
        public boolean accept(File f) {
            return f.isDirectory() || f.getName().toLowerCase(Locale.ROOT).endsWith(".wav");
        }

        @Override
        public String getDescription() {
            return text("notify.editor.dialog.birthdaySound.fileType");
        }
    }
}