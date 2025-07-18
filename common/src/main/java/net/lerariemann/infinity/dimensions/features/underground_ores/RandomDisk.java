package net.lerariemann.infinity.dimensions.features.underground_ores;

import net.lerariemann.infinity.dimensions.RandomFeaturesList;
import net.lerariemann.infinity.dimensions.features.Placement;
import net.lerariemann.infinity.dimensions.features.RandomisedFeature;
import net.lerariemann.infinity.util.core.ConfigType;
import net.lerariemann.infinity.util.core.NbtUtils;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;

import static net.lerariemann.infinity.dimensions.features.Placement.matchingFluids;

public class RandomDisk extends RandomisedFeature {
    String target;
    public RandomDisk(RandomFeaturesList parent) {
        super(parent, "disk");
        target = NbtUtils.getString(daddy.underwater.get(parent.parent.fullname), "Name");
        id = "disk";
        savePlacement();
    }

    public NbtList placement() {
        Placement res = new Placement();
        res.addInSquare();
        res.addHeightmap("OCEAN_FLOOR_WG");
        String s = NbtUtils.getString(daddy.default_fluid, "fluidName");
        if (!s.equals("minecraft:air")) res.addBlockPredicateFilter(matchingFluids(s));
        res.addBiome();
        return res.data;
    }

    public NbtCompound feature() {
        NbtCompound config = new NbtCompound();
        NbtCompound blockProvider = PROVIDER.randomBlockProvider(random, ConfigType.FULL_BLOCKS_WG);
        config.putInt("half_height", random.nextInt(5));
        addRandomIntProvider(config, "radius", 0, 9);
        NbtCompound targets = new NbtCompound();
        boolean awt = PROVIDER.roll(random, "ores_spawn_everywhere");
        targets.putString("type", awt ? "true" : "matching_blocks");
        if (!awt) {
            NbtList blocks = new NbtList();
            blocks.add(NbtString.of(target));
            blocks.add(NbtString.of(NbtUtils.getString(NbtUtils.getCompound(blockProvider, "state"), "Name")));
            targets.put("blocks", blocks);
        }
        config.put("target", targets);
        NbtCompound state_provider = new NbtCompound();
        state_provider.put("fallback", blockProvider);
        state_provider.put("rules", new NbtList());
        config.put("state_provider", state_provider);
        return feature(config);
    }
}
