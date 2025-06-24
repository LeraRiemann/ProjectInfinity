package net.lerariemann.infinity.dimensions.features.fluid_springs;

import net.lerariemann.infinity.dimensions.RandomFeaturesList;
import net.lerariemann.infinity.dimensions.features.Placement;
import net.lerariemann.infinity.dimensions.features.RandomisedFeature;
import net.lerariemann.infinity.util.core.ConfigType;
import net.lerariemann.infinity.util.core.NbtUtils;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;

import static net.lerariemann.infinity.dimensions.features.Placement.heightRange;

public class RandomSpring extends RandomisedFeature {
    public RandomSpring(RandomFeaturesList lst) {
        super(lst, "spring");
        id = "spring_feature";
        savePlacement();
    }

    public NbtCompound feature() {
        NbtCompound config = new NbtCompound();
        boolean whack = random.nextDouble() < 0.1;
        if (whack) {
            config.putInt("hole_count", random.nextInt(7));
            config.putInt("rock_count", random.nextInt(7));
        }
        else {
            boolean open = random.nextBoolean();
            config.putInt("hole_count", open ? 1 : 0);
            config.putInt("rock_count", open ? 4 : 5);
        }
        config.putBoolean("requires_block_below", random.nextBoolean());
        config.put("state", NbtUtils.nameToElement(PROVIDER.randomName(random, ConfigType.FLUIDS)));
        config.putString("valid_blocks", NbtUtils.getString(daddy.default_block, "Name"));
        return feature(config);
    }

    public NbtList placement() {
        Placement res = new Placement();
        int center = random.nextInt(daddy.min_y, daddy.min_y + daddy.height);
        int sigma = random.nextInt(daddy.height);
        res.addCount(1 + random.nextInt(32));
        res.addInSquare();
        res.addHeightRange(heightRange(center - sigma,center + sigma, "trapezoid"));
        res.addBiome();
        return res.data;
    }
}
