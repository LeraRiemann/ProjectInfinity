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
    public NbtCompound data;
    public boolean ultrawarm, foggy;

    RandomDimensionType(RandomDimension dim) {
        parent = dim;
        random = dim.random;
        name = "generated_" +dim.id;
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
        int min_y = 16*Math.min(0, (int)Math.floor(random.nextGaussian(-4.0, 4.0)));
        if (parent.isNotOverworld()) min_y = Math.max(min_y, -48);
        parent.min_y = min_y;
        data.putInt("min_y", parent.min_y);
        int max_y = 16*Math.max(1, Math.min(125, (int)Math.floor(random.nextGaussian(16.0, 4.0))));
        parent.height = max_y - parent.min_y;
        data.putInt("height", parent.height);
        data.putInt("logical_height", parent.height);
        data.putInt("monster_spawn_block_light_limit", random.nextInt(16));
        NbtCompound lightLevel = new NbtCompound();
        lightLevel.putString("type", "uniform");
        lightLevel.put("value", RandomProvider.genBounds(0, random.nextInt(16)));
        data.put("monster_spawn_light_level", lightLevel);
        data.putString("infiniburn", dim.PROVIDER.randomName(random, "tags"));
        String s = dim.PROVIDER.randomName(random, "effects");
        foggy = s.equals("minecraft:the_nether");
        data.putString("effects", s);
        CommonIO.write(data, dim.storagePath + "/dimension_type", name + ".json");
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
