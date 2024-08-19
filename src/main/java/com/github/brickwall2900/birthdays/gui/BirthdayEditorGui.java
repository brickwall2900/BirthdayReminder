package com.github.brickwall2900.birthdays.gui;

import com.github.brickwall2900.birthdays.BirthdayObject;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import java.awt.*;
import java.awt.event.ActionEvent;

import static org.httprpc.sierra.UIBuilder.*;

public class BirthdayEditorGui extends JFrame {
    public static final String TITLE = "Birthday Reminder Editor";
    public static final Dimension SIZE = new Dimension(640, 720);
    public static final int BORDER = 8;

    public BirthdayEditorGui() {
        buildContentPane();

        ListModel<BirthdayObject> defaultListModel = new DefaultListModel<>();
        birthdayList.setModel(defaultListModel);
        closeButton.addActionListener(this::onCloseButtonPressed);
        addButton.addActionListener(this::onAddButtonPressed);
        removeButton.addActionListener(this::onRemoveButtonPressed);
        birthdayList.addListSelectionListener(this::onListSelectionChanged);

        removeButton.setEnabled(false);

        setTitle(TITLE);
        setSize(SIZE);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    private void buildContentPane() {
        JPanel contentPane = column(4,
                cell(headerLabel = new JLabel("Double click a person's name to edit their birthday!")),
                cell(birthdayList = new JList<>()).weightBy(1),
                row(4,
                        cell(addButton = new JButton("Add")),
                        cell(removeButton = new JButton("Remove")),
                        glue(),
                        cell(closeButton = new JButton("Close")))).getComponent();
        contentPane.setBorder(BorderFactory.createEmptyBorder(BORDER, BORDER, BORDER, BORDER));
        setContentPane(contentPane);
    }

    private void onCloseButtonPressed(ActionEvent e) {
        System.exit(0);
    }

    private void onAddButtonPressed(ActionEvent e) {
        BirthdayEditDialogBox editBox = new BirthdayEditDialogBox(this);
        editBox.setVisible(true);
        // wait for user
        BirthdayObject object = editBox.toBirthday();
        if (object != null) {
            DefaultListModel<BirthdayObject> model = (DefaultListModel<BirthdayObject>) birthdayList.getModel();
            model.addElement(object);
        }
    }

    private void onRemoveButtonPressed(ActionEvent e) {
        DefaultListModel<BirthdayObject> model = (DefaultListModel<BirthdayObject>) birthdayList.getModel();
        BirthdayObject selected = birthdayList.getSelectedValue();
        if (selected != null) {
            model.removeElement(selected);
        }
    }

    private void onListSelectionChanged(ListSelectionEvent e) {
        BirthdayObject selected = birthdayList.getSelectedValue();
        removeButton.setEnabled(selected != null);

        if (!e.getValueIsAdjusting() && selected != null) {
            BirthdayEditDialogBox editBox = new BirthdayEditDialogBox(this, selected);
            editBox.setVisible(true);
            // wait for user

            BirthdayObject object = editBox.toBirthday();
            if (object != null) {
                DefaultListModel<BirthdayObject> model = (DefaultListModel<BirthdayObject>) birthdayList.getModel();
                int index = birthdayList.getSelectedIndex();
                model.set(index, object);
            }
        }
    }

    public JLabel headerLabel;
    public JList<BirthdayObject> birthdayList;
    public JButton addButton, removeButton, closeButton;
}
