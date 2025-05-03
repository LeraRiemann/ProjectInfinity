package net.lerariemann.infinity.dimensions.features.vegetation;

import net.lerariemann.infinity.dimensions.RandomFeaturesList;
import net.lerariemann.infinity.dimensions.features.Placement;
import net.lerariemann.infinity.dimensions.features.RandomisedFeature;
import net.lerariemann.infinity.util.core.ConfigType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;

public class RandomMushroom extends RandomisedFeature {
    public RandomMushroom(RandomFeaturesList parent) {
        super(parent, "mushroom");
        id = random.nextBoolean() ? "infinity:random_flat_mushroom" : "infinity:random_round_mushroom";
        savePlacement();
    }

    public NbtList placement() {
        Placement res = new Placement();
        res.addCountEveryLayer(1);
        return res.data;
    }

    public NbtCompound feature() {
        NbtCompound config = new NbtCompound();
        addRandomBlockProvider(config, "cap_provider", ConfigType.FULL_BLOCKS_WG);
        addRandomBlockProvider(config, "stem_provider", ConfigType.FULL_BLOCKS_WG);
        config.put("valid_base_block", parent.surface_block);
        config.putInt("foliage_radius", random.nextBoolean() ? 2 : 1 + (int)Math.floor(random.nextExponential()*2));
        config.putInt("height", random.nextBoolean() ? 5 : random.nextInt(3, 7));
        return feature(config);
    }
}
