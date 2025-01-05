package net.lerariemann.infinity.util.config;

import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.util.core.CommonIO;
import net.lerariemann.infinity.util.core.WeighedStructure;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.Identifier;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

/** A collection of {@link WeighedStructure} objects, used in {@link ConfigGenerator} to auto-sort different types of game content by mod ID.
 * @author LeraRiemann */
public class DataCollection<T> {
    private final Map<String, WeighedStructure<T>> map = new HashMap<>();
    String addPath;
    String name;
    DataCollection(String addPath, String name) {
        this.addPath = addPath;
        this.name = name;
    }

    void add(String modId, T elem) {
        if (!map.containsKey(modId)) map.put(modId, new WeighedStructure<>());
        map.get(modId).add(elem, 1.0);
    }

    static void addIdentifier(DataCollection<String> collection, Identifier id) {
        collection.add(id.getNamespace(), id.toString());
    }

    /** Writes collected content to the disk, creating a separate file for every collected mod. */
    void save() {
        map.forEach((modId, data) -> {
            if (!data.keys.isEmpty()) CommonIO.write(wsToCompound(data),
                    InfinityMod.configPath.resolve("modular").resolve(modId).resolve(addPath),
                    name + ".json");
        });
    }

    /**
     * Converts data stored in a {@link WeighedStructure} into a format compatible with {@link CommonIO}, sorting it alphabetically in the process
     * (this isn't strictly required but makes files much easier to navigate).
     */
    static <T> NbtCompound wsToCompound(WeighedStructure<T> w) {
        NbtCompound res = new NbtCompound();
        NbtList elements = new NbtList();
        List<Integer> range = new ArrayList<>(IntStream.rangeClosed(0, w.keys.size() - 1).boxed().toList());
        range.sort(new Comparator<Integer>() {
            public String extract(int i) {
                T obj = w.keys.get(i);
                if (Objects.requireNonNull(obj) instanceof NbtCompound compound) {
                    return compound.getString("Name");
                } else if (obj instanceof NbtList list) {
                    return list.get(0).toString();
                }
                return obj.toString();
            }
            @Override
            public int compare(Integer i, Integer j) {
                return extract(i).compareTo(extract(j));
            }
        });
        for (int i = 0; i < w.keys.size(); i++) {
            NbtCompound element = new NbtCompound();
            T obj = w.keys.get(range.get(i));
            if (Objects.requireNonNull(obj) instanceof String string) {
                element.putString("key", string);
            } else if (obj instanceof NbtElement e) {
                element.put("key", e);
            } else {
                throw new RuntimeException("Unexpected weighed structure format");
            }
            element.putDouble("weight", w.weights.get(range.get(i)));
            elements.add(element);
        }
        res.put("elements", elements);
        return res;
    }

    static void loggerOutput(int count, String type) {
        InfinityMod.LOGGER.info("Registered {} {}", count, type);
    }

    /** An implementation of {@link DataCollection} that logs the total amount of stored entries in the {@link InfinityMod#LOGGER}. */
    static class Logged<T> extends DataCollection<T> {
        private final AtomicInteger i = new AtomicInteger();
        String loggerName;

        Logged(String addPath, String name) {
            this(addPath, name, name);
        }
        Logged(String addPath, String name, String loggerName) {
            super(addPath, name);
            this.loggerName = loggerName;
        }
        @Override
        void add(String modId, T elem) {
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
