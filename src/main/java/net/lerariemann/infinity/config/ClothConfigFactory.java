package net.lerariemann.infinity.config;


import com.google.gson.JsonElement;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import net.minecraft.client.gui.screen.Screen;
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

    public static void addElement(Map.Entry<String, JsonElement> field, Map.Entry<String, JsonElement> prevField, ConfigBuilder builder) {
        String currentCategory;
        if (prevField == null) currentCategory = "general";
        else currentCategory = prevField.getKey();

        var category = builder.getOrCreateCategory(Text.translatable("config.infinity.title." + currentCategory));
        var entryBuilder = builder.entryBuilder();

        if (field.getValue().getAsJsonPrimitive().isString()) {
            category.addEntry(entryBuilder.startStrField(fieldName(field, currentCategory), field.getValue().getAsString())
                    .build());
        }
        else if (field.getValue().getAsJsonPrimitive().isBoolean()) {
            category.addEntry(entryBuilder.startBooleanToggle(fieldName(field, currentCategory), field.getValue().getAsBoolean())
                    .build());
        }
        else if (field.getValue().getAsJsonPrimitive().isNumber()) {
            if (field.getValue().getAsJsonPrimitive().getAsString().contains(".")) {
                category.addEntry(entryBuilder.startFloatField(fieldName(field, currentCategory), field.getValue().getAsFloat())
                        .build());
            }
            else {
                category.addEntry(entryBuilder.startIntField(fieldName(field, currentCategory), field.getValue().getAsInt())
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
                addElement(field, null, builder);
            }
            else {
                for (var field2 : field.getValue().getAsJsonObject().entrySet()) {
                    if (field2.getValue().isJsonPrimitive()) {
                        addElement(field2, field, builder);

                    }
                    else {
                        for (var field3 : field2.getValue().getAsJsonObject().entrySet()) {
                            if (field3.getValue().isJsonPrimitive()) {
                                addElement(field3, field2, builder);
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
    public static <T> Consumer<T> mapSetter(Map.Entry<String, Object> field) {
        return t -> {
            String b = (String)t;
            field.setValue(b);
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