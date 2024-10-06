package net.lerariemann.infinity.config;


import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.lerariemann.infinity.util.CommonIO;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.text.Text;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static net.lerariemann.infinity.InfinityMod.MOD_ID;

public class ClothConfigFactory {
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
            category.addEntry(entryBuilder.startStrField(fieldName(field, currentCategory), value.getAsString())
                    .setSaveConsumer(mapSetter(field, prevKey, prevPrevKey))
                    .setDefaultValue((String) getDefaultValue(field, prevKey, prevPrevKey, "string"))
                    .build());
        }
        else if (value.isBoolean()) {
            category.addEntry(entryBuilder.startBooleanToggle(fieldName(field, currentCategory), value.getAsBoolean())
                    .setSaveConsumer(mapSetter(field, prevKey, prevPrevKey))
                    .setDefaultValue((boolean) getDefaultValue(field, prevKey, prevPrevKey, "boolean"))
                    .build());
        }
        else if (value.isNumber()) {
            if (value.getAsString().contains(".")) {
                float defaultValue = (float) getDefaultValue(field, prevKey, prevPrevKey, "float");
                //Some root chances do not exist in the JAR.
                if (defaultValue != 0.0f) {
                    category.addEntry(entryBuilder.startFloatField(fieldName(field, currentCategory), value.getAsFloat())
                            .setSaveConsumer(mapSetter(field, prevKey, prevPrevKey))
                            .setDefaultValue(defaultValue)
                            .build());
                }
                else {
                    category.addEntry(entryBuilder.startFloatField(fieldName(field, currentCategory), value.getAsFloat())
                            .setSaveConsumer(mapSetter(field, prevKey, prevPrevKey))
                            .build());
                }
            }
            else {
                if (!Objects.equals(field.getKey(), "infinity_version")) {
                    category.addEntry(entryBuilder.startIntField(fieldName(field, currentCategory), value.getAsInt())
                            .setSaveConsumer(mapSetter(field, prevKey, prevPrevKey))
                            .setDefaultValue((int) getDefaultValue(field, prevKey, prevPrevKey, "int"))
                            .build());
                }
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
        return builder.build();
    }

    public static Text fieldName(Map.Entry<String, JsonElement> field, String category) {
        if (Objects.equals(category, "general")) {
            category = "";
        }
        else category = category + ".";
        return Text.translatableWithFallback("config."+MOD_ID + "." + category + field.getKey(), fallback(field.getKey()));
    }

    // Enable and disable Easter Egg dimensions.
    public static <T> Consumer<T> mapSetter(Map.Entry<String, JsonElement> field, String prevField, String prevPrevField) {
        return t -> {
            NbtCompound rootConfig = readRootConfig();
            NbtCompound configPath = rootConfig;
            if (t != field.getValue()) {
                if (prevField != null) {
                    configPath = rootConfig.getCompound(prevField);
                }
                if (prevPrevField != null) {
                    configPath = rootConfig.getCompound(prevPrevField).getCompound(prevField);
                }

                if (t instanceof String) {
                    configPath.putString(field.getKey(), (String) t);
                }
                else if (t instanceof Boolean) {
                    configPath.putBoolean(field.getKey(), (boolean) t);
                }
                else if (t.toString().contains(".")) {
                    if (t instanceof Float) {
                        configPath.putFloat(field.getKey(), (float) t);
                    }
                }
                else if (t instanceof Integer) {
                    configPath.putInt(field.getKey(), (int) t);
                }


                CommonIO.write(rootConfig, configPath(), "infinity.json");
            }
        };
    }

    public static Object getDefaultValue(Map.Entry<String, JsonElement> field, String prevField, String prevPrevField, String type) {
        NbtCompound rootConfig = readDefaultConfig();
        NbtCompound configPath = rootConfig;
        if (prevField != null) {
            configPath = rootConfig.getCompound(prevField);
        }
        if (prevPrevField != null) {
            configPath = rootConfig.getCompound(prevPrevField).getCompound(prevField);
        }


        if (Objects.equals(type, "string")) {
            return configPath.getString(field.getKey());
        }
        else if (Objects.equals(type, "boolean")) {
            return configPath.getBoolean(field.getKey());
        }
        else if (Objects.equals(type, "float")) {
            return configPath.getFloat(field.getKey());
        }
        else if (Objects.equals(type, "int")) {
            return configPath.getInt(field.getKey());
        }
        return false;
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


    static Path configPath() {
        return Path.of(FabricLoader.getInstance().getConfigDir() + "/infinity");
    }

    public static NbtCompound readRootConfig() {
        return read(configPath() + "/infinity.json");
    }

    public static JsonElement readRootConfigJSON() {
        return readJSON(configPath() + "/infinity.json");
    }

    public static NbtCompound readDefaultConfig() {
        Path tempfile = FabricLoader.getInstance().getModContainer(MOD_ID).orElse(null).getRootPaths().getFirst().resolve("config/infinity.json");
            try {
                Files.copy(tempfile, Path.of(configPath() + "/infinity-default.json"), REPLACE_EXISTING);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }


        return read(configPath()+("/infinity-default.json"));
    }

    public static NbtCompound read(String file) {
        File newFile = new File(file);
        String content;
        try {
            content = FileUtils.readFileToString(newFile, StandardCharsets.UTF_8);
            return StringNbtReader.parse(content);
        } catch (IOException | CommandSyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public static NbtCompound read(Path file) {
        String content;
        try {
            content = FileUtils.readFileToString(file.toFile(), StandardCharsets.UTF_8);
            return StringNbtReader.parse(content);
        } catch (IOException | CommandSyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public static JsonElement readJSON(String file) {

        File newFile = new File(file);
        String content;
        try {
            content = FileUtils.readFileToString(newFile, StandardCharsets.UTF_8);
            return JsonParser.parseString(content);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}