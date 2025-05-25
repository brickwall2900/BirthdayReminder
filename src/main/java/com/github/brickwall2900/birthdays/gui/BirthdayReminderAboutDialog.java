package com.github.brickwall2900.birthdays.gui;

import com.github.brickwall2900.birthdays.LicenseManager;
import com.github.brickwall2900.birthdays.Main;
import org.httprpc.sierra.UILoader;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import static com.github.brickwall2900.birthdays.Main.IMAGE_ICON;
import static com.github.brickwall2900.birthdays.TranslatableText.BUNDLE;
import static com.github.brickwall2900.birthdays.TranslatableText.text;

public class BirthdayReminderAboutDialog extends JDialog {
    public static final String TITLE = text("about.title");
    public static final Dimension SIZE = new Dimension(600, 200);

    public BirthdayReminderAboutDialog(Window owner) {
        super(owner);

        setContentPane(UILoader.load(this, "/ui/about.xml", BUNDLE));

        closeButton.addActionListener(this::onCloseButtonPressed);
        licenseButton.addActionListener(this::onLicenseButtonPressed);

        header.setFont(header.getFont().deriveFont(Font.BOLD, 18));

        contentScrollPane.setViewportView(contentPane = new JTextPane());
        contentPane.setContentType("text/html");
        contentPane.setText(text("about.content"));
        contentPane.setEditable(false);
        contentPane.setBorder(null);
        contentScrollPane.setBorder(null);

        getRootPane().registerKeyboardAction(this::onCloseButtonPressed,
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        setIconImage(IMAGE_ICON);
        setTitle(TITLE);
        setSize(SIZE);
        setModalityType(ModalityType.APPLICATION_MODAL);
        setLocationRelativeTo(owner);
    }

    private void onCloseButtonPressed(ActionEvent e) {
        dispose();
    }

    private void onLicenseButtonPressed(ActionEvent e) {
        LicenseManager.openLicense();
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
        getRootPane().unregisterKeyboardAction(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0));
    }

    public JLabel header;
    public JButton licenseButton, closeButton;
    public JScrollPane contentScrollPane;
    public JTextPane contentPane;
}
