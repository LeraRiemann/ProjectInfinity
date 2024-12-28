package net.lerariemann.infinity.dimensions.features;

import net.lerariemann.infinity.dimensions.RandomFeaturesList;
import net.lerariemann.infinity.util.core.RandomProvider;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;

public class RandomShape extends RandomisedFeature {
    String shape;
    boolean usePreset;
    boolean useBands;

    public RandomShape(RandomFeaturesList parent, String shape) {
        super(parent, shape);
        this.shape = shape;
        this.usePreset = parent.roll("colourful_shapes");
        this.useBands = parent.roll("banded_shapes");
        id = (shape.equals("cube")) ? "infinity:random_cube" : "infinity:random_shape";
        save_with_placement();
    }

    void placement() {
        int a = (int)random.nextGaussian(daddy.sea_level, 16);
        int b = random.nextInt(daddy.sea_level, daddy.height + daddy.min_y);
        placement_floating(1 + random.nextInt(64), Math.max(a, daddy.min_y), b);
    }

    NbtCompound feature() {
        NbtCompound config = new NbtCompound();
        NbtList replaceable = new NbtList();
        replaceable.add(RandomProvider.nameToElement(parent.parent.parent.default_fluid.getString("Name")));
        config.put("replaceable", replaceable);
        if (!usePreset) addRandomBlockProvider(config, "block_provider", "full_blocks_worldgen");
        else config.put("block_provider", PROVIDER.randomPreset(random, useBands ? "weighted_state_provider" : "noise_provider"));
        config.put("radius", RandomProvider.floatProvider(random, 2.0f, 20.0f));
        config.putBoolean("use_bands", useBands);
        if (!shape.equals("cube")) config.putDouble("power", shape.equals("sphere") ? 2.0 :
                (shape.equals("octahedron") ? 1.0 : random.nextDouble(2.0)));
        return feature(config);
    }
}
