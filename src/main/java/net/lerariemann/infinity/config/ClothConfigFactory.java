package net.lerariemann.infinity.config;


import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.lang.reflect.Field;
import java.util.function.Consumer;

import static net.lerariemann.infinity.InfinityMod.MOD_ID;

public class ClothConfigFactory {

    private static final ModConfig DEFAULT_VALUES = new ModConfig();

    public static Screen create(Screen parent) {
        final var builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Text.translatable("config.infinity.title"));

        final var entryBuilder = builder.entryBuilder();
        final var configInstance = ModConfig.get();
        final var generalCategory = builder.getOrCreateCategory(Text.translatable("config.infinity.title"));

        for (var field : ModConfig.class.getFields()) {

            if (field.getType() == boolean.class) {
                generalCategory.addEntry(entryBuilder.startBooleanToggle(fieldName(field), fieldGet(configInstance, field))
                        .setSaveConsumer(fieldSetter(configInstance, field))
                        .setDefaultValue((boolean) fieldGet(DEFAULT_VALUES, field)).build());

            }
            else if (field.getType() == String.class) {
                generalCategory.addEntry(entryBuilder.startStrField(fieldName(field), fieldGet(configInstance, field))
                        .setSaveConsumer(fieldSetter(configInstance, field))
                        .setDefaultValue((String) fieldGet(DEFAULT_VALUES, field)).build());
            }
            else if (field.getType() == int.class) {
                generalCategory.addEntry(entryBuilder.startIntField(fieldName(field), fieldGet(configInstance, field))
                        .setSaveConsumer(fieldSetter(configInstance, field))
                        .setDefaultValue((int) fieldGet(DEFAULT_VALUES, field)).build());
            }
        }
        builder.setSavingRunnable(ModConfig::save);
        return builder.build();
    }
    // Set a config field.
    public static <T> Consumer<T> fieldSetter(Object instance, Field field) {
        return t -> {
            try {
                field.set(instance, t);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        };
    }

    // Automatically generate translation keys for config options.
    public static Text fieldName(Field field) {
        return Text.translatable("config."+MOD_ID + "." + field.getName());
    }
    // Get the current value of a config field.
    @SuppressWarnings("unchecked")
    public static <T> T fieldGet(Object instance, Field field) {
        try {
            return (T) field.get(instance);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}