package net.lerariemann.infinity.compat.cloth;

import com.google.common.collect.Lists;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.Requirement;
import me.shedaniel.clothconfig2.api.ValueHolder;
import me.shedaniel.clothconfig2.gui.entries.DropdownBoxEntry;
import me.shedaniel.clothconfig2.gui.entries.SubCategoryListEntry;
import me.shedaniel.clothconfig2.impl.builders.DropdownMenuBuilder;
import me.shedaniel.clothconfig2.impl.builders.SubCategoryBuilder;
import net.lerariemann.infinity.util.core.CommonIO;
import net.lerariemann.infinity.util.core.NbtUtils;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.text.Text;

import java.util.*;

import static net.lerariemann.infinity.compat.cloth.ClothConfigFactory.*;

public class AmendmentConfigFactory {
    public static void build(ConfigBuilder builder) {
        AmendmentConfigFactory factory = new AmendmentConfigFactory(builder);
        builder.setSavingRunnable(factory::save);
    }

    Map<Integer, NbtCompound> amendments = new HashMap<>();
    boolean edited = false;
    ConfigCategory amendmentCategory;
    ConfigBuilder builder;

    AmendmentConfigFactory(ConfigBuilder builder) {
        this.builder = builder;

        NbtList list = readNbt(configPath()+"/amendments.json").getList("elements", 10);
        int N = list.size();
        for (int i = 0; i < N; i++) if (list.get(i) instanceof NbtCompound e) {
            amendments.put(i, e);
        }

        //adding amendments from the disk
        this.amendmentCategory = builder.getOrCreateCategory(Text.translatable("config.infinity.title.amendments"));
        for (int i = 0; i < N; i++) {
            amendmentCategory.addEntry(AmendmentBuilder.getNew(this, i));
        }

        //"button" to show new amendments
        var moreAmendmentsField = builder.entryBuilder()
                .startIntField(Text.translatable("config.infinity.amendments.addmore"), 0)
                .setTooltip(amendmentTooltip("addmore"))
                .setMax(10)
                .build();
        amendmentCategory.addEntry(moreAmendmentsField);

        //empty fields to add new amendments
        for (int i = N; i < N + 10; i++) {
            amendments.put(i, new NbtCompound());
            amendmentCategory.addEntry(AmendmentBuilder.getShadow(this, i, moreAmendmentsField, i - N));
        }
    }

    void save() {
        if (!edited) return;
        NbtList elements = new NbtList();
        var keys = amendments.keySet().stream().sorted().toList();
        for (int k: keys) {
            NbtCompound value = amendments.get(k);
            if (!value.isEmpty()) elements.add(value);
        }
        NbtCompound res = new NbtCompound();
        res.putInt("amendment_version", (int)((System.currentTimeMillis() - 1754769333185L)/1000));
        res.put("elements", elements);
        CommonIO.write(res, configPath(), "amendments.json");
    }

    interface AmendmentUpdater<T> {
        boolean check(NbtCompound amendment, String key, T value);
        NbtCompound update(NbtCompound amendment, String key, T value);

        AmendmentUpdater<String> ofString = new AmendmentUpdater<>() {
            @Override
            public boolean check(NbtCompound amendment, String key, String value) {
                return !Objects.equals(NbtUtils.getString(amendment, key), value);
            }

            @Override
            public NbtCompound update(NbtCompound amendment, String key, String value) {
                amendment.putString(key, value);
                return amendment;
            }
        };
        AmendmentUpdater<Double> ofDouble = new AmendmentUpdater<>() {
            @Override
            public boolean check(NbtCompound amendment, String key, Double value) {
                return NbtUtils.getDouble(amendment, key) != value;
            }

            @Override
            public NbtCompound update(NbtCompound amendment, String key, Double value) {
                amendment.putDouble(key, value);
                return amendment;
            }
        };
        AmendmentUpdater<List<String>> ofStringList = new AmendmentUpdater<>() {
            @Override
            public boolean check(NbtCompound amendment, String key, List<String> value) {
                var list = NbtUtils.getList(amendment, key, NbtElement.STRING_TYPE);
                StringBuilder a = new StringBuilder();
                StringBuilder b = new StringBuilder();
                for (NbtElement s: list) a.append(s.asString());
                for (String s: value) b.append(s);
                return !Objects.equals(a.toString(), b.toString());
            }
            @Override
            public NbtCompound update(NbtCompound amendment, String key, List<String> value) {
                amendment.put(key, convertNbtList(value));
                return amendment;
            }
        };
    }

    static Requirement always = () -> true;
    static Requirement matches(ValueHolder<String> holder, String m) {
        return () -> Objects.equals(holder.getValue(), m);
    }

    record AmendmentBuilder(AmendmentConfigFactory parent, SubCategoryBuilder subCategory, int index) {
        NbtCompound getData() {
            return parent.amendments.get(index);
        }

