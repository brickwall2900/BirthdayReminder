package com.github.brickwall2900.birthdays.gui;

import com.github.brickwall2900.birthdays.BirthdaysManager;
import com.github.brickwall2900.birthdays.Main;
import com.github.brickwall2900.birthdays.config.BirthdayNotifierConfig;
import com.github.brickwall2900.birthdays.config.object.BirthdayObject;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;

import static com.github.brickwall2900.birthdays.Main.IMAGE_ICON;
import static com.github.brickwall2900.birthdays.TranslatableText.text;
import static org.httprpc.sierra.UIBuilder.*;

public class BirthdayListEditorGui extends JFrame {
    public static final String TITLE = text("editor.title");
    public static final Dimension SIZE = new Dimension(640, 720);
    public static final int BORDER = 8;

    public BirthdayListEditorGui(BirthdayObject[] objects) {
        buildContentPane();

        DefaultListModel<BirthdayObject> defaultListModel = new DefaultListModel<>();
        defaultListModel.addAll(Arrays.asList(objects));
        birthdayList.setModel(defaultListModel);
        closeButton.addActionListener(this::onCloseButtonPressed);
        addButton.addActionListener(this::onAddButtonPressed);
        removeButton.addActionListener(this::onRemoveButtonPressed);
        editButton.addActionListener(this::onEditButtonPressed);
        configButton.addActionListener(this::onConfigButtonPressed);
        birthdayList.addListSelectionListener(this::onListSelectionChanged);
        sortByComboBox.addActionListener(this::onSortSelectionChanged);

        sortByComboBox.setRenderer(new EnumToTextListCellRenderer<>("editor.sortBy.value"));
        sortByComboBox.setSelectedItem(BirthdayNotifierConfig.applicationConfig.sortByOption);

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
                        cell(headerLabel = new JLabel(text("editor.header"))),
                        glue(),
                        cell(sortByLabel = new JLabel(text("editor.sortBy"))),
                        cell(sortByComboBox = new JComboBox<>(new DefaultComboBoxModel<>(SortByOption.values()))).weightBy(1)),
                cell(birthdayScrollPane = new JScrollPane(birthdayList = new JList<>())).weightBy(1),
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
            DefaultListModel<BirthdayObject> model = (DefaultListModel<BirthdayObject>) birthdayList.getModel();
            model.addElement(object);
        }
    }

    private void onRemoveButtonPressed(ActionEvent e) {
        DefaultListModel<BirthdayObject> model = (DefaultListModel<BirthdayObject>) birthdayList.getModel();
        BirthdayObject selected = birthdayList.getSelectedValue();
        if (selected != null) {
            if (JOptionPane.showConfirmDialog(this,
                    text("dialog.remove.confirm", selected.name), text("dialog.remove"),
                    JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                model.removeElement(selected);
            }
        }
    }

    private void onEditButtonPressed(ActionEvent e) {
        BirthdayObject selected = birthdayList.getSelectedValue();
        BirthdayEditorGui editBox = new BirthdayEditorGui(this, selected);
        editBox.setVisible(true);
        // wait for user

        BirthdayObject object = editBox.toBirthday();
        if (object != null) {
            DefaultListModel<BirthdayObject> model = (DefaultListModel<BirthdayObject>) birthdayList.getModel();
            int index = birthdayList.getSelectedIndex();
            model.set(index, object);
        }
    }

    private void onConfigButtonPressed(ActionEvent e) {
        BirthdayNotifierEditorGui notifierEditorGui = new BirthdayNotifierEditorGui(this, BirthdayNotifierConfig.globalConfig);
        notifierEditorGui.setVisible(true);
        // wait for user
        BirthdayNotifierConfig.globalConfig = notifierEditorGui.toConfig();
    }

    private void onListSelectionChanged(ListSelectionEvent e) {
        BirthdayObject selected = birthdayList.getSelectedValue();
        removeButton.setEnabled(selected != null);
        editButton.setEnabled(selected != null);
    }

    private void onSortSelectionChanged(ActionEvent e) {
        SortByOption option = (SortByOption) Objects.requireNonNull(sortByComboBox.getSelectedItem(), "null?");
        sortList(option);
        BirthdayNotifierConfig.applicationConfig.sortByOption = option;
    }

    public void sortList(SortByOption option) {
        DefaultListModel<BirthdayObject> model = (DefaultListModel<BirthdayObject>) birthdayList.getModel();
        BirthdayObject[] objects = getBirthdays(); // get all elements of the list first
        Arrays.sort(objects, option.comparator); // sort
        model.clear();
        model.addAll(Arrays.asList(objects)); // clear list then add sorted elements
    }

    public BirthdayObject[] getBirthdays() {
        DefaultListModel<BirthdayObject> model = (DefaultListModel<BirthdayObject>) birthdayList.getModel();
        Object[] o = model.toArray(); // will be BirthdayObject[] trust me
        BirthdayObject[] value = new BirthdayObject[o.length];
        System.arraycopy(o, 0, value,0, o.length);
        return value;
    }

    public JLabel headerLabel;
    public JLabel sortByLabel;
    public JComboBox<SortByOption> sortByComboBox;
    public JScrollPane birthdayScrollPane;
    public JList<BirthdayObject> birthdayList;
    public JButton addButton, removeButton, editButton, configButton, closeButton;

    public enum SortByOption {
        // idk what I'm doing here
        // I swear I'm not high on drugs huhu ;-;
        NAME(SortByOption::sortByName),
        DAYS(SortByOption::sortByDays),
        DATE(SortByOption::sortByDate),
        AGE(SortByOption::sortByDate);

        final Comparator<BirthdayObject> comparator;

        SortByOption(Comparator<BirthdayObject> comparator) {
            this.comparator = comparator;
        }

        private static int sortByName(BirthdayObject obj, BirthdayObject other) {
            return obj.name.compareTo(other.name);
        }

        private static int sortByDays(BirthdayObject obj, BirthdayObject other) {
            long days = BirthdaysManager.getDaysBeforeBirthday(obj);
            long otherDays = BirthdaysManager.getDaysBeforeBirthday(other);
            return Long.compare(days, otherDays);
        }

        // this is pretty redundant ;-;
        // dates sort the exact same way as ages
        // the older the date, the older the age
        // remove?
        private static int sortByAge(BirthdayObject obj, BirthdayObject other) {
            long months = BirthdaysManager.getAge(obj).toTotalMonths();
            long otherMonths = BirthdaysManager.getAge(other).toTotalMonths();
            return Long.compare(otherMonths, months);
        }

        private static int sortByDate(BirthdayObject obj, BirthdayObject other) {
            return obj.date.compareTo(other.date);
        }
    }
}
