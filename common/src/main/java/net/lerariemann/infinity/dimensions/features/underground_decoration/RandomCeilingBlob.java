package net.lerariemann.infinity.dimensions.features.underground_decoration;

import net.lerariemann.infinity.dimensions.RandomFeaturesList;
import net.lerariemann.infinity.dimensions.features.Placement;
import net.lerariemann.infinity.dimensions.features.RandomisedFeature;
import net.lerariemann.infinity.util.core.ConfigType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;

import static net.lerariemann.infinity.dimensions.features.Placement.fullHeightRange;

public class RandomCeilingBlob extends RandomisedFeature {
    public RandomCeilingBlob(RandomFeaturesList parent) {
        super(parent, "ceilingblob");
        id = "infinity:random_ceiling_blob";
        savePlacement();
    }

    public NbtList placement() {
        Placement res = new Placement();
        res.addCount(random.nextInt(1, daddy.height/8));
        res.addInSquare();
        res.addHeightRange(fullHeightRange());
        res.addBiome();
        return res.data;
    }

    public NbtCompound feature() {
        NbtCompound config = new NbtCompound();
        NbtList targets = new NbtList();
        targets.add(daddy.default_block);
        targets.addAll(daddy.additional_blocks);
        config.put("targets", targets);
        addRandomBlockProvider(config, "block", ConfigType.FULL_BLOCKS);
        config.putInt("size_xz", random.nextInt(5, 12));
        config.putInt("size_y", random.nextInt(8, 16));
        return feature(config);
    }
}
