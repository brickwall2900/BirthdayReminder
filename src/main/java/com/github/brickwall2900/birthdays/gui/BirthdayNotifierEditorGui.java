package com.github.brickwall2900.birthdays.gui;

import com.github.brickwall2900.birthdays.config.BirthdayNotifierConfig;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Locale;

import static com.github.brickwall2900.birthdays.TranslatableText.text;
import static org.httprpc.sierra.UIBuilder.*;

public class BirthdayNotifierEditorGui extends JDialog {
    public static final String TITLE = text("notify.editor.dialog.title");
    public static final Dimension SIZE = new Dimension(400, 150);
    public static final int BORDER = 8;

    public BirthdayNotifierEditorGui(Window owner) {
        super(owner);

        buildContentPane();

        daysBeforeReminderSpinner.setModel(new SpinnerNumberModel(1, 1, 30, 1));
        closeButton.addActionListener(this::onCloseButtonPressed);
        birthdaySoundChooserButton.addActionListener(this::onChooseButtonPressed);

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

    private void buildContentPane() {
        JPanel contentPane = column(4,
                row(4,
                        cell(daysBeforeReminderLabel = new JLabel(text("notify.editor.dialog.daysBeforeReminder"))),
                        cell(daysBeforeReminderSpinner = new JSpinner()).weightBy(1)),
                row(4,
                        cell(birthdaySoundLabel = new JLabel(text("notify.editor.dialog.birthdaySoundLabel"))),
                        cell(birthdaySoundPath = new JTextField()).weightBy(1),
                        cell(birthdaySoundChooserButton = new JButton(">"))),
                glue(),
                row(4,
                        glue(),
                        cell(closeButton = new JButton(text("dialog.close"))))).getComponent();
        contentPane.setBorder(BorderFactory.createEmptyBorder(BORDER, BORDER, BORDER, BORDER));
        setContentPane(contentPane);
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

    public JLabel daysBeforeReminderLabel, birthdaySoundLabel;
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