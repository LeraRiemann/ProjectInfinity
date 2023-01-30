package net.lerariemann.infinity.dimensions;

import net.lerariemann.infinity.InfinityMod;
import net.minecraft.nbt.*;

import java.util.Random;

public class RandomDimensionType {
    public String name;
    public String fullname;
    private final Random random;
    public int height;

    RandomDimensionType(int i, RandomProvider provider, String path) {
        random = new Random(i);
        name = "generated_" +i;
        fullname = InfinityMod.MOD_ID + ":" + name;
        NbtCompound res = new NbtCompound();
        res.putBoolean("ultrawarm", random.nextBoolean());
        res.putBoolean("natural", random.nextBoolean());
        res.putBoolean("has_skylight", random.nextBoolean());
        res.putBoolean("piglin_safe", random.nextBoolean());
        res.putBoolean("bed_works", random.nextBoolean());
        res.putBoolean("respawn_anchor_works", random.nextBoolean());
        res.putBoolean("has_raids", random.nextBoolean());
        res.putBoolean("has_ceiling", random.nextBoolean());
        res.putDouble("coordinate_scale", coordinateScale());
        res.putFloat("ambient_light", ambientLight());
        if (random.nextBoolean()){
            res.putInt("fixed_time", random.nextInt(24000));
        }
        int min_y = -16*Math.min(127, 4 + (int)Math.floor(random.nextExponential()*2));
        res.putInt("min_y", min_y);
        int max_y = 16*Math.max(1, Math.min(125, (int)Math.floor(random.nextGaussian(16.0, 4.0))));
        height = 16 + max_y - min_y;
        res.putInt("height", height);
        res.putInt("logical_height", random.nextBoolean() ? height : height/2 + random.nextInt(height/2));
        res.putInt("monster_spawn_block_light_limit", random.nextInt(16));
        res.put("monster_spawn_light_level", RandomProvider.intProvider(random, 16, true));
        res.putString("infiniburn", provider.TAGS.getRandomElement(random));
        if (RandomProvider.weighedRandom(random,2, 1)) res.putString("effect", RandomProvider.weighedRandom(random,4, 1) ? "minecraft:nether" : "minecraft:the_end");
        CommonIO.write(res, path + "/dimension_type", name + ".json");
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

    float ambientLight() {
        if (random.nextBoolean())
            return 0.0f;
        return random.nextFloat();
    }
}
