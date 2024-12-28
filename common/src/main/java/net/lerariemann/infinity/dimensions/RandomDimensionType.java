package net.lerariemann.infinity.dimensions;

import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.util.core.CommonIO;
import net.minecraft.nbt.*;

import java.util.Random;

public class RandomDimensionType {
    public String name;
    public String fullname;
    private final Random random;
    public RandomDimension parent;
    public NbtCompound data;
    public boolean ultrawarm, foggy;
    public static int scaleCap = 100000;

    RandomDimensionType(RandomDimension dim) {
        parent = dim;
        random = dim.random;
        name = "generated_" +dim.numericId;
        fullname = InfinityMod.MOD_ID + ":" + name;
        data = new NbtCompound();
        ultrawarm = roll("ultrawarm");
        data.putBoolean("ultrawarm", ultrawarm);
        add_roll(data, "natural");
        add_roll(data, "has_skylight");
        add_roll(data, "piglin_safe");
        add_roll(data, "bed_works");
        add_roll(data, "respawn_anchor_works");
        add_roll(data, "has_raids");
        data.putBoolean("has_ceiling", dim.hasCeiling());
        data.putDouble("coordinate_scale", coordinateScale());
        data.putFloat("ambient_light", random.nextFloat());
        if (roll("fixed_time")) {
            data.putInt("fixed_time", random.nextInt(24000));
        }
        data.putInt("min_y", parent.min_y);
        data.putInt("height", parent.height);
        data.putInt("logical_height", parent.height);
        data.putInt("monster_spawn_block_light_limit", random.nextInt(16));
        NbtCompound lightLevel = new NbtCompound();
        lightLevel.putString("type", "uniform");
        lightLevel.putInt("min_inclusive", 0);
        lightLevel.putInt("max_inclusive", random.nextInt(16));
        data.put("monster_spawn_light_level", lightLevel);
        data.putString("infiniburn", dim.PROVIDER.randomName(random, "tags"));
        String s = dim.PROVIDER.randomName(random, "dimension_effects");
        foggy = s.equals("minecraft:the_nether");
        data.putString("effects", s);
        CommonIO.write(data, dim.getStoragePath() + "/dimension_type", name + ".json");
    }

    boolean roll(String key) {
        return parent.PROVIDER.roll(random, key);
    }

    void add_roll(NbtCompound res, String key) {
        res.putBoolean(key, roll(key));
    }

    double coordinateScale() {
        int scaleMax = parent.PROVIDER.gameRulesInt.get("maxDimensionScale");
        if (scaleMax <= 0 || scaleMax > scaleCap) scaleMax = scaleCap;
        int distribParam = Math.min(scaleMax, 8);
        double scaleMin = 1.0 / scaleMax;
        double random1 = random.nextBoolean() ? 1.0 : scaleMin + (Math.sqrt(distribParam) - scaleMin)*random.nextDouble();
        double random2 = distribParam / random1;
        return random.nextBoolean() ? random1 : random2;
    }
}
