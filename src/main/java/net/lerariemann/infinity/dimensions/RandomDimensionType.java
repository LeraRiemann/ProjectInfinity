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
        int min_y = -16*Math.min(127, (int)Math.floor(random.nextExponential()*2));
        res.putInt("min_y", min_y);
        int max_y = 16*Math.max(1, Math.min(125, (int)Math.floor(random.nextGaussian(16.0, 4.0))));
        height = 16 + max_y - min_y;
        res.putInt("height", height);
        res.putInt("logical_height", random.nextBoolean() ? height : height/2 + random.nextInt(height/2));
        res.putInt("monster_spawn_block_light_limit", random.nextInt(16));
        res.put("monster_spawn_light_level", monsterSpawnLightLevel(true));
        res.putString("infiniburn", provider.INFINIBURN.getRandomElement(random));
        if (RandomProvider.weighedRandom(random,2, 1)) res.putString("effect", RandomProvider.weighedRandom(random,4, 1) ? "minecraft:nether" : "minecraft:the_end");
        CommonIO.write(res, path + "/datapacks/" + InfinityMod.MOD_ID + "/data/" + InfinityMod.MOD_ID + "/dimension_type", name + ".json");
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

    NbtCompound genBounds() {
        NbtCompound value = new NbtCompound();
        int a = random.nextInt(16);
        int b = random.nextInt(16);
        value.putInt("min_inclusive", Math.min(a, b));
        value.putInt("max_inclusive", Math.max(a, b));
        return value;
    }

    NbtElement monsterSpawnLightLevel(boolean acceptDistributions) {
        int i = random.nextInt(acceptDistributions ? 6 : 4);
        NbtCompound res = new NbtCompound();
        switch(i) {
            case 0 -> {
                res.putString("type", "constant");
                res.putInt("value", random.nextInt(16));
                return res;
            }
            case 1, 2 -> {
                res.putString("type", i==1 ? "uniform" : "biased_to_bottom");
                res.put("value", genBounds());
                return res;
            }
            case 4 -> {
                res.putString("type", "clamped");
                NbtCompound value = genBounds();
                value.put("source", monsterSpawnLightLevel(false));
                res.put("value", value);
                return res;
            }
            case 3 -> {
                res.putString("type", "clamped_normal");
                NbtCompound value = genBounds();
                value.putDouble("mean", random.nextDouble()*16);
                value.putDouble("deviation", random.nextExponential());
                res.put("value", value);
                return res;
            }
            case 5 -> {
                res.putString("type", "weighted_list");
                int j = 2 + random.nextInt(0, 5);
                NbtList list = new NbtList();
                for (int k=0; k<j; k++) {
                    NbtCompound element = new NbtCompound();
                    element.put("data", monsterSpawnLightLevel(false));
                    element.putInt("weight", random.nextInt(100));
                    list.add(element);
                }
                res.put("distribution", list);
                return res;
            }
        }
        return res;
    }
}
