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

package me.nelonn.propack.core.builder;

import me.nelonn.propack.builder.Project;
import me.nelonn.propack.builder.ProjectBuilder;
import me.nelonn.propack.builder.task.Task;
import me.nelonn.propack.builder.task.TaskBootstrap;
import me.nelonn.propack.core.builder.task.*;
import me.nelonn.propack.core.util.LogManagerCompat;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.Set;

import static java.util.Objects.requireNonNull;

public class DefaultProjectBuilder implements ProjectBuilder {
    private static final Logger LOGGER = LogManagerCompat.getLogger();
    private final Project project;

    public DefaultProjectBuilder(@NotNull Project project) {
        this.project = project;
    }

    public BuiltResourcePack build() {
        long startTimestamp = System.currentTimeMillis();
        DefaultTaskIO io = new DefaultTaskIO(new File(project.getBuildDir(), "temp"));
        try {
            Set<Task> tasksInstances = new LinkedHashSet<>();

            try {
                for (TaskBootstrap bootstrap : project.getBuildConfiguration().getTasks()) {
                    tasksInstances.add(bootstrap.createTask(project));
                }
            } catch (Exception e) {
                throw new IllegalArgumentException("Unable to create instance of task", e);
            }

            for (Task task : tasksInstances) {
                try {
                    task.run(io);
                } catch (Exception e) {
                    LOGGER.info("Task {} FAILED", task);
                    if (e instanceof IllegalArgumentException) {
                        LOGGER.error(e.getMessage());
                    } else {
                        e.printStackTrace();
                    }
                    throw new TaskFailedException(task.toString());
                }
                LOGGER.info("Task {}", task);
            }

            BuiltResourcePack builtResourcePack = new BuiltResourcePack(project,
                    ResourcesCreator.create(
                            io.getAssets().getItemModels(),
                            io.getAssets().getSounds(),
                            io.getAssets().getArmorTextures(),
                            io.getAssets().getFonts(),
                            requireNonNull(io.getExtras().get(ProcessModelsTask.EXTRA_MESH_MAPPING_BUILDER)).build()
                    ),
                    requireNonNull(io.getExtras().get(SerializeTask.EXTRA_FILE)),
                    requireNonNull(io.getExtras().get(PackageTask.EXTRA_ZIP)),
                    requireNonNull(io.getExtras().get(PackageTask.EXTRA_SHA1)),
                    io.getExtras().get(UploadTask.EXTRA_UPLOADED_PACK));

            LOGGER.info("BUILD SUCCESSFUL in {}s", (int) (System.currentTimeMillis() - startTimestamp) / 1000);

            return builtResourcePack;
        } catch (TaskFailedException e) {
            LOGGER.error("BUILD FAILED in {}s", (int) (System.currentTimeMillis() - startTimestamp) / 1000);
            return null;
        }
    }

    @Override
    public @NotNull Project getProject() {
        return project;
    }
}
