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

package me.nelonn.propack.cli.command;

import me.nelonn.propack.cli.CLI;
import me.nelonn.propack.core.util.JarResources;
import me.nelonn.propack.core.util.LogManagerCompat;
import org.slf4j.Logger;
import picocli.CommandLine;

import java.io.File;

@CommandLine.Command(name = "extractexample", description = "Extract example project")
public class ExtractExampleCommand implements Runnable {
    private static final Logger LOGGER = LogManagerCompat.getLogger();

    @CommandLine.Option(names = "--dir", description = "output directory")
    String dir;

    @Override
    public void run() {
        File directory = dir != null ? new File(dir) : new File("./");
        JarResources.extractDirectory(CLI.class, "resources/example/", new File(directory, "example"));
    }

}
