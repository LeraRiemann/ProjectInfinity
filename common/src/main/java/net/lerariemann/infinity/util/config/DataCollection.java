package net.lerariemann.infinity.util.config;

import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.util.core.CommonIO;
import net.lerariemann.infinity.util.core.ConfigType;
import net.lerariemann.infinity.util.core.WeighedStructure;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

/** A collection of {@link WeighedStructure} objects, used in {@link ConfigGenerator} to auto-sort different types of game content by mod ID.
 * @author LeraRiemann */
public class DataCollection {
    private final Map<String, List<NbtCompound>> map = new HashMap<>();
    public static Map<ConfigType, List<Amendment>> amendmentList;
    ConfigType type;
    List<Amendment> amendments;

    DataCollection(ConfigType name) {
        this.type = name;
        this.amendments = amendmentList.getOrDefault(type, List.of());
    }

    void add(String modId, NbtCompound elem) {
        if (!map.containsKey(modId)) map.put(modId, new ArrayList<>());
        map.get(modId).add(elem);
    }
    void add(String modId, String key, @Nullable NbtCompound data) {
        NbtCompound elem = getAmendedEntry(modId, key, data);
        if (elem == null) return;
        add(modId, elem);
    }
    void add(String modId, String key) {
        add(modId, key, null);
    }

    NbtCompound getAmendedEntry(String modId, String key, @Nullable NbtCompound data) {
        double weight = 1.0;
        for (Amendment a: amendments) if (a.applies(modId, key)) {
            weight = a.apply();
            data = a.apply(data);
            break;
        }
        if (weight == 0) return null;
        NbtCompound elem = new NbtCompound();
        elem.putString("key", key);
        if (data != null) elem.put("data", data);
        elem.putDouble("weight", weight);
        return elem;
    }

    public void addIdentifier(Identifier id) {
        add(id.getNamespace(), id.toString());
    }

    /** Writes collected content to the disk, creating a separate file for every collected mod. */
    void save() {
        map.forEach((modId, data) -> CommonIO.write(wrapAndSortByKey(data),
                InfinityMod.configPath.resolve("modular").resolve(modId),
                type.getKey() + ".json"));
    }

    /**
     * Wraps collected data into a format compatible with {@link CommonIO}, sorting it alphabetically in the process
     * (this isn't strictly required but makes files much easier to navigate).
     */
    static NbtCompound wrapAndSortByKey(List<NbtCompound> w) {
        NbtCompound res = new NbtCompound();
        NbtList elements = new NbtList();
        List<Integer> range = new ArrayList<>(IntStream.rangeClosed(0, w.size() - 1).boxed().toList());
        range.sort(new Comparator<Integer>() {
            public String extract(int i) {
                NbtCompound compound = w.get(i);
                return compound.getString("key");
            }
            @Override
            public int compare(Integer i, Integer j) {
                return extract(i).compareTo(extract(j));
            }
        });
        for (int i = 0; i < w.size(); i++) {
            elements.add(w.get(range.get(i)));
        }
        res.put("elements", elements);
        return res;
    }

    static void loggerOutput(int count, String type) {
        InfinityMod.LOGGER.info("Registered {} {}", count, type);
    }

    /** An implementation of {@link DataCollection} that logs the total amount of stored entries in the {@link InfinityMod#LOGGER}. */
    static class Logged extends DataCollection {
        private final AtomicInteger i = new AtomicInteger();
        String loggerName;

        Logged(ConfigType name) {
            this(name, name.getKey());
        }
        Logged(ConfigType name, String loggerName) {
            super(name);
            this.loggerName = loggerName;
        }

        @Override
        void add(String modId, NbtCompound elem) {
            super.add(modId, elem);
            i.getAndIncrement();
        }
        @Override
        void save() {
            super.save();
            loggerOutput(i.get(), loggerName);
        }
    }
}
