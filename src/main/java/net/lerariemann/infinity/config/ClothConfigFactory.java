package net.lerariemann.infinity.config;


import com.google.gson.JsonElement;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import net.lerariemann.infinity.util.CommonIO;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import static net.lerariemann.infinity.InfinityMod.MOD_ID;
import static net.lerariemann.infinity.config.ModConfig.*;

public class ClothConfigFactory {

    private static final ModConfig DEFAULT_VALUES = new ModConfig();

    public static void addString(Map.Entry<String, JsonElement> field) {

    }

    public static void addElement(Map.Entry<String, JsonElement> field, Map.Entry<String, JsonElement> prevField, ConfigBuilder builder, Map.Entry<String, JsonElement> prevPrevField) {
        String currentCategory;
        if (prevField == null) currentCategory = "general";
        else currentCategory = prevField.getKey();
        var value = field.getValue().getAsJsonPrimitive();

        var category = builder.getOrCreateCategory(Text.translatable("config.infinity.title." + currentCategory));
        var entryBuilder = builder.entryBuilder();
        String prevKey = null;
        String prevPrevKey = null;
        if (prevField != null) {
            prevKey = prevField.getKey();
        }
        if (prevPrevField != null) {
            prevPrevKey = prevPrevField.getKey();
        }

        if (value.isString()) {
            category.addEntry(entryBuilder.startStrField(fieldName(field, currentCategory), value.getAsString()).setSaveConsumer(mapSetter(field, prevKey, prevPrevKey))
                    .build());
        }
        else if (value.isBoolean()) {
            category.addEntry(entryBuilder.startBooleanToggle(fieldName(field, currentCategory), value.getAsBoolean())
                    .build());
        }
        else if (value.isNumber()) {
            if (value.getAsString().contains(".")) {
                category.addEntry(entryBuilder.startFloatField(fieldName(field, currentCategory), value.getAsFloat())
                        .build());
            }
            else {
                category.addEntry(entryBuilder.startIntField(fieldName(field, currentCategory), value.getAsInt())
                        .build());
            }
        }
    }

    public static Screen create(Screen parent) {
        final var builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Text.translatable("config.infinity.title"));

        for (var field : readRootConfigJSON().getAsJsonObject().entrySet()) {
            if (field.getValue().isJsonPrimitive()) {
                addElement(field, null, builder, null);
            }
            else {
                for (var field2 : field.getValue().getAsJsonObject().entrySet()) {
                    if (field2.getValue().isJsonPrimitive()) {
                        addElement(field2, field, builder, null);

                    }
                    else {
                        for (var field3 : field2.getValue().getAsJsonObject().entrySet()) {
                            if (field3.getValue().isJsonPrimitive()) {
                                addElement(field3, field2, builder, field);
                            }
                        }
                    }
                }
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
    public static Text fieldName(Map.Entry field, String category) {
        if (Objects.equals(category, "general")) {
            category = "";
        }
        else category = category + ".";
        return Text.translatableWithFallback("config."+MOD_ID + "." + category + field.getKey(), fallback(field.getKey().toString()));
    }

    // Enable and disable Easter Egg dimensions.
    public static <T> Consumer<T> mapSetter(Map.Entry<String, JsonElement> field, String prevField, String prevPrevField) {
        return t -> {
            if (t != field.getValue()) {
                NbtCompound rootConfig = readRootConfig();
                if (prevField != null) {
                    rootConfig = rootConfig.getCompound(prevField);
                }
                if (prevPrevField != null) {
                    rootConfig = rootConfig.getCompound(prevPrevField);
                }
                if (t instanceof String) {
                    rootConfig.putString(field.getKey(), (String) t);
                }
                if (t instanceof Boolean) {
                    rootConfig.putBoolean(field.getKey(), (boolean) t);
                }
                if (t instanceof Integer) {
                    rootConfig.putInt(field.getKey(), (int) t);
                }
                CommonIO.write(rootConfig, configPath(), "infinity.json");
            }
        };
    }


    //Create fallbacks for config options without translations.
    public static String fallback(String text) {
        text = text.replace("_", " ");
        //i am sure java has a smarter way to do title case, but this works too
        StringBuilder newText = new StringBuilder();
        int i = 0;
        for (Character c : text.toCharArray()) {
            if (i == 0) {
                c = c.toString().toUpperCase().charAt(0);
            }
            newText.append(c);
            i++;
            if (c == ' ') {
                i = 0;
            }
        }
        return newText.toString();
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