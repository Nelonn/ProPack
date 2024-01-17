package me.nelonn.propack.bukkit;

import me.nelonn.flint.path.Path;
import me.nelonn.propack.ResourcePack;
import me.nelonn.propack.Resources;
import me.nelonn.propack.asset.Font;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.format.Style;
import org.bukkit.entity.Player;

public class ComponentPatcher {

    public static Component patch(Component input, Player player) {
        ResourcePack resourcePack = ProPack.getAppliedResourcePack(player);
        if (resourcePack == null) return input;
        Resources resources = resourcePack.resources();
        return patch(input, resources);
    }

    public static Component patch(Component input, Resources resources) {
        input = input.children(input.children().stream().map(component -> patchSingle(component, resources)).toList());
        if (input instanceof TranslatableComponent translatableComponent) {
            input = translatableComponent.arguments(translatableComponent
                    .arguments().stream()
                    .map(argument -> patchSingle(argument.asComponent(), resources))
                    .toList());
        }
        return input;
    }

    public static Component patchSingle(Component input, Resources resources) {
        Style style = input.style();
        Key fontKey = style.font();
        if (fontKey == null) return input;
        Path fontPath = Path.of(fontKey.namespace(), fontKey.value());
        Font font = resources.font(fontPath);
        if (font == null) return input;
        Path realPath = font.realPath();
        if (fontPath.equals(realPath)) return input;
        return input.style(style.font(Key.key(realPath.namespace(), realPath.value())));
    }

}
