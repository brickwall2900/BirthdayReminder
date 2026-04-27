package com.github.brickwall2900.birthdays.gui;

import com.github.brickwall2900.birthdays.Main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Objects;

import static com.github.brickwall2900.birthdays.TranslatableText.text;

public abstract class BaseDialog<T> extends JDialog {
    protected boolean canceled;

    private void init() {
        getRootPane().registerKeyboardAction(this::onEscapePressed,
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
        setIconImage(Main.IMAGE_ICON);

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                onEscapePressed(null);
            }
        });
    }

    public BaseDialog(Frame owner, String title) {
        super(owner, title);
        init();
    }

    public BaseDialog(Dialog owner, String title) {
        super(owner, title);
        init();
    }

    public BaseDialog(Window owner, String title) {
        super(owner, title);
        init();
    }

    abstract void initForm();
    public abstract T getResult();

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

    protected void onCloseButtonPressed(ActionEvent e) {
        dispose();
    }

    protected void onCancelButtonPressed(ActionEvent e) {
        canceled = true;
        dispose();
    }

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
}
