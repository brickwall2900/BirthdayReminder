package com.github.brickwall2900.birthdays.gui;

import com.github.brickwall2900.birthdays.BundleMultiplexer;
import com.github.brickwall2900.birthdays.Main;
import com.github.brickwall2900.birthdays.config.BirthdayNotifierConfig;
import org.httprpc.sierra.UILoader;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ResourceBundle;

import static com.github.brickwall2900.birthdays.Main.IMAGE_ICON;

public class BirthdayNotifierEditorGui extends BaseDialog<BirthdayNotifierConfig> {
    public static final Dimension SIZE = new Dimension(450, 180);
    public static final int FORM_INSETS = 2;

    private ResourceBundle bundle = new BundleMultiplexer(
            ResourceBundle.getBundle(BirthdayNotifierEditorGui.class.getName()),
            super.bundle
    );

    public BirthdayNotifierEditorGui(Window owner) {
        super(owner);

        setContentPane(UILoader.load(this, "birthdayNotifyEdit.xml", bundle));
        initForm();

        daysBeforeReminderSpinner.setModel(new SpinnerNumberModel(1, 0, 30, 1));
        closeButton.addActionListener(this::onCloseButtonPressed);
        cancelButton.addActionListener(this::onCancelButtonPressed);
        birthdaySoundChooserButton.addActionListener(this::onChooseButtonPressed);

        daysBeforeReminderSpinner.setToolTipText(bundle.getString("notify.editor.dialog.daysBeforeReminder.tip"));
        birthdaySoundPath.setToolTipText(bundle.getString("notify.editor.dialog.birthdaySound.tip"));

        setIconImage(IMAGE_ICON);
        setTitle(bundle.getString("notify.editor.dialog.title"));
        setSize(SIZE);
        setModalityType(ModalityType.APPLICATION_MODAL);
        setLocationRelativeTo(owner);
    }

    private static final long NO_VALUE = ((long) Integer.MAX_VALUE) + 1;
    private long configHashCode = NO_VALUE;

    public BirthdayNotifierEditorGui(Window owner, BirthdayNotifierConfig config) {
        this(owner);

        daysBeforeReminderSpinner.setValue(config.daysBeforeReminder);
        birthdaySoundPath.setText(config.birthdaySoundPath != null ? config.birthdaySoundPath : null);
        configHashCode = config.hashCode();
    }

    void initForm() {
        JPanel contentPane = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(FORM_INSETS, FORM_INSETS, FORM_INSETS, FORM_INSETS);
        newField(bundle.getString("notify.editor.dialog.daysBeforeReminder"),
                contentPane,
                daysBeforeReminderSpinner = new JSpinner(),
                null,
                c);
        newField(bundle.getString("notify.editor.dialog.birthdaySoundLabel"),
                contentPane,
                birthdaySoundPath = new JTextField(),
                birthdaySoundChooserButton = new JButton(bundle.getString("dialog.open")),
                c);

        formScrollPane.setViewportView(contentPane);
        formScrollPane.setBorder(null);
    }

    private void onChooseButtonPressed(ActionEvent e) {
        if (birthdaySoundChooser == null) {
            birthdaySoundChooser = new JFileChooser(System.getProperty("user.dir"));
            birthdaySoundChooser.setMultiSelectionEnabled(false);
            birthdaySoundChooser.addChoosableFileFilter(new FileNameExtensionFilter(
                    bundle.getString("notify.editor.dialog.birthdaySound.fileType"), "wav"));
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

    private BirthdayNotifierConfig makeConfig() {
        int daysBeforeReminder = (int) daysBeforeReminderSpinner.getValue();
        String birthdaySound = birthdaySoundPath.getText();
        return new BirthdayNotifierConfig(daysBeforeReminder, birthdaySound);
    }

    public BirthdayNotifierConfig getResult() {
        if (canceled || !isDirty()) {
            return null;
        }

        return makeConfig();
    }

    @Override
    public boolean isDirty() {
        return configHashCode != NO_VALUE && makeConfig().hashCode() != configHashCode;
    }

    protected void destroy() {
        Main.destroyContainer(this);
        Main.destroyContainer(getContentPane());
        daysBeforeReminderSpinner = null;
        birthdaySoundPath = null;
        birthdaySoundChooser = null;
        birthdaySoundChooserButton = null;
        cancelButton = null;
        closeButton = null;
        formScrollPane = null;
        bundle = null;
        super.destroy();
    }

    public JScrollPane formScrollPane;
    public JSpinner daysBeforeReminderSpinner;
    public JTextField birthdaySoundPath;
    public JButton birthdaySoundChooserButton;
    public JFileChooser birthdaySoundChooser;
    public JButton cancelButton, closeButton;
}