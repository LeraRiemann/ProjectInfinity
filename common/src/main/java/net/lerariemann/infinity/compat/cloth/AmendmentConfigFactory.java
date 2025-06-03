package net.lerariemann.infinity.compat.cloth;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.impl.builders.DropdownMenuBuilder;
import me.shedaniel.clothconfig2.impl.builders.SubCategoryBuilder;
import net.lerariemann.infinity.util.core.CommonIO;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.Registries;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

import java.util.*;
import java.util.stream.Collectors;

import static net.lerariemann.infinity.compat.cloth.ClothConfigFactory.*;

public class AmendmentConfigFactory {
    public static void build(ConfigBuilder builder) {
        ConfigCategory amendmentCategory = builder.getOrCreateCategory(Text.translatable("config.infinity.title.amendments"));
        JsonObject amendmentList = readJson(configPath()+("/amendments.json")).getAsJsonObject();
        var elements = amendmentList.getAsJsonArray("elements");
        int i = 0;
        for (JsonElement amendmentElement : elements) {
            SubCategoryBuilder subCategory = builder.entryBuilder().startSubCategory(Text.translatable("config.infinity.amendment", String.valueOf(i)));
            JsonObject amendment = amendmentElement.getAsJsonObject();
            addStringOption("area", builder, subCategory, i, amendment);
            addStringOption("mod", builder, subCategory, i, amendment);
            addStringOption("selector", builder, subCategory, i, amendment);
            if (amendment.get("selector").getAsString().equals("matching_any"))
                addListOption("matching", builder, subCategory, i, amendment);
            else if (amendment.get("selector").getAsString().equals("matching")) {
                addStringOption("matching", builder, subCategory, i, amendment);
            }
            addStringOption("results", builder, subCategory, i, amendment);
            if (amendment.get("results").getAsString().equals("set_value"))
                addDoubleOption("value", builder, subCategory, i, amendment);
            else if (amendment.get("results").getAsString().equals("set_field"))
                addStringOption("field_name", builder, subCategory, i, amendment);

            amendmentCategory.addEntry(subCategory.build());
            i++;
        }
        amendmentCategory.addEntry(builder.entryBuilder().startTextDescription(Text.literal("To add new entries, edit amendments.json.").setStyle(Style.EMPTY)).build());
    }

    private static void addStringOption(String name, ConfigBuilder builder, SubCategoryBuilder subCategory, int i, JsonObject amendment) {
        String current;
        if (amendment.get(name) == null) current = "";
        else current =  amendment.get(name).getAsString();
        subCategory.add(builder.entryBuilder().startStrField(
                        Text.translatable("config.infinity.amendments."+name),
                        current)
                .setTooltip(amendmentTooltip(name))
                .setSaveConsumer((value)-> amendmentSetter(name, value, i)).build());

    }

    private static void addDoubleOption(String name, ConfigBuilder builder, SubCategoryBuilder subCategory, int i, JsonObject amendment) {
        subCategory.add(builder.entryBuilder().startDoubleField(
                        Text.translatable("config.infinity.amendments."+name),
                        amendment.get(name).getAsDouble())
                .setTooltip(amendmentTooltip(name))
                .setSaveConsumer((value)-> amendmentSetter(name, value, i)).build());

    }

    private static void addListOption(String name, ConfigBuilder builder, SubCategoryBuilder subCategory, int i, JsonObject amendment) {
        subCategory.add(builder.entryBuilder().startStrList(
                        Text.translatable("config.infinity.amendments."+name),
                        convertNbtList(amendment.get(name).getAsJsonArray()))
                .setTooltip(amendmentTooltip(name))
                .setSaveConsumer((value)-> amendmentSetter(name, value, i)).build());

    }




    static void amendmentSetter(String name, String newValue, int amendmentIndex) {
        NbtCompound elements = readNbt(configPath()+("/amendments.json"));
        NbtCompound amendment = elements.getList("elements", 10).getCompound(amendmentIndex);
        // Check if an amendment should be changed before writing
        if (!Objects.equals(amendment.getString(newValue), newValue)) {
            amendment.putString(name, newValue);
            CommonIO.write(elements, configPath(), "amendments.json");
        }
    }

    static void amendmentSetter(String name, Double newValue, int amendmentIndex) {
        NbtCompound elements = readNbt(configPath()+("/amendments.json"));
        NbtCompound amendment = elements.getList("elements", 10).getCompound(amendmentIndex);
        // Check if an amendment should be changed before writing
        if (amendment.getDouble(name) != newValue) {
            amendment.putDouble(name, newValue);
            CommonIO.write(elements, configPath(), "amendments.json");
        }
    }

    static void amendmentSetter(String name, List<String> newValue, int amendmentIndex) {
        NbtCompound elements = readNbt(configPath()+("/amendments.json"));
        NbtCompound amendment = elements.getList("elements", NbtElement.COMPOUND_TYPE).getCompound(amendmentIndex);
        // TODO Check if an amendment should be changed before writing
        amendment.put(name, convertNbtList(newValue));
        CommonIO.write(elements, configPath(), "amendments.json");

    }

    static NbtList convertNbtList(List<String> list) {
        var nbtList = new NbtList();
        for (String s : list) {
            nbtList.add(NbtString.of(s));
        }
        return nbtList;
    }

    static List<String> convertNbtList(NbtList nbtList) {
        ArrayList<String> list = new ArrayList<>();
        for (NbtElement s : nbtList) {
            list.add(s.asString());
        }
        return list;
    }

    static List<String> convertNbtList(JsonArray nbtList) {
        ArrayList<String> list = new ArrayList<>();
        for (JsonElement s : nbtList) {
            list.add(s.getAsString());
        }
        return list;
    }

    static Optional<Text[]> amendmentTooltip(String option) {
        return Optional.of(createTooltip("config.infinity.amendments."+option+".description").toArray(new Text[0]));
    }
}
