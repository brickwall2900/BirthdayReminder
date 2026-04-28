/*
 * Copyright 2026 brickwall2900
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.brickwall2900.birthdays.systray.dorkbox;

import com.github.brickwall2900.birthdays.systray.TrayCheckboxMenuItem;

import javax.swing.*;

public class DorkboxTrayCheckboxMenuitem extends DorkboxTrayMenuItem implements TrayCheckboxMenuItem {

    public DorkboxTrayCheckboxMenuitem() {
        super(new JCheckBoxMenuItem());
    }

    @Override
    public boolean isChecked() {
        return menuItem.isSelected();
    }

    @Override
    public void setChecked(boolean state) {
        menuItem.setSelected(state);
    }
}
