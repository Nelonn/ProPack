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

package me.nelonn.propack.cli.command;

import me.nelonn.propack.core.ProPackCore;
import me.nelonn.propack.core.builder.InternalProject;
import me.nelonn.propack.core.util.LogManagerCompat;
import org.apache.logging.log4j.Logger;
import picocli.CommandLine;

import java.io.File;

@CommandLine.Command(name = "build", description = "Build project")
public class BuildCommand implements Runnable {
    private static final Logger LOGGER = LogManagerCompat.getLogger();

    @CommandLine.Option(names = "--dir", description = "projects directory")
    String dir;

    @CommandLine.Parameters(index = "0", description = "project name")
    String name;

    @Override
    public void run() {
        File projectFile;
        if (dir == null) {
            projectFile = new File(name + File.separator + "project.json5");
        } else {
            projectFile = new File(dir, name + File.separator + "project.json5");
        }
        ProPackCore proPackCore = new ProPackCore(new File("./"));
        InternalProject internalProject = proPackCore.getProjectLoader().load(projectFile, false);
        internalProject.build();
    }
}
