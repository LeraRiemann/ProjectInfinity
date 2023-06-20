package net.lerariemann.infinity.dimensions.features;

import net.lerariemann.infinity.dimensions.RandomDimension;
import net.lerariemann.infinity.dimensions.RandomFeaturesList;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;

public class RandomOre extends RandomisedFeature {
    RandomDimension daddy;

    public RandomOre(RandomFeaturesList parent) {
        super(parent, "ore");
        daddy = parent.parent.parent;
        type = "ore";
        id = (parent.PROVIDER.roll(random, "scatter_ores")) ? "scattered_ore" : "ore";
        int center = random.nextInt(daddy.min_y, daddy.min_y + daddy.height);
        int sigma = random.nextInt(daddy.height);
        save(1 + random.nextInt(128), center + sigma, center - sigma);
    }

    NbtCompound feature() {
        NbtCompound config = new NbtCompound();
        config.putInt("size", 1 + Math.min(63, (int)Math.floor(random.nextExponential()*4)));
        config.putFloat("discard_chance_on_air_exposure", Math.min(1.0f, (float)(random.nextExponential()*0.2)));
        NbtCompound block = PROVIDER.randomBlock(random, "blocks_features");
        NbtList targets = new NbtList();
        int j = daddy.additional_blocks.size();
        for (int i = 0; i < j + 1; i++) {
            NbtCompound block2 = (i < j) ? daddy.additional_blocks.get(i) : daddy.default_block;
            NbtCompound target = new NbtCompound();
            target.put("state", block);
            target.put("target", target(block2, PROVIDER.roll(random, "ores_spawn_everywhere")));
            targets.add(target);
        }
        config.put("targets", targets);
        return feature(config);
    }

    static NbtCompound target(NbtCompound block, boolean awt) {
        NbtCompound res = new NbtCompound();
        res.putString("predicate_type", awt ? "always_true" : "block_match");
        if (!awt) res.putString("block", block.getString("Name"));
        return res;
    }
}
