package net.lerariemann.infinity.dimensions.features.vegetation;

import net.lerariemann.infinity.dimensions.RandomFeaturesList;
import net.lerariemann.infinity.dimensions.features.Placement;
import net.lerariemann.infinity.dimensions.features.RandomisedFeature;
import net.lerariemann.infinity.util.core.ConfigType;
import net.lerariemann.infinity.util.core.NbtUtils;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;

import java.util.Arrays;

import static net.lerariemann.infinity.dimensions.features.Placement.matchingBlocks;
import static net.lerariemann.infinity.dimensions.features.Placement.offsetToNbt;

public class RandomTree extends RandomisedFeature {
    boolean ishuge;

    public RandomTree(RandomFeaturesList parent) {
        super(parent, "tree");
        ishuge = parent.roll("huge_trees");
        id = "tree";
        savePlacement();
    }

    public NbtList placement() {
        Placement res = new Placement();
        NbtCompound predicate = matchingBlocks(NbtUtils.getString(parent.surface_block, "Name"));
        predicate.put("offset", offsetToNbt(Arrays.asList(0, -1, 0)));
        res.addCountEveryLayer(1);
        res.addWaterDepthFilter((int) Math.floor(random.nextExponential()*4));
        res.addBlockPredicateFilter(predicate);
        return res.data;
    }

    public NbtCompound feature() {
        NbtCompound config = new NbtCompound();
        addRandomBlockProvider(config, "dirt_provider", ConfigType.FULL_BLOCKS);
        addRandomBlockProvider(config, "trunk_provider", ConfigType.FULL_BLOCKS_WG);
        addRandomBlockProvider(config, "foliage_provider", ConfigType.FULL_BLOCKS_WG);
        config.putBoolean("force_dirt", random.nextBoolean());
        config.putBoolean("ignore_vines", random.nextBoolean());
        config.put("trunk_placer", trunkPlacer());
        config.put("foliage_placer", foliagePlacer());
        config.put("minimum_size", minimum_size());
        config.put("decorators", decorators());
        if (random.nextBoolean()) config.put("root_placer", rootPlacer());
        return feature(config);
    }

    NbtCompound rootPlacer() {
        NbtCompound res = new NbtCompound();
        res.putString("type", "mangrove_root_placer");
        addRandomBlockProvider(res, "root_provider", ConfigType.FULL_BLOCKS);
        res.put("trunk_offset_y", NbtUtils.randomIntProvider(random, 10, true));
        if (random.nextBoolean()) {
            NbtCompound above = new NbtCompound();
            addRandomBlockProvider(above, "above_root_provider", ConfigType.BLOCKS_FEATURES);
            above.putFloat("above_root_placement_chance", random.nextFloat());
            res.put("above_root_placement", above);
        }
        NbtCompound mangrove = new NbtCompound();
        mangrove.putInt("max_root_width", 1 + random.nextInt(ishuge ? 12 : 4));
        mangrove.putInt("max_root_length", 1 + random.nextInt(ishuge ? 64 : 8));
        mangrove.putFloat("random_skew_chance", random.nextFloat());
        mangrove.putString("can_grow_through", "#" + PROVIDER.randomName(random, ConfigType.TAGS));
        mangrove.putString("muddy_roots_in", "#" + PROVIDER.randomName(random, ConfigType.TAGS));
        addRandomBlockProvider(mangrove, "muddy_roots_provider", ConfigType.FULL_BLOCKS);
        res.put("mangrove_root_placement", mangrove);
        return res;
    }

    NbtCompound trunkPlacer() {
        NbtCompound res = new NbtCompound();
        res.putInt("base_height", random.nextInt(ishuge ? 32 : 8));
        res.putInt("height_rand_a", random.nextInt(ishuge ? 24 : 4));
        res.putInt("height_rand_b", random.nextInt(ishuge ? 24 : 4));
        String type = PROVIDER.randomName(random, ConfigType.TRUNK_PLACERS);
        res.putString("type", type);
        switch (type) {
            case "bending_trunk_placer" -> {
                addRandomIntProvider(res, "bend_length", 1, ishuge ? 63 : 8);
                res.putInt("min_height_for_leaves", 1 + (int) Math.floor(random.nextExponential()));
            }
            case "upwards_branching_trunk_placer" -> {
                res.putInt("extra_branch_steps", 1 + (int) Math.floor(random.nextExponential()));
                res.putInt("extra_branch_length", (int) Math.floor(random.nextExponential()*3));
                res.putFloat("place_branch_per_log_probability", random.nextFloat());
                res.putString("can_grow_through", "#" + PROVIDER.randomName(random, ConfigType.TAGS));
            }
            case "cherry_trunk_placer" -> {
                addRandomIntProvider(res, "branch_count", 1, 3);
                addRandomIntProvider(res, "branch_horizontal_length", 2, 16);
                NbtCompound branch_start_offset_from_top = new NbtCompound();
                int a = random.nextInt(-16, 0);
                int b = random.nextInt(-16, 0);
                if (a==b) {
                    if (a==0) a-=1;
                    else a+=1;
                }
                branch_start_offset_from_top.putInt("min_inclusive", Math.min(a, b));
                branch_start_offset_from_top.putInt("max_inclusive", Math.max(a, b));
                res.put("branch_start_offset_from_top", branch_start_offset_from_top);
                addRandomIntProvider(res, "branch_end_offset_from_top", -16, 16);
            }
            case "infinity:wonky" -> {
                float a = random.nextFloat();
                float b = random.nextFloat();
                res.putFloat("weight_up", Math.max(a, b));
                res.putFloat("weight_down", Math.min(a, b));
                res.putFloat("weight_side", random.nextFloat());
            }
        }
        return res;
    }

