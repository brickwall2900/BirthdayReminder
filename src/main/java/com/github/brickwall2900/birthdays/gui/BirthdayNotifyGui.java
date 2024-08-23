package com.github.brickwall2900.birthdays.gui;

import com.github.brickwall2900.birthdays.BirthdaysManager;
import com.github.brickwall2900.birthdays.config.BirthdayNotifierConfig;
import com.github.brickwall2900.birthdays.config.object.BirthdayObject;

import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import static com.github.brickwall2900.birthdays.TranslatableText.getArray;
import static com.github.brickwall2900.birthdays.TranslatableText.text;
import static org.httprpc.sierra.UIBuilder.*;

public class BirthdayNotifyGui extends JDialog {
    public static final Dimension SIZE = new Dimension(480, 120);
    public static final int BORDER = 8;
    public static final Image IMAGE_ICON;
    public static final ImageIcon ICON;

    public static final String[] MESSAGES = getArray("messages");

    static {
        try {
            IMAGE_ICON = ImageIO.read(Objects.requireNonNull(BirthdayNotifyGui.class.getResourceAsStream("/icon.png")));
            ICON = new ImageIcon(IMAGE_ICON.getScaledInstance(32, 32, Image.SCALE_DEFAULT));
        } catch (IOException | NullPointerException e) {
            throw new RuntimeException("Cannot read birthday icon!");
        }
    }

    private final BirthdayObject birthday;
    private Clip clip;

    public BirthdayNotifyGui(BirthdayObject object) {
        this.birthday = object;
        buildContentPane(object);

        label.setIcon(ICON);
        closeButton.addActionListener(this::onCloseButtonPressed);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                onClosed();
            }

            @Override
            public void windowClosing(WindowEvent e) {
                onClosed();
            }
        });

        String title = text("notify.title", object.name);
        setTitle(title);
        setIconImage(IMAGE_ICON);
        setAlwaysOnTop(true);
        setSize(SIZE);
        setLocationRelativeTo(null);
        setModalityType(ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    private void buildContentPane(BirthdayObject object) {
        String labelContent = text("notify.content",
                object.name,
                BirthdaysManager.getAgeInDays(object),
                object.customMessage != null
                        ? object.customMessage
                        : MESSAGES[(int) (Math.random() * MESSAGES.length)]);
        JPanel contentPane = column(4,
                cell(label = new JLabel(labelContent)).weightBy(1),
                row(4,
                        glue(),
                        cell(closeButton = new JButton(text("dialog.close")))))
                .getComponent();
        contentPane.setBorder(BorderFactory.createEmptyBorder(BORDER, BORDER, BORDER, BORDER));
        setContentPane(contentPane);
    }

    private void onCloseButtonPressed(ActionEvent e) {
        dispose();
    }

    private void onClosed() {
        if (clip != null) {
            clip.stop();
            clip.close();
        }
    }

    public void open() {
        playSound();
        setVisible(true);
    }

    private void playSound() {
        String soundLocation = birthday.override != null
                ? birthday.override.birthdaySoundPath
                : BirthdayNotifierConfig.globalConfig.birthdaySoundPath;

        if (soundLocation != null && !soundLocation.isBlank()) {
            Path soundPath = Paths.get(soundLocation);
            try {
                DataLine.Info info = new DataLine.Info(Clip.class, null);
                clip = (Clip) AudioSystem.getLine(info);
                clip.open(AudioSystem.getAudioInputStream(soundPath.toFile()));
                clip.loop(Clip.LOOP_CONTINUOUSLY);
            } catch (LineUnavailableException | IOException | UnsupportedAudioFileException e) {
                System.err.println("Cannot open a line for " + soundLocation);
                e.printStackTrace();
            }
        }
    }

    public JLabel label;
    public JButton closeButton;
}
