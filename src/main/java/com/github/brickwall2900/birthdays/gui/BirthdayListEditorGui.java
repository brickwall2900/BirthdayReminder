package com.github.brickwall2900.birthdays.gui;

import com.github.brickwall2900.birthdays.BundleMultiplexer;
import com.github.brickwall2900.birthdays.Main;
import com.github.brickwall2900.birthdays.config.BirthdayNotifierConfig;
import com.github.brickwall2900.birthdays.config.object.BirthdayObject;
import org.httprpc.sierra.UILoader;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import static com.github.brickwall2900.birthdays.Main.IMAGE_ICON;

public class BirthdayListEditorGui extends JFrame {
    private static final ResourceBundle BUNDLE = new BundleMultiplexer(
            ResourceBundle.getBundle(BirthdayListEditorGui.class.getName()),
            BaseDialog.BUNDLE
    );

    public static final String TITLE = BUNDLE.getString("editor.title");
    public static final Dimension SIZE = new Dimension(640, 720);
    private static List<? extends RowSorter.SortKey> lastSortKeys;

    private BirthdayObjectTableModel tableModel;

    private String tableKeyTyped;
    private long tableKeyTypedTimestamp;

    public BirthdayListEditorGui(BirthdayObject[] objects) {
        setContentPane(UILoader.load(this, "birthdayList.xml", BUNDLE));

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

        birthdayTable.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                onKeyTypedTable(e);
            }
        });

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

        popupMenu = new JPopupMenu();
        JMenuItem editMenuItem = new JMenuItem(BUNDLE.getString("dialog.edit"), KeyEvent.VK_E);
        JMenuItem removeMenuItem = new JMenuItem(BUNDLE.getString("dialog.remove"), KeyEvent.VK_R);
        editMenuItem.addActionListener(this::onEditButtonPressed);
        removeMenuItem.addActionListener(this::onRemoveButtonPressed);
        popupMenu.add(editMenuItem);
        popupMenu.add(removeMenuItem);
        birthdayTable.setComponentPopupMenu(popupMenu);

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

    // why won't this work out of the box?
    private void onKeyTypedTable(KeyEvent e) {
        if (birthdayTable.isEditing()) {
            return; // don’t interfere with editing
        }

        if ((System.currentTimeMillis() - tableKeyTypedTimestamp) >= 500) {
            tableKeyTyped = "";
        }

        tableKeyTyped += Character.toLowerCase(e.getKeyChar());

        for (int i = 0; i < birthdayTable.getRowCount(); i++) {
            int modelIndex = birthdayTable.convertRowIndexToModel(i);
            BirthdayObject object = tableModel.getBirthdayObjects().get(modelIndex);

            if (object.name.toLowerCase().startsWith(tableKeyTyped)) {
                birthdayTable.getSelectionModel().setSelectionInterval(i, i);
                birthdayTable.scrollRectToVisible(birthdayTable.getCellRect(i, 0, true));
                break;
            }
        }

        tableKeyTypedTimestamp = System.currentTimeMillis();
    }

    private void onAddButtonPressed(ActionEvent e) {
        BirthdayEditorGui editBox = new BirthdayEditorGui(this);
        editBox.setVisible(true);
        // wait for user
        BirthdayObject object = editBox.getResult();
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
                    BUNDLE.getString("dialog.remove.confirm").formatted(selected.name),
                    BUNDLE.getString("dialog.remove"),
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

            BirthdayObject object = editBox.getResult();
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
        BirthdayNotifierConfig.Config config = notifierEditorGui.getResult();
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
        Main.destroyContainer(popupMenu);
        lastSortKeys = new ArrayList<>(birthdayTable.getRowSorter().getSortKeys());
        popupMenu = null;
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

    public JPopupMenu popupMenu;

    public JScrollPane birthdayScrollPane;
    public JTable birthdayTable;
    public JButton addButton, removeButton, editButton, configButton, closeButton;
    public JButton aboutButton;
}
