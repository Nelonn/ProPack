/*
 * This file is part of ProPack, a Minecraft resource pack toolkit
 * Copyright (C) Michael Neonov <two.nelonn@gmail.com>
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

package me.nelonn.propack.asset;

import me.nelonn.flint.path.Key;
import me.nelonn.flint.path.Path;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class SlotItemModel extends MultiItemModel {
    private final Map<String, Slot> slots;

    public SlotItemModel(@NotNull Path path, @NotNull Set<Key> targetItems,
                            @NotNull Path baseMesh, @NotNull Map<String, Slot> slots) {
        super(path, targetItems, baseMesh);
        this.slots = Collections.unmodifiableMap(slots);
    }

    public @NotNull Path getMesh() {
        return getBaseMesh();
    }

    public @NotNull Path getMesh(Map<String, String> slots) {
        for (Map.Entry<String, String> slotElement : slots.entrySet()) {
            String slotKey = slotElement.getKey();
            if (slotKey == null) continue;
            Slot slot = this.slots.get(slotKey);
            if (slot == null) {
                throw new IllegalArgumentException(this + " does not contain slot '" + slotKey + "'");
            }
            String value = slotElement.getValue();
            if (value == null || value.isEmpty()) {
                continue;
            }
            if (!slot.hasEntry(value)) {
                throw new IllegalArgumentException(this + " slot '" + slotElement.getKey() + "' does not contain entry '" + slotElement.getValue() + "'");
            }
        }
        StringBuilder sb = new StringBuilder();
        boolean empty = true;
        for (Slot slot : getSlots()) {
            if (sb.length() > 0) {
                sb.append('&');
            }
            sb.append(slot.getName()).append(':');
            String element = slots.get(slot.getName());
            if (element != null && !element.isEmpty()) {
                sb.append(element);
                empty = false;
                break;
            }
        }
        if (empty) return getBaseMesh();
        String hex = Integer.toHexString(sb.toString().hashCode());
        return Path.of(getBaseMesh().namespace(), getBaseMesh().value() + '-' + hex);
    }

    public @NotNull Collection<Slot> getSlots() {
        return slots.values().stream().sorted().collect(Collectors.toList());
    }

    public @Nullable Slot getSlot(@NotNull String name) {
        return slots.get(name);
    }

    public boolean hasSlot(@NotNull String name) {
        return slots.containsKey(name);
    }

    public static class Slot implements Iterable<String>, Comparable<Slot> {
        private final String name;
        private final Set<String> entries;

        public Slot(@NotNull String name, @NotNull Set<String> entries) {
            this.name = name;
            this.entries = Collections.unmodifiableSet(entries);
        }

        public @NotNull String getName() {
            return name;
        }

        public @NotNull Set<String> getEntries() {
            return entries;
        }

        public boolean hasEntry(@NotNull String value) {
            return entries.contains(value);
        }

        @NotNull
        @Override
        public Iterator<String> iterator() {
            return entries.iterator();
        }

        @Override
        public int compareTo(@NotNull SlotItemModel.Slot o) {
            return o.getName().compareTo(getName());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Slot strings = (Slot) o;
            return Objects.equals(name, strings.name) && Objects.equals(entries, strings.entries);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, entries);
        }
    }
}
