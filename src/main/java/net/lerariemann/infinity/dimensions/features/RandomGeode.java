package net.lerariemann.infinity.dimensions.features;

import net.lerariemann.infinity.dimensions.RandomFeaturesList;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;

import static java.lang.Math.max;

public class RandomGeode extends RandomisedFeature {
    public RandomGeode(RandomFeaturesList parent) {
        super(parent, "geode");
        save_with_placement();
    }

    void placement() {
        int sea = max(daddy.sea_level, daddy.min_y + daddy.height / 8);
        int halfsea = (sea + daddy.min_y) / 2;
        int minbound = random.nextInt(daddy.min_y, halfsea);
        int maxbound = random.nextInt(halfsea, sea);
        addRarityFilter(1 + random.nextInt(32));
        addInSquare();
        addHeightRange(uniformHeightRange(minbound, maxbound));
        addBiome();
    }

    NbtCompound feature() {
        NbtCompound config = new NbtCompound();
        NbtCompound blocks = new NbtCompound();
        blocks.put("filling_provider", PROVIDER.randomBlockProvider(random, parent.roll("flood_geodes") ? "fluids" : "airs"));
        blocks.put("inner_layer_provider", PROVIDER.randomBlockProvider(random, "full_blocks"));
        blocks.put("alternate_inner_layer_provider", PROVIDER.randomBlockProvider(random, "full_blocks"));
        blocks.put("middle_layer_provider", PROVIDER.randomBlockProvider(random, "full_blocks"));
        blocks.put("outer_layer_provider", PROVIDER.randomBlockProvider(random, "full_blocks"));
        NbtList inner_placements = new NbtList();
        inner_placements.add(PROVIDER.randomBlock(random, "all_blocks"));
        blocks.put("inner_placements", inner_placements);
        blocks.putString("cannot_replace", PROVIDER.randomName(random, "tags"));
        blocks.putString("invalid_blocks", PROVIDER.randomName(random, "tags"));
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
