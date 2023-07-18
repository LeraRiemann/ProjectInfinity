package net.lerariemann.infinity.dimensions;

import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.util.CommonIO;
import net.lerariemann.infinity.util.WeighedStructure;
import net.minecraft.nbt.*;

import java.util.Random;

public class RandomDimensionType {
    public String name;
    public String fullname;
    private final Random random;
    public RandomDimension parent;

    RandomDimensionType(RandomDimension dim) {
        parent = dim;
        random = dim.random;
        name = "generated_" +dim.id;
        fullname = InfinityMod.MOD_ID + ":" + name;
        NbtCompound res = new NbtCompound();
        add_roll(res, "ultrawarm");
        add_roll(res, "natural");
        add_roll(res, "has_skylight");
        add_roll(res, "piglin_safe");
        add_roll(res, "bed_works");
        add_roll(res, "respawn_anchor_works");
        add_roll(res, "has_raids");
        res.putBoolean("has_ceiling", dim.hasCeiling());
        res.putDouble("coordinate_scale", coordinateScale());
        res.putFloat("ambient_light", random.nextFloat());
        if (roll("fixed_time")) {
            res.putInt("fixed_time", random.nextInt(24000));
        }
        int min_y = 16*Math.min(0, (int)Math.floor(random.nextGaussian(-4.0, 4.0)));
        if (parent.isNotOverworld()) min_y = Math.max(min_y, -48);
        parent.min_y = min_y;
        res.putInt("min_y", parent.min_y);
        int max_y = 16*Math.max(1, Math.min(125, (int)Math.floor(random.nextGaussian(16.0, 4.0))));
        parent.height = max_y - parent.min_y;
        res.putInt("height", parent.height);
        res.putInt("logical_height", parent.height);
        res.putInt("monster_spawn_block_light_limit", random.nextInt(16));
        NbtCompound lightLevel = new NbtCompound();
        lightLevel.putString("type", "uniform");
        lightLevel.put("value", RandomProvider.genBounds(0, random.nextInt(16)));
        res.put("monster_spawn_light_level", lightLevel);
        res.putString("infiniburn", dim.PROVIDER.randomName(random, "tags"));
        res.putString("effects", dim.PROVIDER.randomName(random, "effects"));
        CommonIO.write(res, dim.storagePath + "/dimension_type", name + ".json");
    }

    boolean roll(String key) {
        return parent.PROVIDER.roll(random, key);
    }

    void add_roll(NbtCompound res, String key) {
        res.putBoolean(key, roll(key));
    }

    double coordinateScale() {
        WeighedStructure<Double> values = new WeighedStructure<>();
        values.add(1.0, 2.0);
        values.add(8.0, 2.0);
        double random1 = Math.min(random.nextExponential(), 16.0);
        values.add(Math.exp(random1+3.0), 1.0);
        values.add(Math.exp(-random1), 1.0);
        values.add(1.0 + 7*random.nextDouble(), 2.0);
        return values.getRandomElement(random);
    }
}