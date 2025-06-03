package net.lerariemann.infinity.compat.cloth;


import com.google.gson.*;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.architectury.platform.Platform;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.impl.builders.SubCategoryBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.util.core.CommonIO;
import net.lerariemann.infinity.util.InfinityMethods;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtString;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.text.Text;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static net.lerariemann.infinity.InfinityMod.MOD_ID;
import static net.lerariemann.infinity.InfinityMod.rootConfigPathInJar;
import static net.lerariemann.infinity.util.InfinityMethods.formatAsTitleCase;
import static net.minecraft.client.resource.language.I18n.hasTranslation;

public class ClothConfigFactory {

    /**
     * Add a config entry to the screen.
     */
    static void addEntry(AbstractConfigListEntry<?> newOption, Object category) {
        if (category instanceof ConfigCategory configCategory) {
            configCategory.addEntry(newOption);
        }
        else if (category instanceof SubCategoryBuilder subCategoryBuilder) {
            subCategoryBuilder.add(newOption);
        }
    }

    /**
     * Create a config entry from JSON. This will then be added to the screen via addEntry.
     */
    static void addElement(Map.Entry<String, JsonElement> field, Map.Entry<String, JsonElement> prevField, ConfigBuilder builder, Map.Entry<String, JsonElement> prevPrevField, Object category) {
        String currentCategory;
        String nestedCurrentCategory = "";
        if (prevField == null) {
            currentCategory = "general";
        }
        else {
            currentCategory = prevField.getKey();
            nestedCurrentCategory = "";
        }

        if (prevPrevField != null) {
            currentCategory = prevPrevField.getKey();
            assert prevField != null;
            nestedCurrentCategory = prevField.getKey();
        }

        JsonPrimitive value = field.getValue().getAsJsonPrimitive();
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();
        String prevKey = null;
        String prevPrevKey = null;
        if (prevField != null) {
            prevKey = prevField.getKey();
        }
        if (prevPrevField != null) {
            prevPrevKey = prevPrevField.getKey();
        }


        if (value.isString()) {
            var newOption = entryBuilder.startStrField(fieldName(field, currentCategory), value.getAsString())
                    .setSaveConsumer(mapSetter(field, prevKey, prevPrevKey))
                    .setTooltip(fieldTooltip(field, currentCategory, nestedCurrentCategory))
                    .setDefaultValue((String) getDefaultValue(field, prevKey, prevPrevKey, "string"))
                    .build();
            addEntry(newOption, category);


        }
        else if (value.isBoolean()) {
            var newOption = entryBuilder.startBooleanToggle(fieldName(field, currentCategory), value.getAsBoolean())
                    .setSaveConsumer(mapSetter(field, prevKey, prevPrevKey))
                    .setDefaultValue((boolean) getDefaultValue(field, prevKey, prevPrevKey, "boolean"))
                    .setTooltip(fieldTooltip(field, currentCategory, nestedCurrentCategory))
                    .build();
            addEntry(newOption, category);
        }
        else if (value.isNumber()) {
            if (value.getAsString().contains(".")) {
                double defaultValue = (double) getDefaultValue(field, prevKey, prevPrevKey, "double");
                //Some root chances do not exist in the JAR.
                if (defaultValue != 0.0f) {
                    var newOption = entryBuilder.startDoubleField(fieldName(field, currentCategory), value.getAsDouble())
                            .setSaveConsumer(mapSetter(field, prevKey, prevPrevKey))
                            .setDefaultValue(defaultValue)
                            .setTooltip(fieldTooltip(field, currentCategory, nestedCurrentCategory))
                            .build();
                    addEntry(newOption, category);
                }
                else {
                    var newOption = entryBuilder.startDoubleField(fieldName(field, currentCategory), value.getAsDouble())
                            .setSaveConsumer(mapSetter(field, prevKey, prevPrevKey))
                            .setTooltip(fieldTooltip(field, currentCategory, nestedCurrentCategory))
                            .build();
                    addEntry(newOption, category);
                }

            }
            else {
                if (!field.getKey().equals("infinity_version")) {
                    var newOption = entryBuilder.startIntField(fieldName(field, currentCategory), value.getAsInt())
                            .setSaveConsumer(mapSetter(field, prevKey, prevPrevKey))
                            .setTooltip(fieldTooltip(field, currentCategory, nestedCurrentCategory))
                            .setDefaultValue((int) getDefaultValue(field, prevKey, prevPrevKey, "int"))
                            .build();
                    addEntry(newOption, category);
                }
            }
        }

    }

