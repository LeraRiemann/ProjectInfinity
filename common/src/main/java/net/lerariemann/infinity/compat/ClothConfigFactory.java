package net.lerariemann.infinity.compat;


import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.architectury.platform.Platform;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.impl.builders.SubCategoryBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.lerariemann.infinity.util.CommonIO;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.text.Text;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static net.lerariemann.infinity.InfinityMod.MOD_ID;
import static net.minecraft.client.resource.language.I18n.hasTranslation;

public class ClothConfigFactory {

    static void addEntry(AbstractConfigListEntry<?> newOption, Object category) {
        if (category instanceof ConfigCategory configCategory) {
            configCategory.addEntry(newOption);
        }
        else if (category instanceof SubCategoryBuilder subCategoryBuilder) {
            subCategoryBuilder.add(newOption);
        }
    }

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

    public static Screen create(Screen parent) {
        final var builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Text.translatable("config.infinity.title"));

        for (var field : readRootConfigJson().getAsJsonObject().entrySet()) {
            if (field.getValue().isJsonPrimitive()) {
                ConfigCategory category = builder.getOrCreateCategory(Text.translatable("config.infinity.title.general"));
                addElement(field, null, builder, null, category);
            }
            else {
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
        return builder.build();
    }

    static Text fieldName(Map.Entry<String, JsonElement> field, String category) {
        if (category.equals("general")) {
            category = "";
        }
        else category = category + ".";
        return Text.translatableWithFallback("config."+MOD_ID + "." + category + field.getKey(), fallback(field.getKey()));
    }

    @Environment(EnvType.CLIENT)
    static Text[] fieldTooltip(Map.Entry<String, JsonElement> field, String category, String nested) {
        if (category.equals("general")) {
            category = "";
        }
        else category = category + ".";
        if (!nested.isEmpty()) {
            nested += ".";
        }
        var translationKey = "config."+MOD_ID + "." + category + nested + field.getKey() + ".description";
        return createTooltip(translationKey).toArray(new Text[0]);
    }

    // Create a custom, potentially multi-line tooltip.
    @Environment(EnvType.CLIENT)
    public static List<Text> createTooltip(String loreKey) {
        //Setup list to store (potentially multi-line) tooltip.
        ArrayList<Text> lines = new ArrayList<>();
        int maxLength = 40;
        //Check if the key exists.
        if (!loreKey.isEmpty()) {
            //Translate the lore key.
            String translatedKey = I18n.translate(loreKey);
            //Check if the translated key exists.
            if (hasTranslation(loreKey)) {
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

    //Handles detection of when a line break should be added in a tooltip.
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

    // Enable and disable config elements.
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
        return Path.of(Platform.getConfigFolder() + "/infinity");
    }

    public static NbtCompound readRootConfigNbt() {
        return readNbt(configPath() + "/infinity.json");
    }

    public static JsonElement readRootConfigJson() {
        return readJson(configPath() + "/infinity.json");
    }

    public static NbtCompound readDefaultConfig() {
        Path tempfile = FabricLoader.getInstance().getModContainer(MOD_ID).orElse(null).getRootPaths().getFirst().resolve("config/infinity.json");
        try {
            Files.copy(tempfile, Path.of(configPath() + "/.infinity-default.json"), REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return readNbt(configPath()+("/.infinity-default.json"));
    }

    public static NbtCompound readNbt(String file) {
        File newFile = new File(file);
        String content;
        try {
            content = FileUtils.readFileToString(newFile, StandardCharsets.UTF_8);
            return StringNbtReader.parse(content);
        } catch (IOException | CommandSyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public static JsonElement readJson(String file) {
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