package com.github.brickwall2900.birthdays.gui;

import com.github.brickwall2900.birthdays.BirthdayObject;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Consumer;

import static com.github.brickwall2900.birthdays.TranslatableText.text;
import static org.httprpc.sierra.UIBuilder.*;

public class BirthdayEditorGui extends JFrame {
    public static final String TITLE = text("editor.title");
    public static final Dimension SIZE = new Dimension(640, 720);
    public static final int BORDER = 8;

    public BirthdayEditorGui(BirthdayObject[] objects) {
        buildContentPane();

        DefaultListModel<BirthdayObject> defaultListModel = new DefaultListModel<>();
        defaultListModel.addAll(Arrays.asList(objects));
        birthdayList.setModel(defaultListModel);
        closeButton.addActionListener(this::onCloseButtonPressed);
        addButton.addActionListener(this::onAddButtonPressed);
        removeButton.addActionListener(this::onRemoveButtonPressed);
        editButton.addActionListener(this::onEditButtonPressed);
        birthdayList.addListSelectionListener(this::onListSelectionChanged);

        removeButton.setEnabled(false);
        editButton.setEnabled(false);

        setTitle(TITLE);
        setSize(SIZE);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    private void buildContentPane() {
        JPanel contentPane = column(4,
                cell(headerLabel = new JLabel(text("editor.header"))),
                cell(birthdayScrollPane = new JScrollPane(birthdayList = new JList<>())).weightBy(1),
                row(4,
                        cell(addButton = new JButton(text("dialog.add"))),
                        cell(removeButton = new JButton(text("dialog.remove"))),
                        cell(editButton = new JButton(text("dialog.edit"))),
                        glue(),
                        cell(closeButton = new JButton(text("dialog.close"))))).getComponent();
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

    private void onEditButtonPressed(ActionEvent e) {
        BirthdayObject selected = birthdayList.getSelectedValue();
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

    private void onListSelectionChanged(ListSelectionEvent e) {
        BirthdayObject selected = birthdayList.getSelectedValue();
        removeButton.setEnabled(selected != null);
        editButton.setEnabled(selected != null);
    }

    public BirthdayObject[] getBirthdays() {
        DefaultListModel<BirthdayObject> model = (DefaultListModel<BirthdayObject>) birthdayList.getModel();
        Object[] o = model.toArray();
        BirthdayObject[] value = new BirthdayObject[o.length];
        System.arraycopy(o, 0, value,0, o.length);
        return value;
    }

    public JLabel headerLabel;
    public JScrollPane birthdayScrollPane;
    public JList<BirthdayObject> birthdayList;
    public JButton addButton, removeButton, editButton, closeButton;
}
