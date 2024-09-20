package com.github.brickwall2900.birthdays.gui;

import javax.swing.*;

import java.awt.*;

import static com.github.brickwall2900.birthdays.TranslatableText.text;

public class EnumToTextListCellRenderer<T extends Enum<?>> extends DefaultListCellRenderer {
        private final String key;

        public EnumToTextListCellRenderer(String key) {
            this.key = key;
        }

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value != null) {
                label.setText(text(key + '.' + value));
            }
            return label;
        }
    }