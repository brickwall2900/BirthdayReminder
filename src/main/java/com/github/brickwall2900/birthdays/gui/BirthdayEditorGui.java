package com.github.brickwall2900.birthdays.gui;

import com.github.brickwall2900.birthdays.config.BirthdayNotifierConfig;
import com.github.brickwall2900.birthdays.config.object.BirthdayObject;
import org.httprpc.sierra.DatePicker;
import org.httprpc.sierra.UILoader;

import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.time.LocalDate;

import static com.github.brickwall2900.birthdays.Main.IMAGE_ICON;
import static com.github.brickwall2900.birthdays.TranslatableText.BUNDLE;
import static com.github.brickwall2900.birthdays.TranslatableText.text;

public class BirthdayEditorGui extends JDialog {
    public static final Dimension SIZE = new Dimension(400, 250);
    public static final int BORDER = 8;

    private BirthdayObject birthday;

    public BirthdayEditorGui(BirthdayListEditorGui parent) {
        super(parent);

        setContentPane(UILoader.load(this, "/ui/birthdayEditor.xml", BUNDLE));

        enabledCheckBox.setSelected(true);
        closeButton.addActionListener(this::onCloseButtonPressed);
        overrideConfigButton.addActionListener(this::onOverrideConfigButtonPressed);
        removeOverrideConfigButton.addActionListener(this::onRemoveOverrideConfigButtonPressed);
        removeOverrideConfigButton.setEnabled(false);

        nameField.setToolTipText(text("editor.dialog.fields.name.tip"));
        datePicker.setToolTipText(text("editor.dialog.fields.date.tip"));
        enabledCheckBox.setToolTipText(text("editor.dialog.fields.enabled.tip"));
        customMessageField.setToolTipText(text("editor.dialog.fields.customMessage.tip"));
        overrideConfigButton.setToolTipText(text("editor.dialog.fields.override.tip"));

        this.birthday = new BirthdayObject();

        getRootPane().registerKeyboardAction(this::onEscapePressed,
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        setIconImage(IMAGE_ICON);
        setTitle(text("editor.dialog.title", "???"));
        setSize(SIZE);
        setLocationRelativeTo(parent);
        setModalityType(ModalityType.APPLICATION_MODAL);
    }

    public BirthdayEditorGui(BirthdayListEditorGui parent, BirthdayObject birthday) {
        this(parent);
        this.birthday = birthday;

        setTitle(text("editor.dialog.title", birthday.name));
        nameField.setText(birthday.name);
        datePicker.setDate(birthday.date);
        enabledCheckBox.setSelected(birthday.enabled);
        customMessageField.setText(birthday.customMessage);

        removeOverrideConfigButton.setEnabled(birthday.override != null);
    }
    
    private void onEscapePressed(ActionEvent e) {
        dispose();
    }

    private void onCloseButtonPressed(ActionEvent e) {
        dispose();
    }

    private void onOverrideConfigButtonPressed(ActionEvent e) {
        BirthdayNotifierConfig.Config defaultConfig = BirthdayNotifierConfig.globalConfig;
        BirthdayNotifierEditorGui notifierEditorGui = new BirthdayNotifierEditorGui(this, birthday.override != null ? birthday.override : defaultConfig);
        notifierEditorGui.setVisible(true);
        // wait for user
        BirthdayNotifierConfig.Config modifiedConfig = notifierEditorGui.toConfig();
        birthday.override = !defaultConfig.equals(modifiedConfig) ? modifiedConfig : null;
        removeOverrideConfigButton.setEnabled(birthday.override != null);
    }

    private void onRemoveOverrideConfigButtonPressed(ActionEvent e) {
        birthday.override = null;
        removeOverrideConfigButton.setEnabled(false);
    }

    public BirthdayObject toBirthday() {
        String name = nameField.getText();
        if (name.isBlank()) return null;
        LocalDate date = datePicker.getDate();
        boolean enabled = enabledCheckBox.isSelected();
        String customMessage = customMessageField.getText();

        return birthday = new BirthdayObject(name, enabled, date, !customMessage.isBlank() ? customMessage : null, birthday.override);
    }

    public JLabel nameLabel, dateLabel, enabledLabel, customMessageLabel, overrideConfigLabel;

    public JTextField nameField;
    public DatePicker datePicker;
    public JCheckBox enabledCheckBox;
    public JTextField customMessageField;
    public JButton overrideConfigButton, removeOverrideConfigButton;

    public JButton closeButton;
}
