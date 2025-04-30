package net.lerariemann.infinity.dimensions.features.lakes;

import net.lerariemann.infinity.dimensions.RandomFeaturesList;
import net.lerariemann.infinity.dimensions.features.Placement;
import net.lerariemann.infinity.dimensions.features.RandomisedFeature;
import net.lerariemann.infinity.util.core.ConfigType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;

import java.util.Arrays;

import static net.lerariemann.infinity.dimensions.features.Placement.*;

public class RandomLake extends RandomisedFeature {
    public RandomLake(RandomFeaturesList parent) {
        super(parent, "lake");
        savePlacement();
    }

    void env_scan(Placement placement) {
        NbtList predicates = new NbtList();
        predicates.add(not(matchingBlocks("minecraft:air")));
        predicates.add(singleRule("inside_world_bounds", "offset", offsetToNbt(Arrays.asList(0, -5, 0))));
        NbtCompound res = singleRule("environment_scan", "direction_of_search", NbtString.of("down"));
        res.putInt("max_steps", 32);
        res.put("target_condition", singleRule("all_of", "predicates", predicates));
        placement.data.add(res);
    }

    @Override
    public NbtList placement() {
        Placement res = new Placement();
        boolean surface = random.nextBoolean();
        if (surface) {
            res.addRarityFilter(1 + random.nextInt(50));
            res.addInSquare();
            res.addHeightmap("WORLD_SURFACE_WG");
            res.addBiome();
        }
        else {
            res.addRarityFilter(1 + random.nextInt(9));
            res.addInSquare();
            res.addHeightRange(fullHeightRange());
            env_scan(res);
            NbtCompound a = singleRule("surface_relative_threshold_filter", "heightmap", NbtString.of("OCEAN_FLOOR_WG"));
            a.putInt("max_inclusive", -5);
            res.data.add(a);
            res.addBiome();
        }
        return res.data;
    }

    public NbtCompound feature() {
        NbtCompound config = new NbtCompound();
        NbtCompound block = genBlockOrFluid();
        config.put("fluid", PROVIDER.blockToProvider(block, random));
        addRandomBlockProvider(config, "barrier", ConfigType.FULL_BLOCKS_WG);
        return feature(config);
    }
}
