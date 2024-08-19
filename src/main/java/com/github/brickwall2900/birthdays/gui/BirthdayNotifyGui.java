package com.github.brickwall2900.birthdays.gui;

import com.github.brickwall2900.birthdays.BirthdayObject;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.Objects;

import static org.httprpc.sierra.UIBuilder.*;

public class BirthdayNotifyGui extends JDialog {
    public static final Dimension SIZE = new Dimension(480, 120);
    public static final int BORDER = 8;
    public static final Image IMAGE_ICON;
    public static final ImageIcon ICON;

    public static final String[] MESSAGES = new String[] {
            "Wish them a good day!",
            "Celebrate them!",
            "Wish them well!",
            "Congratulate them!",
            "Greet them a happy birthday!"
    };

    static {
        try {
            IMAGE_ICON = ImageIO.read(Objects.requireNonNull(BirthdayNotifyGui.class.getResourceAsStream("/icon.png")));
            ICON = new ImageIcon(IMAGE_ICON.getScaledInstance(32, 32, Image.SCALE_DEFAULT));
        } catch (IOException | NullPointerException e) {
            throw new RuntimeException("Cannot read birthday icon!");
        }
    }

    public BirthdayNotifyGui(BirthdayObject object) {
        buildContentPane(object);

        label.setIcon(ICON);
        closeButton.addActionListener(this::onCloseButtonPressed);

        String title = String.format("Today is %s's birthday!!!", object.name);
        setTitle(title);
        setIconImage(IMAGE_ICON);
        setAlwaysOnTop(true);
        setSize(SIZE);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    private void buildContentPane(BirthdayObject object) {
        String labelContent = String.format("Today is %s's birthday! ", object.name);
        if (object.customMessage != null) {
            labelContent += object.customMessage;
        } else {
            labelContent += MESSAGES[(int) (Math.random() * MESSAGES.length)];
        }
        JPanel contentPane = column(4,
                cell(label = new JLabel(labelContent)).weightBy(1),
                row(4,
                        glue(),
                        cell(closeButton = new JButton("Close"))))
                .getComponent();
        contentPane.setBorder(BorderFactory.createEmptyBorder(BORDER, BORDER, BORDER, BORDER));
        setContentPane(contentPane);
    }

    private void onCloseButtonPressed(ActionEvent e) {
        dispose();
    }

    public JLabel label;
    public JButton closeButton;
}
