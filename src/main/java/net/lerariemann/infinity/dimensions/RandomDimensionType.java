package net.lerariemann.infinity.dimensions;

import net.lerariemann.infinity.InfinityMod;
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
        res.putBoolean("ultrawarm", random.nextBoolean());
        res.putBoolean("natural", random.nextBoolean());
        res.putBoolean("has_skylight", RandomProvider.weighedRandom(random, 1, 3));
        res.putBoolean("piglin_safe", random.nextBoolean());
        res.putBoolean("bed_works", random.nextBoolean());
        res.putBoolean("respawn_anchor_works", random.nextBoolean());
        res.putBoolean("has_raids", random.nextBoolean());
        res.putBoolean("has_ceiling", random.nextBoolean());
        res.putDouble("coordinate_scale", coordinateScale());
        res.putFloat("ambient_light", random.nextFloat());
        if (random.nextBoolean()){
            res.putInt("fixed_time", random.nextInt(24000));
        }
        parent.min_y = 16*Math.min(0, (int)Math.floor(random.nextGaussian(-4.0, 4.0)));
        res.putInt("min_y", parent.min_y);
        int max_y = 16*Math.max(1, Math.min(125, (int)Math.floor(random.nextGaussian(16.0, 4.0))));
        parent.height = max_y - parent.min_y;
        res.putInt("height", parent.height);
        res.putInt("logical_height", random.nextBoolean() ? parent.height : parent.height/2 + random.nextInt(parent.height/2));
        res.putInt("monster_spawn_block_light_limit", random.nextInt(16));
        res.put("monster_spawn_light_level", RandomProvider.intProvider(random, 16, true));
        res.putString("infiniburn", dim.PROVIDER.TAGS.getRandomElement(random));
        if (RandomProvider.weighedRandom(random,2, 1)) res.putString("effect", RandomProvider.weighedRandom(random,4, 1) ? "minecraft:nether" : "minecraft:the_end");
        CommonIO.write(res, dim.storagePath + "/dimension_type", name + ".json");
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
