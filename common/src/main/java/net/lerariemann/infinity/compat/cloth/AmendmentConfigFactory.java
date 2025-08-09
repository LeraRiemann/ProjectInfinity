package net.lerariemann.infinity.compat.cloth;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.Requirement;
import me.shedaniel.clothconfig2.api.ValueHolder;
import me.shedaniel.clothconfig2.gui.entries.DropdownBoxEntry;
import me.shedaniel.clothconfig2.impl.builders.DropdownMenuBuilder;
import me.shedaniel.clothconfig2.impl.builders.SubCategoryBuilder;
import net.lerariemann.infinity.util.core.CommonIO;
import net.lerariemann.infinity.util.core.NbtUtils;
import net.minecraft.nbt.NbtCompound;
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
        int numAmendments = 0;
        for (JsonElement amendmentElement : elements) {
            numAmendments = AmendmentBuilder.addNew(builder, amendmentElement, numAmendments, amendmentCategory);
        }
        // todo new amendments
//        addAmendment(builder, null, i, amendmentCategory);
        amendmentCategory.addEntry(builder.entryBuilder().startTextDescription(Text.literal("To add new entries, edit amendments.json.").setStyle(Style.EMPTY)).build());
    }

    interface AmendmentUpdater<T> {
        boolean check(NbtCompound amendment, String key, T value);
        void update(NbtCompound amendment, String key, T value);

        AmendmentUpdater<String> ofString = new AmendmentUpdater<>() {
            @Override
            public boolean check(NbtCompound amendment, String key, String value) {
                return !Objects.equals(NbtUtils.getString(amendment, key), value);
            }

            @Override
            public void update(NbtCompound amendment, String key, String value) {
                amendment.putString(key, value);
            }
        };
        AmendmentUpdater<Double> ofDouble = new AmendmentUpdater<>() {
            @Override
            public boolean check(NbtCompound amendment, String key, Double value) {
                return NbtUtils.getDouble(amendment, key) != value;
            }

            @Override
            public void update(NbtCompound amendment, String key, Double value) {
                amendment.putDouble(key, value);
            }
        };
        AmendmentUpdater<List<String>> ofStringList = new AmendmentUpdater<>() {
            @Override
            public boolean check(NbtCompound amendment, String key, List<String> value) {
                // TODO Check if an amendment should be changed before writing
                return true;
            }
            @Override
            public void update(NbtCompound amendment, String key, List<String> value) {
                amendment.put(key, convertNbtList(value));
            }
        };
    }

    record StaticStringValueHolder(String s) implements ValueHolder<String> {
        @Override
        public String getValue() {
            return s;
        }
    }
    static ValueHolder<String> hold(String s) {
        return new StaticStringValueHolder(s);
    }

    static class AmendmentBuilder {
        ConfigBuilder builder;
        SubCategoryBuilder subCategory;
        int i;
        JsonObject amendment;

        AmendmentBuilder(ConfigBuilder builder, SubCategoryBuilder subCategory, int i, JsonObject amendment) {
            this.builder = builder;
            this.subCategory = subCategory;
            this.i = i;
            this.amendment = amendment;
        }

        static Requirement always = () -> true;

        void build() {
            var area = addStringDropdownOption("area", always,
                    Lists.newArrayList("blocks", "fluids", "items", "structures", "trees", "mobs"));
            addStringOption("mod", always);

            var selector = addStringDropdownOption("selector", always,
                    Lists.newArrayList("all", "matching", "matching_any", "matching_block_tag", "containing"));
            addListOption("matching",
                    Requirement.matches(selector, hold("matching_any")));
            addStringOption("matching",
                    Requirement.matches(selector, hold("matching")));
            addStringOption("matching",
                    Requirement.matches(selector, hold("matching_block_tag")));

            var results = addStringDropdownOption("results", always,
                    Lists.newArrayList("set_value", "set_field", "erase"));
            addDoubleOption("value",
                    Requirement.matches(results, hold("set_value")));
            addStringDropdownOption("field_name",
                    Requirement.all(Requirement.matches(area, hold("blocks")), Requirement.matches(results, hold("set_field"))),
                    Lists.newArrayList("full", "float", "top", "laggy"));
        }

        private void addStringOption(String name, Requirement req) {
            String current = getSafeString(amendment, name);
            subCategory.add(builder.entryBuilder().startStrField(
                            Text.translatable("config.infinity.amendments."+name),
                            current)
                    .setTooltip(amendmentTooltip(name))
                    .setDisplayRequirement(req)
                    .setSaveConsumer((value)-> amendmentSetter(name, value, i, AmendmentUpdater.ofString)).build());
        }

        private DropdownBoxEntry<String> addStringDropdownOption(String name, Requirement req, List<String> options) {
            String current = getSafeString(amendment, name);
            DropdownBoxEntry<String> res = builder.entryBuilder().startDropdownMenu(Text.translatable("config.infinity.amendments."+name), DropdownMenuBuilder.TopCellElementBuilder.of(current, (s) -> s))
                    .setTooltip(amendmentTooltip(name))
                    .setDisplayRequirement(req)
                    .setSaveConsumer((value) -> amendmentSetter(name, String.valueOf(value), i, AmendmentUpdater.ofString))
                    .setSelections(options).build();
            subCategory.add(res);
            return res;
        }

        private void addDoubleOption(String name, Requirement req) {
            Double current = getSafeDouble(amendment, name);
            subCategory.add(builder.entryBuilder().startDoubleField(
                            Text.translatable("config.infinity.amendments."+name),
                            current)
                    .setTooltip(amendmentTooltip(name))
                    .setDisplayRequirement(req)
                    .setSaveConsumer((value) -> amendmentSetter(name, value, i, AmendmentUpdater.ofDouble)).build());
        }

        private void addListOption(String name, Requirement req) {
            JsonElement list = amendment.get(name);
            List<String> current = (list == null || !list.isJsonArray()) ? List.of("") : convertNbtList(list.getAsJsonArray());
            if (!Objects.equals(current.getLast(), ""))
                current.add("");
            subCategory.add(builder.entryBuilder().startStrList(
                            Text.translatable("config.infinity.amendments."+name),
                            current)
                    .setTooltip(amendmentTooltip(name))
                    .setDisplayRequirement(req)
                    .setSaveConsumer((value) -> amendmentSetter(name, value, i, AmendmentUpdater.ofStringList)).build());
        }

        <T> void amendmentSetter(String name, T newValue, int amendmentIndex, AmendmentUpdater<T> updater) {
            NbtCompound elements = readNbt(configPath()+("/amendments.json"));
            NbtCompound amendmentNbt = elements.getList("elements", 10).getCompound(amendmentIndex);
            // Check if an amendment should be changed before writing
            if (updater.check(amendmentNbt, name, newValue)) {
                updater.update(amendmentNbt, name, newValue);
                CommonIO.write(elements, configPath(), "amendments.json");
                amendment = readJson(configPath()+("/amendments.json"))
                        .getAsJsonObject()
                        .getAsJsonArray("elements")
                        .get(amendmentIndex)
                        .getAsJsonObject();
            }
        }

        static int addNew(ConfigBuilder builder, JsonElement amendmentElement, int i, ConfigCategory amendmentCategory) {
            JsonObject amendment;
            SubCategoryBuilder subCategory;
            if (amendmentElement != null) {
                subCategory = builder.entryBuilder().startSubCategory(Text.translatable("config.infinity.amendment", String.valueOf(i)));
                amendment = amendmentElement.getAsJsonObject();
            } else {
                subCategory = builder.entryBuilder().startSubCategory(Text.translatable("config.infinity.amendment.new"));
                amendment = new JsonObject();
            }

            (new AmendmentBuilder(builder, subCategory, i, amendment)).build();
            amendmentCategory.addEntry(subCategory.build());
            i++;
            return i;
        }
    }

    private static String getSafeString(JsonObject amendment, String key) {
        var amend = amendment.get(key);
        if (amend != null && !amend.isJsonArray())
            return amend.getAsString();
        else return "";
    }
    private static Double getSafeDouble(JsonObject amendment, String key) {
        var amend = amendment.get(key);
        if (amend != null && !amend.isJsonArray())
            return amend.getAsDouble();
        else return 0d;
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