        void build() {
            var area = addStringDropdownOption("area", always,
                    Lists.newArrayList("blocks", "fluids", "items", "structures", "trees", "mobs"));
            addStringOption("mod", always);

            var selector = addStringDropdownOption("selector", always,
                    Lists.newArrayList("all", "matching", "matching_any", "matching_block_tag", "containing"));
            addListOption("matching_any", matches(selector, "matching_any"));
            addStringOption("matching", matches(selector, "matching"));
            addStringOption("containing", matches(selector, "containing"));
            addStringOption("matching_block_tag", matches(selector, "matching_block_tag"));

            var results = addStringDropdownOption("results", always,
                    Lists.newArrayList("set_value", "set_field", "erase"));
            addDoubleOption("value", matches(results, "set_value"));
            addStringDropdownOption("field_name",
                    Requirement.all(matches(area, "blocks"), matches(results, "set_field")),
                    Lists.newArrayList("full", "float", "top", "laggy"));
            addDeleteButton();
        }

        private void addDeleteButton() {
            subCategory.add(parent.builder.entryBuilder().startBooleanToggle(Text.translatable("config.infinity.amendments.delete"), false)
                    .requireRestart()
                    .setTooltip(amendmentTooltip("delete"))
                    .setSaveConsumer((value) -> {
                        if (value) deleteAmendment();
                    })
                    .build());
        }

        private void addStringOption(String name, Requirement req) {
            String current = NbtUtils.getString(getData(), name, "");
            subCategory.add(parent.builder.entryBuilder().startStrField(
                            Text.translatable("config.infinity.amendments."+name),
                            current)
                    .requireRestart()
                    .setTooltip(amendmentTooltip(name))
                    .setDisplayRequirement(req)
                    .setSaveConsumer((value)-> amendmentSetter(name, value, AmendmentUpdater.ofString)).build());
        }

        private DropdownBoxEntry<String> addStringDropdownOption(String name, Requirement req, List<String> options) {
            String current = NbtUtils.getString(getData(), name, "");
            DropdownBoxEntry<String> res = parent.builder.entryBuilder().startDropdownMenu(
                            Text.translatable("config.infinity.amendments."+name),
                            DropdownMenuBuilder.TopCellElementBuilder.of(current, (s) -> s))
                    .requireRestart()
                    .setTooltip(amendmentTooltip(name))
                    .setDisplayRequirement(req)
                    .setSaveConsumer((value) -> amendmentSetter(name, String.valueOf(value), AmendmentUpdater.ofString))
                    .setSelections(options).build();
            subCategory.add(res);
            return res;
        }

        private void addDoubleOption(String name, Requirement req) {
            double current = NbtUtils.getDouble(getData(), name, 0d);
            subCategory.add(parent.builder.entryBuilder().startDoubleField(
                            Text.translatable("config.infinity.amendments."+name),
                            current)
                    .requireRestart()
                    .setTooltip(amendmentTooltip(name))
                    .setDisplayRequirement(req)
                    .setSaveConsumer((value) -> amendmentSetter(name, value, AmendmentUpdater.ofDouble)).build());
        }

        List<String> getSafeList(String name) {
            NbtCompound data = getData();
            if (data.contains(name, NbtElement.LIST_TYPE)) return convertNbtList(data.getList(name, NbtElement.STRING_TYPE));
            return List.of("");
        }

        private void addListOption(String name, Requirement req) {
            List<String> current = getSafeList(name);
            if (!Objects.equals(current.getLast(), "")) current.add("");
            subCategory.add(parent.builder.entryBuilder().startStrList(
                            Text.translatable("config.infinity.amendments."+name),
                            current)
                    .requireRestart()
                    .setTooltip(amendmentTooltip(name))
                    .setDisplayRequirement(req)
                    .setSaveConsumer((value) -> amendmentSetter(name, value, AmendmentUpdater.ofStringList)).build());
        }

        <T> void amendmentSetter(String key, T newValue, AmendmentUpdater<T> updater) {
            NbtCompound amendmentNbt = getData();
            // Check if an amendment should be changed before writing
            if (updater.check(amendmentNbt, key, newValue)) {
                amendmentNbt = updater.update(amendmentNbt, key, newValue);
                parent.edited = true;
                parent.amendments.put(index, amendmentNbt);
            }
        }

        void deleteAmendment() {
            parent.amendments.put(index, new NbtCompound());
            parent.edited = true;
        }

        static SubCategoryListEntry getNew(AmendmentConfigFactory parent, int i) {
            SubCategoryBuilder subCategory = parent.builder.entryBuilder().startSubCategory(
                    Text.translatable("config.infinity.amendment", String.valueOf(i)));
            (new AmendmentBuilder(parent, subCategory, i)).build();
            return subCategory.build();
        }

        static SubCategoryListEntry getShadow(AmendmentConfigFactory parent, int i, ValueHolder<Integer> shadowAmount, int shadowI) {
            SubCategoryBuilder subCategory = parent.builder.entryBuilder()
                    .startSubCategory(Text.translatable("config.infinity.amendment.new"))
                    .setDisplayRequirement(() -> shadowAmount.getValue() > shadowI);
            (new AmendmentBuilder(parent, subCategory, i)).build();
            return subCategory.build();
        }
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

    static Optional<Text[]> amendmentTooltip(String option) {
        return Optional.of(createTooltip("config.infinity.amendments."+option+".description").toArray(new Text[0]));
    }
}
