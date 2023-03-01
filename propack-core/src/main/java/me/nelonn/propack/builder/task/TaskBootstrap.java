package me.nelonn.propack.builder.task;

import me.nelonn.propack.builder.Project;
import me.nelonn.propack.builder.task.Task;
import org.jetbrains.annotations.NotNull;

public interface TaskBootstrap {

    @NotNull Task createTask(@NotNull Project project);

}
