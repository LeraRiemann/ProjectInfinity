package net.lerariemann.infinity.compat.cloth;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.impl.builders.DropdownMenuBuilder;
import me.shedaniel.clothconfig2.impl.builders.SubCategoryBuilder;
import net.lerariemann.infinity.util.core.CommonIO;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

import java.util.*;

import static net.lerariemann.infinity.compat.cloth.ClothConfigFactory.*;

public class AmendmentConfigFactory {

    public static void build(ConfigBuilder builder) {


        ConfigCategory amendmentCategory = builder.getOrCreateCategory(Text.translatable("config.infinity.title.amendments"));
        JsonObject amendmentList = readJson(configPath()+("/amendments.json")).getAsJsonObject();
        var elements = amendmentList.getAsJsonArray("elements");
        int i = 0;
        for (JsonElement amendmentElement : elements) {
            i = addAmendment(builder, amendmentElement, i, amendmentCategory);
        }
        // todo new amendments
//        addAmendment(builder, null, i, amendmentCategory);
        amendmentCategory.addEntry(builder.entryBuilder().startTextDescription(Text.literal("To add new entries, edit amendments.json.").setStyle(Style.EMPTY)).build());
    }

    private static int addAmendment(ConfigBuilder builder, JsonElement amendmentElement, int i, ConfigCategory amendmentCategory) {
        JsonObject amendment;
        SubCategoryBuilder subCategory;
        if (amendmentElement != null) {
            subCategory = builder.entryBuilder().startSubCategory(Text.translatable("config.infinity.amendment", String.valueOf(i)));
            amendment = amendmentElement.getAsJsonObject();
        } else {
            subCategory = builder.entryBuilder().startSubCategory(Text.translatable("config.infinity.amendment.new"));
            amendment = new JsonObject();
        }
        addStringDropdownOption("area", builder, subCategory, i, amendment, Lists.newArrayList("blocks", "fluids", "items", "structures", "trees", "mobs"));
        addStringOption("mod", builder, subCategory, i, amendment);
        addStringDropdownOption("selector", builder, subCategory, i, amendment, Lists.newArrayList("all", "matching", "matching_any", "matching_block_tag", "containing"));
        if (getSafeString(amendment, "selector").equals("matching_any"))
            addListOption("matching", builder, subCategory, i, amendment);
        else if (getSafeString(amendment, "selector").equals("matching")) {
            addStringOption("matching", builder, subCategory, i, amendment);
        }
        addStringDropdownOption("results", builder, subCategory, i, amendment, Lists.newArrayList("set_value", "set_field", "erase"));
        if (getSafeString(amendment, "results").equals("set_value"))
            addDoubleOption("value", builder, subCategory, i, amendment);
        else if (getSafeString(amendment, "results").equals("set_field"))
            addStringDropdownOption("field_name", builder, subCategory, i, amendment, Lists.newArrayList("full", "float", "top", "laggy"));

        amendmentCategory.addEntry(subCategory.build());
        i++;
        return i;
    }

    private static String getSafeString(JsonObject amendment, String key) {
        var amend = amendment.get(key);
        if (amend != null)
            return amend.getAsString();
        else return "";
    }

    private static void addStringOption(String name, ConfigBuilder builder, SubCategoryBuilder subCategory, int i, JsonObject amendment) {
        String current = getSafeString(amendment, name);
        subCategory.add(builder.entryBuilder().startStrField(
                        Text.translatable("config.infinity.amendments."+name),
                        current)
                .setTooltip(amendmentTooltip(name))
                .setSaveConsumer((value)-> amendmentSetter(name, value, i)).build());

    }

    private static void addStringDropdownOption(String name, ConfigBuilder builder, SubCategoryBuilder subCategory, int i, JsonObject amendment, List<String> options) {
        String current = getSafeString(amendment, name);
        subCategory.add(builder.entryBuilder().startDropdownMenu(Text.translatable("config.infinity.amendments."+name), DropdownMenuBuilder.TopCellElementBuilder.of(current, (s) -> s))
                .setTooltip(amendmentTooltip(name))
                .setSaveConsumer((value)-> amendmentSetter(name, String.valueOf(value), i))
                .setSelections(options).build());
    }

    private static void addDoubleOption(String name, ConfigBuilder builder, SubCategoryBuilder subCategory, int i, JsonObject amendment) {
        subCategory.add(builder.entryBuilder().startDoubleField(
                        Text.translatable("config.infinity.amendments."+name),
                        amendment.get(name).getAsDouble())
                .setTooltip(amendmentTooltip(name))
                .setSaveConsumer((value)-> amendmentSetter(name, value, i)).build());
    }

    private static void addListOption(String name, ConfigBuilder builder, SubCategoryBuilder subCategory, int i, JsonObject amendment) {
        var list = convertNbtList(amendment.get(name).getAsJsonArray());
        if (!Objects.equals(list.getLast(), ""))
            list.add("");
        subCategory.add(builder.entryBuilder().startStrList(
                        Text.translatable("config.infinity.amendments."+name),
                        list)
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
        if (Objects.equals(newValue.getLast(), ""))
            newValue.removeLast();
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
