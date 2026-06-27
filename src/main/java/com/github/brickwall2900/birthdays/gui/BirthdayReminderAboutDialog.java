package com.github.brickwall2900.birthdays.gui;

import com.github.brickwall2900.birthdays.BundleMultiplexer;
import com.github.brickwall2900.birthdays.LicenseManager;
import com.github.brickwall2900.birthdays.Main;
import com.github.brickwall2900.birthdays.config.ConfigHolder;
import org.httprpc.sierra.UILoader;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ResourceBundle;

import static com.github.brickwall2900.birthdays.Main.IMAGE_ICON;

public class BirthdayReminderAboutDialog extends JDialog {
    public static final Dimension SIZE = new Dimension(600, 230);

    private ResourceBundle bundle = new BundleMultiplexer(
            ResourceBundle.getBundle(BirthdayReminderAboutDialog.class.getName()),
            ResourceBundle.getBundle(BaseDialog.class.getName())
    );

    public BirthdayReminderAboutDialog(Window owner) {
        super(owner);

        setContentPane(UILoader.load(this, "about.xml", bundle));

        darkModeCheckbox.setSelected(ConfigHolder.getApplicationConfig().darkMode);
        header.setFont(header.getFont().deriveFont(Font.BOLD, 18));

        contentScrollPane.setViewportView(contentPane = new JTextPane());
        contentPane.setContentType("text/html");
        contentPane.setText(bundle.getString("about.content").formatted(Main.getVersion()));
        contentPane.setEditable(false);
        contentPane.setBorder(null);
        contentScrollPane.setBorder(null);

        closeButton.addActionListener(this::onCloseButtonPressed);
        licenseButton.addActionListener(this::onLicenseButtonPressed);
        darkModeCheckbox.addActionListener(this::onDarkModeCheckboxSelected);

        getRootPane().registerKeyboardAction(this::onCloseButtonPressed,
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        setIconImage(IMAGE_ICON);
        setTitle(bundle.getString("about.title"));
        setSize(SIZE);
        setModalityType(ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(owner);
    }

    private void onCloseButtonPressed(ActionEvent e) {
        dispose();
    }

    private void onLicenseButtonPressed(ActionEvent e) {
        LicenseManager.openLicense();
    }

    private void onDarkModeCheckboxSelected(ActionEvent e) {
        Main.setDarkMode(darkModeCheckbox.isSelected());
        contentPane.setBorder(null);
        contentScrollPane.setBorder(null);
    }

    void destroy() {
        Main.destroyContainer(this);
        Main.destroyContainer(getContentPane());
        header = null;
        licenseButton = null;
        closeButton = null;
        contentScrollPane = null;
        contentPane.setText(null);
        contentPane = null;
        bundle = null;
        getRootPane().unregisterKeyboardAction(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0));
    }

    public JLabel header;
    public JButton licenseButton, closeButton;
    public JCheckBox darkModeCheckbox;
    public JScrollPane contentScrollPane;
    public JTextPane contentPane;
}