    /**
     * Create a Cloth Config screen.
     */
    public static Screen create(Screen parent) {
        final var builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Text.translatable("config.infinity.title"));
        JsonObject infinityJson = readRootConfigJson().getAsJsonObject();
        for (var field : infinityJson.entrySet()) {
            if (field.getValue().isJsonPrimitive()) {
                ConfigCategory category = builder.getOrCreateCategory(Text.translatable("config.infinity.title.general"));
                addElement(field, null, builder, null, category);
            }
            else if (field.getValue().isJsonObject()) {
                for (var field2 : field.getValue().getAsJsonObject().entrySet()) {
                    if (field2.getValue().isJsonPrimitive()) {
                        ConfigCategory category = builder.getOrCreateCategory(Text.translatable("config.infinity.title." + field.getKey()));
                        addElement(field2, field, builder, null, category);

                    }
                    else {
                        ConfigCategory category = builder.getOrCreateCategory(Text.translatable("config.infinity.title."+field.getKey()));
                        SubCategoryBuilder subCategory = builder.entryBuilder().startSubCategory(Text.translatable("config.infinity.title."+field2.getKey()));

                        for (var field3 : field2.getValue().getAsJsonObject().entrySet()) {
                            if (field3.getValue().isJsonPrimitive()) {
                                addElement(field3, field2, builder, field, subCategory);
                            }
                        }
                        category.addEntry(subCategory.build());
                    }
                }
            }
        }
        EasterConfigFactory.build(builder, infinityJson);
        AmendmentConfigFactory.build(builder);
        return builder.build();
    }

    /**
     * Create a translatable text name for a config entry.
     */
    static Text fieldName(Map.Entry<String, JsonElement> field, String category) {
        if (category.equals("general")) {
            category = "";
        }
        else category = category + ".";
        return Text.translatableWithFallback("config."+MOD_ID + "." + category + field.getKey(), formatAsTitleCase(field.getKey()));
    }

    /**
     * Create a translatable text tooltip for a config entry.
     */
    @Environment(EnvType.CLIENT)
    static Text[] fieldTooltip(Map.Entry<String, JsonElement> field, String category, String nested) {
        if (category.equals("general")) {
            category = "";
        }
        else category = category + ".";
        if (!nested.isEmpty()) {
            nested += ".";
        }
        var translationKey = "config.%s.%s%s%s.description".formatted(MOD_ID, category, nested, field.getKey());
        return createTooltip(translationKey).toArray(new Text[0]);
    }

    public static List<Text> createTooltip(String loreKey) {
        return createTooltip(loreKey, true);
    }

    /**
     * Create a custom, potentially multi-line tooltip.
     * This code was adapted from Item Descriptions, as
     * Cloth Config's tooltip wrapper can sometimes cause a crash.
    */
    @Environment(EnvType.CLIENT)
    public static List<Text> createTooltip(String loreKey, boolean translate) {
        //Setup list to store (potentially multi-line) tooltip.
        ArrayList<Text> lines = new ArrayList<>();
        int maxLength = 40;
        //Check if the key exists.
        if (!loreKey.isEmpty()) {
            //Translate the lore key.
            String translatedKey = I18n.translate(loreKey);
            //Check if the translated key exists.
            if (hasTranslation(loreKey) || (!translate)) {
                //Check if custom wrapping should be used.
                //Any tooltip longer than XX characters should be shortened.
                while (translatedKey.length() >= maxLength) {
                    //Find how much to shorten the tooltip by.
                    int index = getIndex(translatedKey, maxLength);
                    //Add a shortened tooltip.
                    lines.add(Text.literal(translatedKey.substring(0, index)));
                    //Remove the shortened tooltip substring from the tooltip. Repeat.
                    translatedKey = translatedKey.substring(index);
                }
                //Add the final tooltip.
                lines.add(Text.literal(translatedKey));
            }
        }
        return lines;
    }

    /**
    * Handles detection of when a line break should be added in a tooltip.
     */
    public static int getIndex(String translatedKey, int maxLength) {
        String subKey = translatedKey.substring(0, maxLength);
        int index;
        //Find the last space character in the substring, if not, default to the length of the substring.
        if (subKey.contains(" ")) {
            index = subKey.lastIndexOf(" ")+1;
        }
        else index = maxLength;
        return index;
    }

    /**
     * Enable and disable config elements.
     */
    static <T> Consumer<T> mapSetter(Map.Entry<String, JsonElement> field, String prevField, String prevPrevField) {
        return t -> {
            NbtCompound rootConfig = readRootConfigNbt();
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
                    if (t instanceof Double) {
                        configPath.putDouble(field.getKey(), (double) t);
                    }
                }
                else if (t instanceof Integer) {
                    configPath.putInt(field.getKey(), (int) t);
                }


                CommonIO.write(rootConfig, configPath(), "infinity.json");
            }
        };
    }

    /**
     * Check a config entry's default value.
     */
    static Object getDefaultValue(Map.Entry<String, JsonElement> field, String prevField, String prevPrevField, String type) {
        NbtCompound rootConfig = readDefaultConfig();
        NbtCompound configPath = rootConfig;
        if (prevField != null) {
            configPath = rootConfig.getCompound(prevField);
        }
        if (prevPrevField != null) {
            configPath = rootConfig.getCompound(prevPrevField).getCompound(prevField);
        }

        return switch (type) {
            case "string" -> configPath.getString(field.getKey());
            case "boolean" -> configPath.getBoolean(field.getKey());
            case "double" -> configPath.getDouble(field.getKey());
            case "int" -> configPath.getInt(field.getKey());
            default -> false;
        };
    }

    static Path configPath() {
        return Path.of(Platform.getConfigFolder() + "/infinity");
    }

    public static NbtCompound readRootConfigNbt() {
        return readNbt(configPath() + "/infinity.json");
    }

    public static JsonElement readRootConfigJson() {
        return readJson(configPath() + "/infinity.json");
    }

    public static NbtCompound readDefaultConfig() {
        Path tempfile = rootConfigPathInJar.resolve("infinity.json");
        try {
            Files.copy(tempfile, Path.of(configPath() + "/.infinity-default.json"), REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return readNbt(configPath()+("/.infinity-default.json"));
    }

    /**
     * Reads a given file and converts it to a NbtCompound.
     */
    public static NbtCompound readNbt(String file) {
        try {
            return StringNbtReader.parse(readConfig(file));
        } catch (CommandSyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Reads a config file, and returns a string with its contents.
     */
    public static String readConfig(String file) {
        File newFile = new File(file);
        try {
            return FileUtils.readFileToString(newFile, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Reads a given file and converts it to JSON.
     */
    public static JsonElement readJson(String file) {
        return JsonParser.parseString(readConfig(file));
    }

}