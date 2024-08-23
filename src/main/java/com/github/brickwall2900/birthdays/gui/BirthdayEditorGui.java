package com.github.brickwall2900.birthdays.gui;

import com.github.brickwall2900.birthdays.config.BirthdayNotifierConfig;
import com.github.brickwall2900.birthdays.config.object.BirthdayObject;
import org.httprpc.sierra.DatePicker;

import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.LocalDate;

import static com.github.brickwall2900.birthdays.TranslatableText.text;
import static org.httprpc.sierra.UIBuilder.*;

public class BirthdayEditorGui extends JDialog {
    public static final Dimension SIZE = new Dimension(400, 200);
    public static final int BORDER = 8;

    private BirthdayObject birthday;

    public BirthdayEditorGui(BirthdayListEditorGui parent) {
        super(parent);

        buildContentPane();
        enabledCheckBox.setSelected(true);
        closeButton.addActionListener(this::onCloseButtonPressed);
        overrideConfigButton.addActionListener(this::onOverrideConfigButtonPressed);
        removeOverrideConfigButton.addActionListener(this::onRemoveOverrideConfigButtonPressed);
        removeOverrideConfigButton.setEnabled(false);

        this.birthday = new BirthdayObject();

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

    private void buildContentPane() {
        JPanel contentPane = column(4,
                row(4,
                        cell(nameLabel = new JLabel(text("editor.dialog.fields.name"))),
                        cell(nameField = new JTextField()).weightBy(1)),
                row(4,
                        cell(dateLabel = new JLabel(text("editor.dialog.fields.date"))),
                        cell(datePicker = new DatePicker()).weightBy(1)),
                row(4,
                        cell(enabledLabel = new JLabel(text("editor.dialog.fields.enabled"))),
                        cell(enabledCheckBox = new JCheckBox())),
                row(4,
                        cell(customMessageLabel = new JLabel(text("editor.dialog.fields.customMessage"))),
                        cell(customMessageField = new JTextField()).weightBy(1)),
                row(4,
                        cell(overrideConfigLabel = new JLabel(text("editor.dialog.fields.override"))),
                        cell(overrideConfigButton = new JButton(text("dialog.edit"))).weightBy(1),
                        cell(removeOverrideConfigButton = new JButton(text("dialog.remove")))),
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

    private void onOverrideConfigButtonPressed(ActionEvent e) {
        BirthdayNotifierConfig.Config defaultConfig = new BirthdayNotifierConfig.Config();
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
