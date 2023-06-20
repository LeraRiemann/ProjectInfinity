package net.lerariemann.infinity.dimensions;

import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.util.CommonIO;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;

import java.util.Random;

public class RandomCarver {
    public String name;
    public String fullname;
    private final Random random;
    public RandomBiome parent;

    RandomCarver(RandomBiome b, boolean cave) {
        parent = b;
        String s = cave ? "cave" : "canyon";
        name = s + "_" + b.id;
        fullname = InfinityMod.MOD_ID + ":" + name;
        random = b.random;
        NbtCompound res = new NbtCompound();
        res.putString("type", "minecraft:" + s);
        NbtCompound config = new NbtCompound();
        config.putFloat("probability", (float)Math.min(1.0, random.nextExponential()*(cave ? 0.1 : 0.02)));
        NbtCompound lava_level = new NbtCompound();
        lava_level.putInt("above_bottom", 0);
        config.put("lava_level", lava_level);
        config.put("y", y());
        config.put("replaceable", replaceable());
        if (cave) {
            putScale(config, "yScale", 3.0f);
            putScale(config, "horizontal_radius_multiplier", 2.5f);
            putScale(config, "vertical_radius_multiplier", 2.5f);
            config.put("floor_level", RandomProvider.floatProvider(random, -1.0f, 1.0f));
        }
        else {
            putScale(config, "yScale", 3.0f);
            config.put("vertical_rotation", RandomProvider.floatProvider(random, -1.0f, 1.0f));
            NbtCompound shape = new NbtCompound();
            putScale(shape, "distance_factor", 2.0f);
            putScale(shape, "thickness", 3.0f);
            putScale(shape, "horizontal_radius_factor", 2.0f);
            shape.putFloat("vertical_radius_default_factor", random.nextFloat(0.5f, 2.0f));
            shape.putFloat("vertical_radius_center_factor", random.nextFloat(0.5f, 2.0f));
            shape.putInt("width_smoothness", random.nextInt(8));
            config.put("shape", shape);
        }
        res.put("config", config);
        CommonIO.write(res, parent.parent.storagePath + "/worldgen/configured_carver", name + ".json");
    }

    void putScale(NbtCompound config, String key, float bound) {
        config.put(key, RandomProvider.floatProvider(random, 1/bound, bound));
    }

    NbtCompound y() {
        int min_y = parent.parent.min_y;
        return RandomProvider.heightProvider(random, min_y, min_y + parent.parent.height, true, false);
    }

    NbtList replaceable() {
        NbtList res = new NbtList();
        res.add(parent.parent.default_block.get("Name"));
        for (NbtCompound a : parent.parent.additional_blocks) res.add(a.get("Name"));
        res.add(parent.parent.default_fluid.get("Name"));
        res.add(NbtString.of(parent.parent.underwater.get(parent.fullname)));
        return res;
    }
}
