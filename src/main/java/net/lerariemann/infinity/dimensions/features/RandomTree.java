package net.lerariemann.infinity.dimensions.features;

import net.lerariemann.infinity.dimensions.RandomFeaturesList;
import net.lerariemann.infinity.dimensions.RandomProvider;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;

public class RandomTree extends RandomisedFeature {
    boolean ishuge;

    public RandomTree(RandomFeaturesList parent, boolean placef) {
        super(parent, "tree", placef);
        ishuge = random.nextBoolean();
        id = type = "tree";
        save(1 + random.nextInt(20), (int) Math.floor(random.nextExponential()*4), parent.surface_block);
    }

    NbtCompound feature() {
        NbtCompound config = new NbtCompound();
        addRandomBlockProvider(config, "dirt_provider", "full_blocks");
        addRandomBlockProvider(config, "trunk_provider", "full_blocks");
        addRandomBlockProvider(config, "foliage_provider", "full_blocks");
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
        addRandomBlockProvider(res, "root_provider", "full_blocks");
        res.put("trunk_offset_y", RandomProvider.intProvider(random, 10, true));
        if (random.nextBoolean()) {
            NbtCompound above = new NbtCompound();
            addRandomBlockProvider(above, "above_root_provider", "blocks_features");
            above.putFloat("above_root_placement_chance", random.nextFloat());
            res.put("above_root_placement", above);
        }
        NbtCompound mangrove = new NbtCompound();
        mangrove.putInt("max_root_width", 1 + random.nextInt(ishuge ? 12 : 4));
        mangrove.putInt("max_root_length", 1 + random.nextInt(ishuge ? 64 : 8));
        mangrove.putFloat("random_skew_chance", random.nextFloat());
        mangrove.putString("can_grow_through", PROVIDER.randomName(random, "tags"));
        mangrove.putString("muddy_roots_in", PROVIDER.randomName(random, "tags"));
        addRandomBlockProvider(mangrove, "muddy_roots_provider", "full_blocks");
        res.put("mangrove_root_placement", mangrove);
        return res;
    }

    NbtCompound trunkPlacer() {
        NbtCompound res = new NbtCompound();
        res.putInt("base_height", random.nextInt(ishuge ? 32 : 8));
        res.putInt("height_rand_a", random.nextInt(ishuge ? 24 : 4));
        res.putInt("height_rand_b", random.nextInt(ishuge ? 24 : 4));
        String type = PROVIDER.randomName(random, "trunk_placers");
        res.putString("type", type);
        switch (type) {
            case "bending_trunk_placer" -> {
                res.put("bend_length", RandomProvider.intProvider(random, 1, ishuge ? 63 : 8, true));
                res.putInt("min_height_for_leaves", 1 + (int) Math.floor(random.nextExponential()));
            }
            case "upwards_branching_trunk_placer" -> {
                res.putInt("extra_branch_steps", 1 + (int) Math.floor(random.nextExponential()));
                res.putInt("extra_branch_length", (int) Math.floor(random.nextExponential()*3));
                res.putFloat("place_branch_per_log_probability", random.nextFloat());
                res.putString("can_grow_through", PROVIDER.randomName(random, "tags"));
            }
        }
        return res;
    }

    NbtCompound foliagePlacer() {
        NbtCompound res = new NbtCompound();
        res.put("radius", RandomProvider.intProvider(random, 1, ishuge ? 15 : 5, true));
        res.put("offset", RandomProvider.intProvider(random, ishuge ? 10 : 3, true));
        String type = PROVIDER.randomName(random, "foliage_placers");
        res.putString("type", type);
        switch (type) {
            case "blob_foliage_placer", "bush_foliage_placer", "fancy_foliage_placer", "jungle_foliage_placer" -> res.putInt("height", random.nextInt(16));
            case "pine_foliage_placer" -> res.put("height", RandomProvider.intProvider(random, ishuge ? 24 : 6, true));
            case "spruce_foliage_placer" -> res.put("trunk_height", RandomProvider.intProvider(random, ishuge ? 24 : 6, true));
            case "mega_pine_foliage_placer" -> res.put("crown_height", RandomProvider.intProvider(random, ishuge ? 24 : 6, true));
            case "random_spread_foliage_placer" -> {
                res.put("foliage_height", RandomProvider.intProvider(random, 1, ishuge ? 512 : 16, true));
                res.putInt("leaf_placement_attempts", random.nextInt(256));
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
            String type = PROVIDER.randomName(random, "tree_decorators");
            dec.putString("type", type);
            switch(type) {
                case "leave_vine", "cocoa", "beehive" -> dec.putFloat("probability", random.nextFloat());
                case "alter_ground" -> addRandomBlockProvider(dec, "provider", "blocks_features");
                case "attached_to_leaves" -> {
                    dec.putFloat("probability", random.nextFloat());
                    dec.putInt("exclusion_radius_xz", Math.min((int) Math.floor(random.nextExponential()*2), 16));
                    dec.putInt("exclusion_radius_y", Math.min((int) Math.floor(random.nextExponential()*2), 16));
                    dec.putInt("required_empty_blocks", 1 + Math.min((int) Math.floor(random.nextExponential()*2), 15));
                    addRandomBlockProvider(dec, "block_provider", "blocks_features");
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

