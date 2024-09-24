package com.github.brickwall2900.birthdays.gui;

import com.github.brickwall2900.birthdays.BirthdaysManager;
import com.github.brickwall2900.birthdays.config.object.BirthdayObject;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableRowSorter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static com.github.brickwall2900.birthdays.TranslatableText.text;

public class BirthdayObjectTableModel extends AbstractTableModel {
    public static final int COL_NAMES = 0;
    public static final int COL_DATES = 1;
    public static final int COL_AGE = 2;
    public static final int COL_DAYS = 3;
    public static final int COL_COUNT = 4;
    private List<BirthdayObject> birthdayObjects = new ArrayList<>();

    public List<BirthdayObject> getBirthdayObjects() {
        return birthdayObjects;
    }

    public void addBirthday(BirthdayObject birthday) {
        birthdayObjects.add(birthday);
    }

    public BirthdayObject getBirthday(int index) {
        return birthdayObjects.get(index);
    }

    public void setBirthday(int index, BirthdayObject birthday) {
        birthdayObjects.set(index, birthday);
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (aValue instanceof BirthdayObject) {
            birthdayObjects.set(rowIndex, (BirthdayObject) aValue);
        }
    }

    public void removeBirthday(BirthdayObject birthday) {
        birthdayObjects.remove(birthday);
    }

    public void setBirthdayObjects(List<BirthdayObject> objects) {
        birthdayObjects = objects;
    }

    @Override
    public int getRowCount() {
        return birthdayObjects.size();
    }

    @Override
    public int getColumnCount() {
        return COL_COUNT;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (birthdayObjects.size() > rowIndex) {
            BirthdayObject object = birthdayObjects.get(rowIndex);

            // table debugging
            boolean tableDebugging = Boolean.getBoolean("table.debugging");
            return switch (columnIndex) {
                case COL_NAMES -> object.name + (tableDebugging ? "[%d]".formatted(rowIndex) : "");
                case COL_DATES -> object.date;
                case COL_AGE -> BirthdaysManager.getAgeInYears(object);
                case COL_DAYS -> BirthdaysManager.getDaysBeforeBirthday(object);
                default -> text("editor.table.unknown");
            };
        } else {
            return null;
        }
    }

    @Override
    public String getColumnName(int column) {
        return switch (column) {
            case COL_NAMES -> text("editor.table.column.names");
            case COL_DATES -> text("editor.table.column.dates");
            case COL_AGE -> text("editor.table.column.age");
            case COL_DAYS -> text("editor.table.column.days");
            default -> super.getColumnName(column);
        };
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return switch (columnIndex) {
            case COL_NAMES -> String.class;
            case COL_DATES -> LocalDate.class;
            case COL_AGE -> Integer.class;
            case COL_DAYS -> Long.class;
            default -> super.getColumnClass(columnIndex);
        };
    }

    public static class BirthdayTableSorter extends TableRowSorter<BirthdayObjectTableModel> {
        public BirthdayTableSorter(BirthdayObjectTableModel model) {
            super(model);
            setComparator(COL_NAMES, BirthdayTableSorter::sortByName);
            setComparator(COL_DATES, BirthdayTableSorter::sortByDate);
            setComparator(COL_AGE, BirthdayTableSorter::sortByAge);
            setComparator(COL_DAYS, BirthdayTableSorter::sortByDays);
        }

        private static int sortByName(Object obj, Object other) {
            if (obj instanceof String b1 && other instanceof String b2) {
                return b1.compareTo(b2);
            } else {
                return 0;
            }
        }

        private static int sortByDays(Object obj, Object other) {
            if (obj instanceof Long days && other instanceof Long otherDays) {
                return Long.compare(days, otherDays);
            } else {
                return 0;
            }
        }

        private static int sortByDate(Object obj, Object other) {
            if (obj instanceof LocalDate b1 && other instanceof LocalDate b2) {
                return b1.compareTo(b2);
            } else {
                return 0;
            }
        }

        private static int sortByAge(Object obj, Object other) {
            if (obj instanceof Integer b1 && other instanceof Integer b2) {
                return b1.compareTo(b2);
            } else {
                return 0;
            }
        }
    }
}
