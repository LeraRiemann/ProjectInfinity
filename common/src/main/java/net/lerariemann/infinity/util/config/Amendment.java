package net.lerariemann.infinity.util.config;

import dev.architectury.platform.Platform;
import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.util.core.CommonIO;
import net.lerariemann.infinity.util.core.ConfigType;
import net.lerariemann.infinity.util.core.NbtUtils;
import net.minecraft.block.Block;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public record Amendment(ConfigType area, ModSelector modSelector, Selector selector, Results results) {
    public static Amendment of(NbtCompound data) {
        String areaName = NbtUtils.getString(data, "area", "");
        ConfigType area = ConfigType.byName(areaName);
        if (area == null) {
            InfinityMod.LOGGER.warn("Unknown amendment area: {}", areaName);
            return null;
        }

        String mod = NbtUtils.getString(data, "mod");
        ModSelector modSelector;
        if (mod.equals("all")) {
            modSelector = new UniversalModSelector();
        }
        else if (!Platform.isModLoaded(mod)) return null;
        else modSelector = new MatchingModSelector(mod);

        String selectorType = NbtUtils.getString(data,"selector", "");
        Selector selector = switch(selectorType) {
            case "all" -> new UniversalSelector();
            case "containing" -> new ContainingSelector(NbtUtils.getString(data, "containing"));
            case "matching" -> new MatchingSelector(NbtUtils.getString(data, "matching"));
            case "matching_block_tag" -> new MatchingBlockTagSelector(NbtUtils.getString(data, "matching"));
            case "matching_any" -> new MatchingAnySelector(data.getList("matching", NbtElement.STRING_TYPE)
                    .stream().map(e->(NbtString)e).map(NbtString::asString).toList());
            default -> {
                InfinityMod.LOGGER.warn("Unknown amendment selector type: {}", selectorType);
                yield null;
            }
        };

        String resultType = NbtUtils.getString(data, "results");
        Results results = switch (resultType) {
            case "set_value" -> new SetValue(NbtUtils.getInt(data, "value"));
            case "erase" -> new SetValue(0);
            case "set_field" -> new SetField(NbtUtils.getString(data, "field_name"), data.get("field"));
            default -> {
                InfinityMod.LOGGER.warn("Unknown amendment result type: {}", resultType);
                yield null;
            }
        };

        if (selector == null || results == null) return null;
        return new Amendment(area, modSelector, selector, results);
    }

    public static Map<ConfigType, List<Amendment>> getAmendmentList() {
        Map<ConfigType, List<Amendment>> data = new HashMap<>();
        NbtCompound rawData = CommonIO.read(InfinityMod.amendmentPath);
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
    public record MatchingBlockTagSelector(TagKey<Block> tag) implements Selector {
        public MatchingBlockTagSelector(String key) {
            this(TagKey.of(RegistryKeys.BLOCK, Identifier.of(key)));
        }

        public boolean applies(String key) {
            Block b = Registries.BLOCK.get(RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(key)));
            if (b != null) {
                return b.getDefaultState().isIn(this.tag);
            }
            return false;
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
