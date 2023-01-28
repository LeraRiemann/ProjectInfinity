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
        res.put("ultrawarm", NbtByte.of(random.nextBoolean()));
        res.put("natural", NbtByte.of(random.nextBoolean()));
        res.put("has_skylight", NbtByte.of(random.nextBoolean()));
        res.put("piglin_safe", NbtByte.of(random.nextBoolean()));
        res.put("bed_works", NbtByte.of(random.nextBoolean()));
        res.put("respawn_anchor_works", NbtByte.of(random.nextBoolean()));
        res.put("has_raids", NbtByte.of(random.nextBoolean()));
        res.put("has_ceiling", NbtByte.of(random.nextBoolean()));
        res.put("coordinate_scale", NbtDouble.of(coordinateScale()));
        res.put("ambient_light", NbtDouble.of(ambientLight()));
        if (random.nextBoolean()){
            res.put("fixed_time", NbtInt.of(random.nextInt(24000)));
        }
        int min_y = minY(127);
        res.put("min_y", NbtInt.of(min_y));
        int max_y = -minY(125);
        height = 16 + max_y - min_y;
        res.put("height", NbtInt.of(height));
        res.put("logical_height", NbtInt.of(random.nextBoolean() ? height : height/2 + random.nextInt(height/2)));
        res.put("monster_spawn_block_light_limit", NbtInt.of(random.nextInt(16)));
        res.put("monster_spawn_light_level", monsterSpawnLightLevel(true));
        res.put("infiniburn", NbtString.of(provider.INFINIBURN.getRandomElement(random)));
        if (weighedRandom(2, 1)) res.put("effect", NbtString.of(weighedRandom(4, 1) ? "minecraft:nether" : "minecraft:the_end"));
        CommonIO.write(res, path + "/datapacks/" + InfinityMod.MOD_ID + "/data/" + InfinityMod.MOD_ID + "/dimension_type", name + ".json");
    }

    boolean weighedRandom(int weight0, int weight1) {
        int i = random.nextInt(weight0+weight1);
        return i < weight1;
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

    double ambientLight() {
        if (random.nextBoolean())
            return 0.0;
        return random.nextDouble();
    }

    int minY(int upperLimit) {
        int random1 = Math.min(upperLimit, (int)Math.floor(random.nextExponential()*2));
        return -16*random1;
    }

    NbtCompound genBounds() {
        NbtCompound value = new NbtCompound();
        int a = random.nextInt(16);
        int b = random.nextInt(16);
        value.put("min_inclusive", NbtInt.of(Math.min(a, b)));
        value.put("max_inclusive", NbtInt.of(Math.max(a, b)));
        return value;
    }

    NbtElement monsterSpawnLightLevel(boolean acceptDistributions) {
        int i = random.nextInt(acceptDistributions ? 6 : 4);
        NbtCompound res = new NbtCompound();
        switch(i) {
            case 0 -> {
                res.put("type", NbtString.of("constant"));
                res.put("value", NbtInt.of(random.nextInt(16)));
                return res;
            }
            case 1, 2 -> {
                res.put("type", NbtString.of(i==1 ? "uniform" : "biased_to_bottom"));
                res.put("value", genBounds());
                return res;
            }
            case 4 -> {
                res.put("type", NbtString.of("clamped"));
                NbtCompound value = genBounds();
                value.put("source", monsterSpawnLightLevel(false));
                res.put("value", value);
                return res;
            }
            case 3 -> {
                res.put("type", NbtString.of("clamped_normal"));
                NbtCompound value = genBounds();
                value.put("mean", NbtDouble.of(random.nextDouble()*16));
                value.put("deviation", NbtDouble.of(random.nextExponential()));
                res.put("value", value);
                return res;
            }
            case 5 -> {
                res.put("type", NbtString.of("weighted_list"));
                int j = 2 + random.nextInt(0, 5);
                NbtList list = new NbtList();
                for (int k=0; k<j; k++) {
                    NbtCompound element = new NbtCompound();
                    element.put("data", monsterSpawnLightLevel(false));
                    element.put("weight", NbtInt.of(random.nextInt(100)));
                    list.add(element);
                }
                res.put("distribution", list);
                return res;
            }
        }
        return res;
    }
}
