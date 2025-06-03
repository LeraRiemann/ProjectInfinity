package net.lerariemann.infinity.compat.cloth;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.util.InfinityMethods;
import net.lerariemann.infinity.util.core.CommonIO;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtString;
import net.minecraft.text.Text;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import static net.lerariemann.infinity.compat.cloth.ClothConfigFactory.*;


public class EasterConfigFactory {
    public static void build(ConfigBuilder builder, JsonObject infinityJson) {
        ConfigCategory easterCategory = builder.getOrCreateCategory(Text.translatable("config.infinity.title.easter"));
        JsonArray currentConfig = infinityJson.getAsJsonArray("disabledDimensions");
        JsonArray defaultConfig = readJson(configPath()+("/.infinity-default.json")).getAsJsonObject().getAsJsonArray("disabledDimensions");
        Stream<String> sorted = InfinityMod.provider.easterizer.map.keySet().stream().sorted();
        for (String name : sorted.toList()) {
            var defaultsDisabled = isEasterEggDisabled(defaultConfig, name);
            easterCategory.addEntry(builder.entryBuilder().startBooleanToggle(
                            Text.of(InfinityMethods.formatAsTitleCase(name)), isEasterEggDisabled(currentConfig, name))
                    .setSaveConsumer(newValue -> easterSetter(name, newValue))
                    .setTooltip(easterTooltip(defaultsDisabled))
                    .setDefaultValue(defaultsDisabled).build());
        }
    }

    static void easterSetter(String name, boolean newValue) {
        NbtCompound rootConfig = readRootConfigNbt();
        // If a dimension should be enabled...
        if (newValue) {
            // and it is currently disabled (in the list of disabled dimensions)
            if (rootConfig.getList("disabledDimensions", 8).contains(NbtString.of(name))) {
                // remove it from the list of disabled dimensions.
                rootConfig.getList("disabledDimensions", 8).remove(NbtString.of(name));

                CommonIO.write(rootConfig, configPath(), "infinity.json");
            }
        }
        // If a dimension should be disabled...
        else {
            if (!rootConfig.getList("disabledDimensions", 8).contains(NbtString.of(name))) {
                // remove it from the list of disabled dimensions.
                rootConfig.getList("disabledDimensions", 8).add(NbtString.of(name));

                CommonIO.write(rootConfig, configPath(), "infinity.json");
            }
        }
    }

    static boolean isEasterEggDisabled(JsonArray array, String name) {
        boolean result = false;
        for (JsonElement disabled : array) {
            if (Objects.equals(disabled.getAsString(), name)) {
                result = true;
            }
        }
        return !result;
    }

    static Optional<Text[]> easterTooltip(boolean enabled) {
        if (!enabled)
            return Optional.of(createTooltip("config.infinity.easter.disabled.description").toArray(new Text[0]));
        else return Optional.empty();
    }
}
