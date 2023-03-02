/*
 * This file is part of ProPack, a Minecraft resource pack toolkit
 * Copyright (C) Nelonn <two.nelonn@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.nelonn.propack.module;

import me.nelonn.propack.core.ProPackCore;

public abstract class JavaModule implements Module { // TODO: module loading system
    private boolean enabled = false;
    private ProPackCore core = null;

    @Override
    public final void enable() {
        if (enabled) return;
        enabled = true;
        onEnable();
    }

    protected void onEnable() {
    }

    @Override
    public final void disable() {
        if (!enabled) return;
        enabled = false;
        onDisable();
    }

    protected void onDisable() {
    }

    @Override
    public void setEnabled(boolean enabled) {
        if (this.enabled != enabled) {
            this.enabled = enabled;
            if (this.enabled) {
                onEnable();
            } else {
                onDisable();
            }
        }
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
