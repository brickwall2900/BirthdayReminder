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
import java.time.chrono.Chronology;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static com.github.brickwall2900.birthdays.Main.IMAGE_ICON;
import static com.github.brickwall2900.birthdays.TranslatableText.BUNDLE;
import static com.github.brickwall2900.birthdays.TranslatableText.text;

public class BirthdayEditorGui extends BaseDialog<BirthdayObject> {
    public static final Dimension SIZE = new Dimension(450, 270);
    public static final int FORM_INSETS = 2;

    public final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(
            DateTimeFormatterBuilder.getLocalizedDateTimePattern(
                    FormatStyle.MEDIUM, null, Chronology.ofLocale(getLocale()), getLocale()));

    private static final String DATES_PATTERNS;

    static {
        List<String> patterns = ProperInputVerifier.PATTERNS;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append('\n');
        for (int i = 0; i < patterns.size(); i++) {
            String pattern = patterns.get(i);
            stringBuilder.append(pattern);
            if (i <= patterns.size() - 2) {
                stringBuilder.append(',').append('\n');
            }
        }
        DATES_PATTERNS = stringBuilder.toString();
    }

    private BirthdayObject birthday;

    public BirthdayEditorGui(BirthdayListEditorGui parent) {
        super(parent, text("editor.dialog.title", "???"));

        setContentPane(UILoader.load(this, "/ui/birthdayEditor.xml", BUNDLE));
        initForm();

        enabledCheckBox.setSelected(true);
        closeButton.addActionListener(this::onCloseButtonPressed);
        cancelButton.addActionListener(this::onCancelButtonPressed);
        overrideConfigButton.addActionListener(this::onOverrideConfigButtonPressed);
        removeOverrideConfigButton.addActionListener(this::onRemoveOverrideConfigButtonPressed);
        removeOverrideConfigButton.setEnabled(false);

        nameField.setToolTipText(text("editor.dialog.fields.name.tip"));
        datePicker.setToolTipText(text("editor.dialog.fields.date.tip", DATES_PATTERNS));
        enabledCheckBox.setToolTipText(text("editor.dialog.fields.enabled.tip"));
        customMessageField.setToolTipText(text("editor.dialog.fields.customMessage.tip"));
        overrideConfigButton.setToolTipText(text("editor.dialog.fields.override.tip"));

        datePicker.setInputVerifier(new ProperInputVerifier());

        this.birthday = new BirthdayObject();

        setIconImage(IMAGE_ICON);
        setSize(SIZE);
        setLocationRelativeTo(parent);
        setModalityType(ModalityType.APPLICATION_MODAL);
    }

    void initForm() {
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
        datePicker.setDate(birthday.date); // set date internally
        datePicker.setText(dateFormatter.format(birthday.date)); // set date in text field
        enabledCheckBox.setSelected(birthday.enabled);
        customMessageField.setText(birthday.customMessage);

        removeOverrideConfigButton.setEnabled(birthday.override != null);
    }

    private void onOverrideConfigButtonPressed(ActionEvent e) {
        BirthdayNotifierConfig.Config defaultConfig = BirthdayNotifierConfig.globalConfig;
        BirthdayNotifierEditorGui notifierEditorGui = new BirthdayNotifierEditorGui(this, birthday.override != null ? birthday.override : defaultConfig);
        notifierEditorGui.setVisible(true);
        // wait for user
        BirthdayNotifierConfig.Config modifiedConfig = notifierEditorGui.getResult();
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

    public BirthdayObject getResult() {
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

    class ProperInputVerifier extends InputVerifier {
        private static final List<String> PATTERNS;
        private static final List<DateTimeFormatter> FORMATTERS;

        static {
            PATTERNS = new ArrayList<>();
            FORMATTERS = new ArrayList<>();
            for (FormatStyle formatStyle : FormatStyle.values()) {
                String pattern = DateTimeFormatterBuilder.getLocalizedDateTimePattern(
                        formatStyle, null, Chronology.ofLocale(Locale.getDefault()), Locale.getDefault());
                PATTERNS.add(pattern);
                DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(pattern);
                FORMATTERS.add(dateFormatter);
            }
        }

        private boolean validate(LocalDate date, DatePicker datePicker) {
            LocalDate minimumDate = datePicker.getMinimumDate();
            LocalDate maximumDate = datePicker.getMaximumDate();
            return (minimumDate == null || !date.isBefore(minimumDate))
                    && (maximumDate == null || !date.isAfter(maximumDate));
        }

        @Override
        public boolean verify(JComponent input) {
            if (input instanceof DatePicker picker) {
                LocalDate pickerDate = picker.getDate();
                System.out.println("PickerDate " + pickerDate);
                try {
                    LocalDate date = null;
                    for (DateTimeFormatter formatter : FORMATTERS) {
                        try {
                            date = LocalDate.parse(picker.getText(), formatter);
                            break;
                        } catch (DateTimeParseException ignored) {
                        }
                    }
                    System.out.println("Date " + date);
                    if (date == null) {
                        throw new IllegalStateException();
                    }

                    if (date.equals(pickerDate)) {
                        return true;
                    }

                    if (!validate(date, picker)) {
                        picker.setText(dateFormatter.format(pickerDate));
                        picker.selectAll();

                        return false;
                    }

                    datePicker.setDate(date); // This sets the internal date
                    datePicker.setText(dateFormatter.format(date)); // This sets the date shown on the field
                } catch (IllegalStateException exception) {
                    datePicker.setText(dateFormatter.format(pickerDate));
                }

            }
            return true;
        }
    }

    public JScrollPane formScrollPane;
    public JTextField nameField;
    public DatePicker datePicker;
    public JCheckBox enabledCheckBox;
    public JTextField customMessageField;
    public JButton overrideConfigButton, removeOverrideConfigButton;

    public JButton cancelButton, closeButton;
}
