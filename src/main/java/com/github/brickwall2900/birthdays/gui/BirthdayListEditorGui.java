package com.github.brickwall2900.birthdays.gui;

import com.github.brickwall2900.birthdays.Main;
import com.github.brickwall2900.birthdays.config.BirthdayNotifierConfig;
import com.github.brickwall2900.birthdays.config.object.BirthdayObject;
import org.httprpc.sierra.UILoader;

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
import static com.github.brickwall2900.birthdays.TranslatableText.BUNDLE;
import static com.github.brickwall2900.birthdays.TranslatableText.text;

public class BirthdayListEditorGui extends JFrame {
    public static final String TITLE = text("editor.title");
    public static final Dimension SIZE = new Dimension(640, 720);
    private static List<? extends RowSorter.SortKey> lastSortKeys;

    private BirthdayObjectTableModel tableModel;

    public BirthdayListEditorGui(BirthdayObject[] objects) {
        setContentPane(UILoader.load(this, "/ui/birthdayList.xml", BUNDLE));

        birthdayTable = new JTable();
        birthdayScrollPane.setViewportView(birthdayTable);
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
        if (lastSortKeys != null) {
            tableSorter.setSortKeys(lastSortKeys);
        } else {
            List<RowSorter.SortKey> sortKeys = new ArrayList<>();
            sortKeys.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));
            tableSorter.setSortKeys(sortKeys);
        }

        closeButton.addActionListener(this::onCloseButtonPressed);
        addButton.addActionListener(this::onAddButtonPressed);
        removeButton.addActionListener(this::onRemoveButtonPressed);
        editButton.addActionListener(this::onEditButtonPressed);
        configButton.addActionListener(this::onConfigButtonPressed);
        birthdayTable.getSelectionModel().addListSelectionListener(this::onListSelectionChanged);
        aboutButton.addActionListener(this::onHelpButtonPressed);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                SwingUtilities.invokeLater(Main::save);
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
        editBox.destroy();
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
            editBox.destroy();
        }
    }

    private void onConfigButtonPressed(ActionEvent e) {
        BirthdayNotifierEditorGui notifierEditorGui = new BirthdayNotifierEditorGui(this, BirthdayNotifierConfig.globalConfig);
        notifierEditorGui.setVisible(true);
        // wait for user
        BirthdayNotifierConfig.Config config = notifierEditorGui.toConfig();
        if (config != null) {
            BirthdayNotifierConfig.globalConfig = config;
        }
        notifierEditorGui.destroy();
    }

    private void onListSelectionChanged(ListSelectionEvent e) {
        BirthdayObject selected = getSelected();
        removeButton.setEnabled(selected != null);
        editButton.setEnabled(selected != null);
    }

    private void onHelpButtonPressed(ActionEvent e) {
        BirthdayReminderAboutDialog dialog = new BirthdayReminderAboutDialog(this);
        dialog.setVisible(true);
        dialog.destroy();
    }

    public BirthdayObject[] getBirthdays() {
        return tableModel.getBirthdayObjects().toArray(new BirthdayObject[0]);
    }

    public void destroy() {
        Main.destroyContainer(this);
        Main.destroyContainer(getContentPane());
        lastSortKeys = new ArrayList<>(birthdayTable.getRowSorter().getSortKeys());
        birthdayTable = null;
        birthdayScrollPane = null;
        addButton = null;
        removeButton = null;
        editButton = null;
        configButton = null;
        closeButton = null;
        aboutButton = null;
        tableModel.destroy();
        tableModel = null;
    }

    public JScrollPane birthdayScrollPane;
    public JTable birthdayTable;
    public JButton addButton, removeButton, editButton, configButton, closeButton;
    public JButton aboutButton;
}
