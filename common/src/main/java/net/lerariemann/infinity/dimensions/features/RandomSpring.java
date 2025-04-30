package net.lerariemann.infinity.dimensions.features;

import net.lerariemann.infinity.dimensions.RandomFeaturesList;
import net.lerariemann.infinity.util.core.ConfigType;
import net.lerariemann.infinity.util.core.NbtUtils;
import net.minecraft.nbt.NbtCompound;

public class RandomSpring extends RandomisedFeature {
    public RandomSpring(RandomFeaturesList lst) {
        super(lst, "spring");
        id = "spring_feature";
        save_with_placement();
    }

    @Override
    NbtCompound feature() {
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
        config.putString("valid_blocks", daddy.default_block.getString("Name"));
        return feature(config);
    }

    @Override
    void placement() {
        int center = random.nextInt(daddy.min_y, daddy.min_y + daddy.height);
        int sigma = random.nextInt(daddy.height);
        addCount(1 + random.nextInt(32));
        addInSquare();
        addHeightRange(heightRange(center - sigma,center + sigma, "trapezoid"));
        addBiome();
    }
}
