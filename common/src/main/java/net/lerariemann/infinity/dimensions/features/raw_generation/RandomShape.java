package net.lerariemann.infinity.dimensions.features.raw_generation;

import net.lerariemann.infinity.dimensions.RandomFeaturesList;
import net.lerariemann.infinity.dimensions.features.Placement;
import net.lerariemann.infinity.dimensions.features.RandomisedFeature;
import net.lerariemann.infinity.util.core.ConfigType;
import net.lerariemann.infinity.util.core.NbtUtils;
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
        savePlacement();
    }

    public NbtList placement() {
        int a = (int)random.nextGaussian(daddy.sea_level, 16);
        int b = random.nextInt(daddy.sea_level, daddy.height + daddy.min_y);
        return Placement.floating(1 + random.nextInt(64), Math.max(a, daddy.min_y), b);
    }

    public NbtCompound feature() {
        NbtCompound config = new NbtCompound();
        NbtList replaceable = new NbtList();
        replaceable.add(NbtUtils.nameToElement(parent.parent.parent.default_fluid.getString("Name")));
        config.put("replaceable", replaceable);
        if (!usePreset) addRandomBlockProvider(config, "block_provider", ConfigType.FULL_BLOCKS_WG);
        else config.put("block_provider", PROVIDER.randomPreset(random, useBands ? "weighted_state_provider" : "noise_provider"));
        config.put("radius", NbtUtils.randomFloatProvider(random, 2.0f, 20.0f));
        config.putBoolean("use_bands", useBands);
        if (!shape.equals("cube")) config.putDouble("power", shape.equals("sphere") ? 2.0 :
                (shape.equals("octahedron") ? 1.0 : random.nextDouble(2.0)));
        return feature(config);
    }
}
