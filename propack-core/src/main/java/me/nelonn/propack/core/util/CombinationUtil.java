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

package me.nelonn.propack.core.util;

import org.jetbrains.annotations.NotNull;

import java.util.*;

public final class CombinationUtil {
    // Test
    public static void main(String[] args) {
        Map<String, List<String>> slots = new HashMap<>();
        List<String> slot1 = new ArrayList<>();
        slot1.add("A_1");
        slot1.add("A_2");
        slot1.add("A_3");
        slots.put("s1", slot1);
        List<String> slot2 = new ArrayList<>();
        slot2.add("BB_1");
        slot2.add("BB_2");
        slot2.add("BB_3");
        slots.put("s2", slot2);
        List<String> slot3 = new ArrayList<>();
        slot3.add("CCC_1");
        slot3.add("CCC_2");
        slot3.add("CCC_3");
        slots.put("s3", slot3);

        List<Map<String, String>> combinations = generateSlotCombinations(slots);
        List<String> out = new ArrayList<>();
        for (Map<String, String> combination : combinations) {
            out.add("s1:" + combination.get("s1") + "&s2:" + combination.get("s2") + "&s3:" + combination.get("s3"));
        }
        System.out.println(out);
        System.out.println(generateAllCombinations(new String[]{"AAA_1", "BB_1", "C_1"}));
    }

    public static @NotNull List<List<String>> generateAllCombinations(@NotNull String[] args) {
        return generateAllCombinations(Arrays.asList(args));
    }

    public static @NotNull List<List<String>> generateAllCombinations(@NotNull Collection<String> args) {
        List<List<String>> output = new LinkedList<>();
        for (int i = 1; i <= args.size(); i++) {
            output.addAll(combination(args, i));
        }
        return output;
    }

    public static <T> List<List<T>> combination(Collection<T> values, int size) {
        if (size == 0) {
            return Collections.singletonList(Collections.emptyList());
        }

        if (values.isEmpty()) {
            return Collections.emptyList();
        }

        List<List<T>> combination = new LinkedList<>();

        T actual = values.iterator().next();

        List<T> subSet = new LinkedList<>(values);
        subSet.remove(actual);

        List<List<T>> subSetCombination = combination(subSet, size - 1);

        for (List<T> set : subSetCombination) {
            List<T> newSet = new LinkedList<>(set);
            newSet.add(0, actual);
            combination.add(newSet);
        }

        combination.addAll(combination(subSet, size));

        return combination;
    }

    public static <T> @NotNull List<Map<String, T>> generateSlotCombinations(@NotNull Map<String, List<T>> slots) {
        List<Map<String, T>> output = new ArrayList<>();
        if (slots.isEmpty()) return output;
        slotCombinations(slots, output, slots.keySet().stream().findFirst().get(), new HashMap<>());
        return output;
    }

    private static <T> void slotCombinations(Map<String, List<T>> slots, List<Map<String, T>> output,
                                             String previousSlot, Map<String, T> previousCombination) {
        List<String> keySet = new ArrayList<>(slots.keySet());
        if (keySet.indexOf(previousSlot) >= keySet.size()) return;
        List<T> elements = new ArrayList<>(slots.get(previousSlot));
        elements.add(null);
        for (T element : elements) {
            Map<String, T> combination = new HashMap<>(previousCombination);
            combination.put(previousSlot, element);
            if ((keySet.indexOf(previousSlot) + 1) == keySet.size()) {
                output.add(combination);
            } else {
                slotCombinations(slots, output, keySet.get(keySet.indexOf(previousSlot) + 1), combination);
            }
        }
    }

    private CombinationUtil() {
        throw new UnsupportedOperationException();
    }

}