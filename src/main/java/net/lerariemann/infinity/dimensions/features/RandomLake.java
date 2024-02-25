package net.lerariemann.infinity.dimensions.features;

import net.lerariemann.infinity.dimensions.RandomFeaturesList;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;

import java.util.Arrays;

public class RandomLake extends RandomisedFeature {
    public RandomLake(RandomFeaturesList parent) {
        super(parent, "lake");
        save_with_placement();
    }

    void env_scan() {
        NbtList predicates = new NbtList();
        predicates.add(not(matchingBlocks("minecraft:air")));
        predicates.add(singleRule("inside_world_bounds", "offset", offsetToNbt(Arrays.asList(0, -5, 0))));
        NbtCompound res = singleRule("environment_scan", "direction_of_search", NbtString.of("down"));
        res.putInt("max_steps", 32);
        res.put("target_condition", singleRule("all_of", "predicates", predicates));
        placement_data.add(res);
    }

    void placement() {
        boolean surface = random.nextBoolean();
        if (surface) {
            addRarityFilter(1 + random.nextInt(50));
            addInSquare();
            addHeightmap("WORLD_SURFACE_WG");
            addBiome();
        }
        else {
            addRarityFilter(1 + random.nextInt(9));
            addInSquare();
            addHeightRange(fullHeightRange());
            env_scan();
            NbtCompound a = singleRule("surface_relative_threshold_filter", "heightmap", NbtString.of("OCEAN_FLOOR_WG"));
            a.putInt("max_inclusive", -5);
            placement_data.add(a);
            addBiome();
        }
    }

    NbtCompound feature() {
        NbtCompound config = new NbtCompound();
        NbtCompound block = genBlockOrFluid();
        config.put("fluid", PROVIDER.blockToProvider(block, random));
        addRandomBlockProvider(config, "barrier", "full_blocks_worldgen");
        return feature(config);
    }
}
