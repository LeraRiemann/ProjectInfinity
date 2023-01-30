package net.lerariemann.infinity.dimensions.features;

import net.lerariemann.infinity.dimensions.RandomProvider;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;

public class RandomGeode extends RandomisedFeature {
    public RandomGeode(int i, RandomProvider provider, String path) {
        super(i, provider);
        name = "geode_" + i;
        id = type = "geode";
        save(path,1 + random.nextInt(32));
    }

    NbtCompound feature() {
        NbtCompound config = new NbtCompound();
        NbtCompound blocks = new NbtCompound();
        blocks.put("filling_provider", PROVIDER.randomBlockProvider(random, RandomProvider.weighedRandom(random,1, 3)? PROVIDER.AIR : PROVIDER.FLUIDS));
        blocks.put("inner_layer_provider", PROVIDER.randomBlockProvider(random, PROVIDER.FULL_BLOCKS));
        blocks.put("alternate_inner_layer_provider", PROVIDER.randomBlockProvider(random, PROVIDER.FULL_BLOCKS));
        blocks.put("middle_layer_provider", PROVIDER.randomBlockProvider(random, PROVIDER.FULL_BLOCKS));
        blocks.put("outer_layer_provider", PROVIDER.randomBlockProvider(random, PROVIDER.FULL_BLOCKS));
        NbtList inner_placements = new NbtList();
        inner_placements.add(PROVIDER.randomBlock(random, PROVIDER.ALL_BLOCKS));
        blocks.put("inner_placements", inner_placements);
        blocks.putString("cannot_replace", PROVIDER.TAGS.getRandomElement(random));
        blocks.putString("invalid_blocks", PROVIDER.TAGS.getRandomElement(random));
        config.put("blocks", blocks);
        NbtCompound layers = new NbtCompound();
        double r = 1.0;
        for (String str: new String[]{"filling", "inner_layer", "middle_layer", "outer_layer"}) {
            r += random.nextExponential();
            layers.putDouble(str, r);
        }
        config.put("layers", layers);
        NbtCompound crack = new NbtCompound();
        crack.putDouble("generate_crack_chance", random.nextDouble());
        crack.putDouble("base_crack_size", random.nextDouble()*5);
        crack.putInt("crack_point_offset", random.nextInt(11));
        config.put("crack", crack);
        config.putDouble("noise_multiplier", Math.min(1.0, random.nextExponential()*0.1));
        config.putDouble("use_potential_placements_chance", random.nextDouble());
        config.putDouble("use_alternate_layer0_chance:", random.nextDouble());
        config.putBoolean("placements_require_layer0_alternate", random.nextBoolean());
        config.putInt("invalid_blocks_threshold", 1 + random.nextInt(16));
        return feature(config);
    }
}
