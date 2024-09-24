package com.github.brickwall2900.birthdays.gui;

import com.github.brickwall2900.birthdays.Main;
import com.github.brickwall2900.birthdays.config.BirthdayNotifierConfig;
import com.github.brickwall2900.birthdays.config.object.BirthdayObject;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.github.brickwall2900.birthdays.Main.IMAGE_ICON;
import static com.github.brickwall2900.birthdays.TranslatableText.text;
import static org.httprpc.sierra.UIBuilder.*;

public class BirthdayListEditorGui extends JFrame {
    public static final String TITLE = text("editor.title");
    public static final Dimension SIZE = new Dimension(640, 720);
    public static final int BORDER = 8;

    private final BirthdayObjectTableModel tableModel;

    public BirthdayListEditorGui(BirthdayObject[] objects) {
        buildContentPane();

        tableModel = new BirthdayObjectTableModel();
        tableModel.setBirthdayObjects(new ArrayList<>(Arrays.asList(objects)));
        birthdayTable.setAutoCreateRowSorter(false);
        birthdayTable.setModel(tableModel);
        birthdayTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        birthdayTable.setColumnSelectionAllowed(false);
        birthdayTable.setRowSelectionAllowed(true);
        birthdayTable.getTableHeader().setReorderingAllowed(false);

        BirthdayObjectTableModel.BirthdayTableSorter tableSorter = new BirthdayObjectTableModel.BirthdayTableSorter(tableModel);
        birthdayTable.setRowSorter(tableSorter);
        tableSorter.setSortsOnUpdates(true);

        // sort by names on startup
        List<RowSorter.SortKey> sortKeys = new ArrayList<>();
        sortKeys.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));
        tableSorter.setSortKeys(sortKeys);

        closeButton.addActionListener(this::onCloseButtonPressed);
        addButton.addActionListener(this::onAddButtonPressed);
        removeButton.addActionListener(this::onRemoveButtonPressed);
        editButton.addActionListener(this::onEditButtonPressed);
        configButton.addActionListener(this::onConfigButtonPressed);
        birthdayTable.getSelectionModel().addListSelectionListener(this::onListSelectionChanged);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                Main.save();
            }
        });

        removeButton.setEnabled(false);
        editButton.setEnabled(false);

        setIconImage(IMAGE_ICON);
        setTitle(TITLE);
        setSize(SIZE);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    private void buildContentPane() {
        JPanel contentPane = column(4,
                row(4,
                        cell(headerLabel = new JLabel(text("editor.header")))),
//                cell(birthdayScrollPane = new JScrollPane(birthdayList = new JList<>())).weightBy(1),
                cell(birthdayScrollPane = new JScrollPane(birthdayTable = new JTable())).weightBy(1),
                row(4,
                        cell(addButton = new JButton(text("dialog.add"))),
                        cell(removeButton = new JButton(text("dialog.remove"))),
                        cell(editButton = new JButton(text("dialog.edit"))),
                        cell(configButton = new JButton(text("dialog.notify.config"))),
                        glue(),
                        cell(closeButton = new JButton(text("dialog.close"))))).getComponent();
        contentPane.setBorder(BorderFactory.createEmptyBorder(BORDER, BORDER, BORDER, BORDER));
        setContentPane(contentPane);
    }

    private void onCloseButtonPressed(ActionEvent e) {
        // simulate closing a window
        // closing events won't work without this
        dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
    }

    private void onAddButtonPressed(ActionEvent e) {
        BirthdayEditorGui editBox = new BirthdayEditorGui(this);
        editBox.setVisible(true);
        // wait for user
        BirthdayObject object = editBox.toBirthday();
        if (object != null) {
            tableModel.addBirthday(object);
            tableModel.fireTableDataChanged();
        }
    }

    private BirthdayObject getSelected() {
        int row = birthdayTable.getSelectedRow();
        if (row != -1) {
            row = birthdayTable.convertRowIndexToModel(row);
            return tableModel.getBirthday(row);
        }
        return null;
    }

    private void onRemoveButtonPressed(ActionEvent e) {
        BirthdayObject selected = getSelected();
        if (selected != null) {
            if (JOptionPane.showConfirmDialog(this,
                    text("dialog.remove.confirm", selected.name), text("dialog.remove"),
                    JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                birthdayTable.clearSelection();
                tableModel.removeBirthday(selected);
                tableModel.fireTableDataChanged();
            }
        }
    }

    private void onEditButtonPressed(ActionEvent e) {
        int selectedRow = birthdayTable.getSelectedRow();
        selectedRow = birthdayTable.convertRowIndexToModel(selectedRow);
        BirthdayObject selected = getSelected();
        if (selected != null) {
            BirthdayEditorGui editBox = new BirthdayEditorGui(this, selected);
            editBox.setVisible(true);
            // wait for user

            BirthdayObject object = editBox.toBirthday();
            if (object != null) {
                tableModel.setBirthday(selectedRow, object);
                tableModel.fireTableRowsUpdated(selectedRow, selectedRow);
            }
        }
    }

    private void onConfigButtonPressed(ActionEvent e) {
        BirthdayNotifierEditorGui notifierEditorGui = new BirthdayNotifierEditorGui(this, BirthdayNotifierConfig.globalConfig);
        notifierEditorGui.setVisible(true);
        // wait for user
        BirthdayNotifierConfig.globalConfig = notifierEditorGui.toConfig();
    }

    private void onListSelectionChanged(ListSelectionEvent e) {
        BirthdayObject selected = getSelected();
        removeButton.setEnabled(selected != null);
        editButton.setEnabled(selected != null);
    }

    public BirthdayObject[] getBirthdays() {
        return tableModel.getBirthdayObjects().toArray(new BirthdayObject[0]);
    }

    public JLabel headerLabel;
    public JScrollPane birthdayScrollPane;
    public JTable birthdayTable;
    public JButton addButton, removeButton, editButton, configButton, closeButton;
}
