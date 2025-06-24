package net.lerariemann.infinity.dimensions.features.underground_ores;

import net.lerariemann.infinity.dimensions.RandomFeaturesList;
import net.lerariemann.infinity.dimensions.features.Placement;
import net.lerariemann.infinity.dimensions.features.RandomisedFeature;
import net.lerariemann.infinity.util.core.ConfigType;
import net.lerariemann.infinity.util.core.NbtUtils;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;

import static net.lerariemann.infinity.dimensions.features.Placement.heightRange;

public class RandomOre extends RandomisedFeature {

    public RandomOre(RandomFeaturesList parent) {
        super(parent, "ore");
        id = (parent.PROVIDER.roll(random, "scatter_ores")) ? "scattered_ore" : "ore";
        savePlacement();
    }

    public NbtList placement() {
        Placement res = new Placement();
        int center = random.nextInt(daddy.min_y, daddy.min_y + daddy.height);
        int sigma = random.nextInt(daddy.height);
        res.addCount(1 + random.nextInt(128));
        res.addInSquare();
        res.addHeightRange(heightRange(center - sigma,center + sigma, "trapezoid"));
        res.addBiome();
        return res.data;
    }

    public NbtCompound feature() {
        NbtCompound config = new NbtCompound();
        config.putInt("size", 1 + Math.min(63, (int)Math.floor(random.nextExponential()*4)));
        config.putFloat("discard_chance_on_air_exposure", Math.max(0.0f, Math.min(1.0f, (float)(random.nextExponential()*0.2))));
        NbtCompound block = PROVIDER.randomElement(random, ConfigType.FULL_BLOCKS_WG);
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
        if (!awt) res.putString("block", NbtUtils.getString(block, "Name"));
        return res;
    }
}
