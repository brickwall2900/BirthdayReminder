package com.github.brickwall2900.birthdays.gui;

import com.github.brickwall2900.birthdays.BirthdayObject;
import org.httprpc.sierra.DatePicker;

import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.LocalDate;

import static org.httprpc.sierra.UIBuilder.*;

public class BirthdayEditDialogBox extends JDialog {
    public static final String TITLE = "Edit someone's birthday...";
    public static final Dimension SIZE = new Dimension(400, 200);
    public static final int BORDER = 8;

    public BirthdayEditDialogBox(BirthdayEditorGui parent) {
        super(parent);

        buildContentPane();
        enabledCheckBox.setSelected(true);
        closeButton.addActionListener(this::onCloseButtonPressed);

        setTitle(TITLE);
        setSize(SIZE);
        setLocationRelativeTo(parent);
        setModalityType(ModalityType.APPLICATION_MODAL);
    }

    public BirthdayEditDialogBox(BirthdayEditorGui parent, BirthdayObject birthday) {
        this(parent);

        nameField.setText(birthday.name);
        datePicker.setDate(birthday.date);
        enabledCheckBox.setSelected(birthday.enabled);
        customMessageField.setText(birthday.customMessage);
    }

    private void buildContentPane() {
        JPanel contentPane = column(4,
                row(4,
                        cell(nameLabel = new JLabel("Name: ")),
                        cell(nameField = new JTextField()).weightBy(1)),
                row(4,
                        cell(dateLabel = new JLabel("Date: ")),
                        cell(datePicker = new DatePicker()).weightBy(1)),
                row(4,
                        cell(enabledLabel = new JLabel("Enabled: ")),
                        cell(enabledCheckBox = new JCheckBox())),
                row(4,
                        cell(customMessageLabel = new JLabel("Custom Message: ")),
                        cell(customMessageField = new JTextField()).weightBy(1)),
                glue(),
                row(4,
                        glue(),
                        cell(closeButton = new JButton("Close")))).getComponent();
        contentPane.setBorder(BorderFactory.createEmptyBorder(BORDER, BORDER, BORDER, BORDER));
        setContentPane(contentPane);
    }

    private void onCloseButtonPressed(ActionEvent e) {
        dispose();
    }

    public BirthdayObject toBirthday() {
        String name = nameField.getText();
        if (name.isBlank()) return null;
        LocalDate date = datePicker.getDate();
        boolean enabled = enabledCheckBox.isSelected();
        String customMessage = customMessageField.getText();

        return new BirthdayObject(name, enabled, date, !customMessage.isBlank() ? customMessage : null);
    }

    public JLabel nameLabel, dateLabel, enabledLabel, customMessageLabel;

    public JTextField nameField;
    public DatePicker datePicker;
    public JCheckBox enabledCheckBox;
    public JTextField customMessageField;

    public JButton closeButton;
}