    NbtCompound foliagePlacer() {
        NbtCompound res = new NbtCompound();
        addRandomIntProvider(res, "radius", 1, ishuge ? 15 : 5);
        addRandomIntProvider(res, "offset", 1, ishuge ? 10 : 3);
        String type = PROVIDER.randomName(random, ConfigType.FOLIAGE_PLACERS);
        res.putString("type", type);
        switch (type) {
            case "blob_foliage_placer", "bush_foliage_placer", "fancy_foliage_placer", "jungle_foliage_placer" -> res.putInt("height", random.nextInt(16));
            case "pine_foliage_placer" -> res.put("height", NbtUtils.randomIntProvider(random, ishuge ? 24 : 6, true));
            case "spruce_foliage_placer" -> res.put("trunk_height", NbtUtils.randomIntProvider(random, ishuge ? 24 : 6, true));
            case "mega_pine_foliage_placer" -> res.put("crown_height", NbtUtils.randomIntProvider(random, ishuge ? 24 : 6, true));
            case "random_spread_foliage_placer" -> {
                addRandomIntProvider(res, "foliage_height", 1, ishuge ? 512 : 16);
                res.putInt("leaf_placement_attempts", random.nextInt(256));
            }
            case "cherry_foliage_placer" -> {
                res.put("height", NbtUtils.randomIntProvider(random, 4, 16, true));
                res.putFloat("wide_bottom_layer_hole_chance", random.nextFloat());
                res.putFloat("corner_hole_chance", random.nextFloat());
                res.putFloat("hanging_leaves_chance", random.nextFloat());
                res.putFloat("hanging_leaves_extension_chance", random.nextFloat());
            }
        }
        return res;
    }

    NbtCompound minimum_size() {
        NbtCompound res = new NbtCompound();
        if (random.nextBoolean()) res.putFloat("min_clipped_height", Math.min(80.0f, (float) random.nextExponential()*3));
        boolean i = random.nextBoolean();
        if (i) {
            res.putString("type", "two_layers_feature_size");
        }
        else {
            res.putString("type", "three_layers_feature_size");
            if (random.nextBoolean()) res.putInt("upper_limit", random.nextInt(80));
            if (random.nextBoolean()) res.putInt("middle_size", Math.min(16, (int)Math.floor(random.nextExponential()*2)));
        }
        if (random.nextBoolean()) res.putInt("limit", random.nextInt(80));
        if (random.nextBoolean()) res.putInt("lower_size", Math.min(16, (int)Math.floor(random.nextExponential()*2)));
        if (random.nextBoolean()) res.putInt("upper_size", Math.min(16, (int)Math.floor(random.nextExponential()*2)));
        return res;
    }

    NbtList decorators() {
        NbtList res = new NbtList();
        int num = (int) Math.floor(random.nextExponential());
        for (int i = 0; i < num; i+=1) {
            NbtCompound dec = new NbtCompound();
            String type = PROVIDER.randomName(random, ConfigType.TREE_DECORATORS);
            dec.putString("type", type);
            switch(type) {
                case "leave_vine", "cocoa", "beehive" -> dec.putFloat("probability", random.nextFloat());
                case "alter_ground" -> addRandomBlockProvider(dec, "provider", ConfigType.BLOCKS_FEATURES);
                case "attached_to_leaves" -> {
                    dec.putFloat("probability", random.nextFloat());
                    dec.putInt("exclusion_radius_xz", Math.min((int) Math.floor(random.nextExponential()*2), 16));
                    dec.putInt("exclusion_radius_y", Math.min((int) Math.floor(random.nextExponential()*2), 16));
                    dec.putInt("required_empty_blocks", 1 + Math.min((int) Math.floor(random.nextExponential()*2), 15));
                    addRandomBlockProvider(dec, "block_provider", ConfigType.BLOCKS_FEATURES);
                    NbtList directions = new NbtList();
                    String[] dirs = {"up", "down", "north", "south", "west", "east"};
                    int j = 0;
                    for (String k: dirs) {
                        if (random.nextBoolean()) {
                            j+=1;
                            directions.add(NbtString.of(k));
                        }
                    }
                    if (j==0) {
                        directions.add(NbtString.of(dirs[random.nextInt(6)]));
                    }
                    dec.put("directions", directions);
                }
            }
            res.add(dec);
        }
        return res;
    }
}

