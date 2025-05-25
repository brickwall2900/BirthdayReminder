package com.github.brickwall2900.birthdays.gui;

import com.github.brickwall2900.birthdays.Main;
import com.github.brickwall2900.birthdays.config.BirthdayNotifierConfig;
import com.github.brickwall2900.birthdays.config.object.BirthdayObject;
import org.httprpc.sierra.DatePicker;
import org.httprpc.sierra.UILoader;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.time.LocalDate;
import java.util.Objects;

import static com.github.brickwall2900.birthdays.Main.IMAGE_ICON;
import static com.github.brickwall2900.birthdays.TranslatableText.BUNDLE;
import static com.github.brickwall2900.birthdays.TranslatableText.text;

public class BirthdayEditorGui extends JDialog {
    public static final Dimension SIZE = new Dimension(450, 270);
    public static final int FORM_INSETS = 2;

    private BirthdayObject birthday;
    private boolean canceled;

    public BirthdayEditorGui(BirthdayListEditorGui parent) {
        super(parent);

        setContentPane(UILoader.load(this, "/ui/birthdayEditor.xml", BUNDLE));
        initForm();

        enabledCheckBox.setSelected(true);
        closeButton.addActionListener(this::onCloseButtonPressed);
        cancelButton.addActionListener(this::onCancelButtonPressed);
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

    private void initForm() {
        JPanel contentPane = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(FORM_INSETS, FORM_INSETS, FORM_INSETS, FORM_INSETS);
        newField(text("editor.dialog.fields.name"), contentPane, nameField = new JTextField(), null, c);
        newField(text("editor.dialog.fields.date"), contentPane, datePicker = new DatePicker(), null, c);
        newField(text("editor.dialog.fields.enabled"), contentPane, enabledCheckBox = new JCheckBox(), null, c);
        newField(text("editor.dialog.fields.customMessage"), contentPane, customMessageField = new JTextField(), null, c);
        newField(text("editor.dialog.fields.override"), contentPane, overrideConfigButton = new JButton(), removeOverrideConfigButton = new JButton(), c);
        overrideConfigButton.setText(text("dialog.edit"));
        removeOverrideConfigButton.setText(text("dialog.remove"));

        formScrollPane.setViewportView(contentPane);
        formScrollPane.setBorder(null);
    }

    // TODO: I'm sure we can replace the forms with JGoodies FormLayout... It'd be much, much better than whatever I'm doing...
    static <T extends JComponent, X extends JComponent> void newField(String fieldName, JPanel where, T component, X feedbackComponent, GridBagConstraints c) {
        c.gridx = 0;
        c.gridy += 1;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.WEST;
        c.weightx = 0;
        where.add(new JLabel(fieldName), c);

        c.gridx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        where.add(component, c);

        c.gridy += 1;
        c.gridx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        where.add(Objects.requireNonNullElseGet(feedbackComponent, Box::createHorizontalGlue), c); // WHAT THAT'S A THING??
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
        int option = JOptionPane.showConfirmDialog(this,
                text("dialog.save.confirm"), getTitle(), JOptionPane.YES_NO_CANCEL_OPTION);
        if (option == JOptionPane.YES_OPTION) {
            dispose();
        } else if (option == JOptionPane.NO_OPTION) {
            canceled = true;
            dispose();
        }
    }

    private void onCloseButtonPressed(ActionEvent e) {
        dispose();
    }

    private void onCancelButtonPressed(ActionEvent e) {
        canceled = true;
        dispose();
    }

    private void onOverrideConfigButtonPressed(ActionEvent e) {
        BirthdayNotifierConfig.Config defaultConfig = BirthdayNotifierConfig.globalConfig;
        BirthdayNotifierEditorGui notifierEditorGui = new BirthdayNotifierEditorGui(this, birthday.override != null ? birthday.override : defaultConfig);
        notifierEditorGui.setVisible(true);
        // wait for user
        BirthdayNotifierConfig.Config modifiedConfig = notifierEditorGui.toConfig();
        if (modifiedConfig != null) {
            birthday.override = !defaultConfig.equals(modifiedConfig) ? modifiedConfig : null;
        }
        removeOverrideConfigButton.setEnabled(birthday.override != null);
        notifierEditorGui.destroy();
    }

    private void onRemoveOverrideConfigButtonPressed(ActionEvent e) {
        birthday.override = null;
        removeOverrideConfigButton.setEnabled(false);
    }

    public BirthdayObject toBirthday() {
        if (canceled) {
            return null;
        }
        String name = nameField.getText();
        if (name.isBlank()) return null;
        LocalDate date = datePicker.getDate();
        boolean enabled = enabledCheckBox.isSelected();
        String customMessage = customMessageField.getText();

        return birthday = new BirthdayObject(name, enabled, date, !customMessage.isBlank() ? customMessage : null, birthday.override);
    }

    void destroy() {
        Main.destroyContainer(this);
        Main.destroyContainer(getContentPane());
        nameField = null;
        datePicker = null;
        enabledCheckBox = null;
        customMessageField = null;
        overrideConfigButton = null;
        removeOverrideConfigButton = null;
        cancelButton = null;
        closeButton = null;
        birthday = null;
        formScrollPane = null;
        getRootPane().unregisterKeyboardAction(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0));
    }

    public JScrollPane formScrollPane;
    public JTextField nameField;
    public DatePicker datePicker;
    public JCheckBox enabledCheckBox;
    public JTextField customMessageField;
    public JButton overrideConfigButton, removeOverrideConfigButton;

    public JButton cancelButton, closeButton;
}
