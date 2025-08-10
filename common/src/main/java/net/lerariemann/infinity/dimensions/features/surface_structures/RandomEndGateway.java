package net.lerariemann.infinity.dimensions.features.surface_structures;

import net.lerariemann.infinity.dimensions.RandomFeaturesList;
import net.lerariemann.infinity.dimensions.features.Placement;
import net.lerariemann.infinity.dimensions.features.RandomisedFeature;
import net.lerariemann.infinity.util.core.ConfigType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;

public class RandomEndGateway extends RandomisedFeature {
    public RandomEndGateway(RandomFeaturesList parent) {
        super(parent, "gateway");
        id = "infinity:end_gateway";
        savePlacement();
    }

    public NbtList placement() {
        int a = daddy.min_y + random.nextInt(daddy.height);
        int b = daddy.min_y + random.nextInt(daddy.height);
        return Placement.floating(random.nextInt(1,33), a, b);
    }

    public NbtCompound feature() {
        NbtCompound config = new NbtCompound();
        config.putInt("spread", (int)(1000 * Math.pow(2, 2 * random.nextDouble() - 1)));
        if (PROVIDER.roll(random, "gateways_random_block")) addRandomBlock(config, "block", ConfigType.FULL_BLOCKS);
        return feature(config);
    }
}
