package net.lerariemann.infinity.dimensions.features;

import net.lerariemann.infinity.dimensions.RandomProvider;
import net.minecraft.nbt.NbtCompound;

import java.util.List;

public class RandomFungus extends  RandomisedFeature {
    List<String> validbaseblocks;
    public RandomFungus(int i, RandomProvider provider, String path, List<String> valid_base_blocks) {
        super(i, provider);
        name = "fungus_" + i;
        id = "huge_fungus";
        type = "everylayer";
        validbaseblocks = valid_base_blocks;
        save(path,1 + random.nextInt(10));
    }

    NbtCompound feature() {
        NbtCompound config = new NbtCompound();
        addRandomBlock(config, "hat_state");
        addRandomBlock(config, "decor_state");
        addRandomBlock(config, "stem_state");
        addBlockCarefully(config, "valid_base_block", validbaseblocks.get(random.nextInt(validbaseblocks.size())));
        return feature(config);
    }
}
