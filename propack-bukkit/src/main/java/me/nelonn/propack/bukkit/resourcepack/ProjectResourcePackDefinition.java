package me.nelonn.propack.bukkit.resourcepack;

import me.nelonn.propack.ResourcePack;
import me.nelonn.propack.builder.Project;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class ProjectResourcePackDefinition implements ResourcePackDefinition {
    private final Project project;

    public ProjectResourcePackDefinition(@NotNull Project project) {
        this.project = project;
    }

    @Override
    public @NotNull String getName() {
        return project.getName();
    }

    @Override
    public @NotNull Optional<ResourcePack> getResourcePack() {
        return project.getResourcePack();
    }

    public @NotNull Project getProject() {
        return project;
    }
}
