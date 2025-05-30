package net.lerariemann.infinity.util.config;

import dev.architectury.platform.Platform;
import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.util.core.CommonIO;
import net.lerariemann.infinity.util.core.ConfigType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtString;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public record Amendment(ConfigType area, ModSelector modSelector, Selector selector, Results results) {
    public static Amendment of(NbtCompound data) {
        String mod = data.getString("mod");
        ModSelector modSelector;
        if (mod.equals("all")) {
            modSelector = new UniversalModSelector();
        }
        else if (!Platform.isModLoaded(mod)) return null;
        else modSelector = new MatchingModSelector(mod);

        ConfigType area = ConfigType.byName(data.getString("area"));
        Selector selector = switch(data.getString("selector")) {
            case "all" -> new UniversalSelector();
            case "containing" -> new ContainingSelector(data.getString("containing"));
            case "matching" -> new MatchingSelector(data.getString("matching"));
            case "matching_any" -> new MatchingAnySelector(data.getList("matching", NbtElement.STRING_TYPE)
                    .stream().map(e->(NbtString)e).map(NbtString::asString).toList());
            default -> null;
        };
        Results results = switch (data.getString("results")) {
            case "set_value" -> new SetValue(data.getInt("value"));
            case "erase" -> new SetValue(0);
            case "set_field" -> new SetField(data.getString("field_name"), data.get("field"));
            default -> null;
        };

        if (area == null || selector == null || results == null) return null;
        return new Amendment(area, modSelector, selector, results);
    }

    public static Map<ConfigType, List<Amendment>> getAmendmentList() {
        Map<ConfigType, List<Amendment>> data = new HashMap<>();
        NbtCompound rawData = CommonIO.read(InfinityMod.configPath.resolve("modular").resolve("amendments.json"));
        AtomicInteger i = new AtomicInteger();
        for (NbtElement e : rawData.getList("elements", NbtElement.COMPOUND_TYPE)) {
            Amendment amd = Amendment.of((NbtCompound)e);
            if (amd == null) continue;
            i.getAndIncrement();
            ConfigType type = amd.area;
            if (!data.containsKey(type)) data.put(type, new ArrayList<>());
            data.get(type).add(amd);
        }
        InfinityMod.LOGGER.info("Registered {} valid amendments", i.get());
        return data;
    }

    public boolean applies(String modId, String key) {
        return modSelector.applies(modId) && selector.applies(key);
    }
    public double apply() {
        return results.apply();
    }
    public NbtCompound apply(NbtCompound data) {
        return results.apply(data);
    }

    public interface ModSelector {
        boolean applies(String modId);
    }
    public static class UniversalModSelector implements ModSelector {
        @Override
        public boolean applies(String modId) {
            return true;
        }
    }
    public record MatchingModSelector(String mod) implements ModSelector {
        @Override
        public boolean applies(String modId) {
            return modId.equals(this.mod);
        }
    }

    public interface Selector {
        boolean applies(String key);
    }
    public static class UniversalSelector implements Selector {
        public boolean applies(String key) {
            return true;
        }
    }
    public record MatchingSelector(String key) implements Selector {
        public boolean applies(String key) {
            return key.equals(this.key);
        }
    }
    public record MatchingAnySelector(List<String> lst) implements Selector {
        public boolean applies(String key) {
            return this.lst.contains(key);
        }
    }
    public record ContainingSelector(String key) implements Selector {
        public boolean applies(String key) {
            return key.contains(this.key);
        }
    }

    public interface Results{
        default double apply() {
            return 1.0;
        }
        default NbtCompound apply(NbtCompound data) {
            return data;
        }
    }
    public record SetValue(double value) implements Results {
        @Override
        public double apply() {
            return value;
        }
    }
    public record SetField(String key, NbtElement value) implements Results {
        @Override
        public NbtCompound apply(NbtCompound data) {
            if (data == null) data = new NbtCompound();
            data.put(key, value);
            return data;
        }
    }
}
