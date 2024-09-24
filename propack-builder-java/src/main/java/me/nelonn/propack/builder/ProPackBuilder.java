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

package me.nelonn.propack.builder;

import me.nelonn.propack.core.util.LogManagerCompat;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Path;

public class ProPackBuilder {
    private static final Logger LOGGER = LogManagerCompat.getLogger();

    private final Path binaryPath;

    public ProPackBuilder(Path binaryPath) {
        this.binaryPath = binaryPath;
    }

    public void build() {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(binaryPath.toAbsolutePath().toString());
            processBuilder.redirectErrorStream(true);

            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                LOGGER.info(line);
            }

            process.waitFor();
        } catch (Throwable e) {
            throw new RuntimeException("Failed to build", e);
        }
    }

}
