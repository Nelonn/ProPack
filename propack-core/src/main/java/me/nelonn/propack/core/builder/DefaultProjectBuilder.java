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

package me.nelonn.propack.core.builder;

import me.nelonn.propack.builder.Project;
import me.nelonn.propack.builder.ProjectBuilder;
import me.nelonn.propack.builder.task.Task;
import me.nelonn.propack.core.builder.task.*;
import me.nelonn.propack.core.util.LogManagerCompat;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.Set;

import static java.util.Objects.requireNonNull;

public class DefaultProjectBuilder implements ProjectBuilder {
    private static final Logger LOGGER = LogManagerCompat.getLogger();
    private final Project project;
    private final LinkedHashSet<Task> tasks = new LinkedHashSet<>();

    public DefaultProjectBuilder(@NotNull Project project) {
        this.project = project;
        tasks.add(new GatherSourcesTask(project));
        tasks.add(new ProcessModelsTask(project));
        tasks.add(new ProcessSoundsTask(project));
        tasks.add(new ProcessArmorTextures(project));
        tasks.add(new ProcessLanguagesTask(project));
        tasks.add(new ProcessFontsTask(project));
        if (project.getBuildConfiguration().getObfuscationConfiguration().isEnabled()) {
            tasks.add(new ObfuscateTask(project));
        }
        tasks.add(new SortAssetsTask(project));
        tasks.add(new PackageTask(project));
        tasks.add(new SerializeTask(project));
        if (project.getBuildConfiguration().getHosting() != null) {
            tasks.add(new UploadTask(project));
        }
    }

    @Override
    public @NotNull Set<Task> getTasks() {
        return tasks;
    }

    public LocalResourcePack build() {
        long startTimestamp = System.currentTimeMillis();
        DefaultTaskIO io = new DefaultTaskIO(new File(project.getBuildDir(), "temp"));
        try {
            for (Task task : tasks) {
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

            LocalResourcePack localResourcePack = new LocalResourcePack(project,
                    requireNonNull(io.getExtras().get(ProcessModelsTask.EXTRA_MAPPINGS_BUILDER)),
                    io.getAssets().getItemModels(),
                    io.getAssets().getSounds(),
                    io.getAssets().getArmorTextures(),
                    io.getAssets().getFonts(),
                    requireNonNull(io.getExtras().get(SerializeTask.EXTRA_FILE)),
                    requireNonNull(io.getExtras().get(PackageTask.EXTRA_ZIP)),
                    requireNonNull(io.getExtras().get(PackageTask.EXTRA_SHA1)),
                    io.getExtras().get(UploadTask.EXTRA_UPLOADED_PACK));

            LOGGER.info("BUILD SUCCESSFUL in {}s", (int) (System.currentTimeMillis() - startTimestamp) / 1000);

            return localResourcePack;
        } catch (TaskFailedException e) {
            LOGGER.error("BUILD FAILED in {}s", (int) (System.currentTimeMillis() - startTimestamp) / 1000);
            return null;
        }
    }

    public @NotNull Project getProject() {
        return project;
    }
}
