package net.lerariemann.infinity.dimensions.features;

import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.dimensions.CommonIO;
import net.lerariemann.infinity.dimensions.RandomFeaturesList;
import net.lerariemann.infinity.dimensions.RandomProvider;
import net.minecraft.nbt.NbtCompound;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public abstract class RandomisedFeature {
    protected final RandomProvider PROVIDER;
    String type;
    String id;
    String name;
    Random random;
    RandomFeaturesList parent;
    boolean place;
    public Set<String> BLOCKS;

    public RandomisedFeature(RandomFeaturesList lst, String namecore) {
        this(lst.random.nextInt(), lst, namecore, true);
    }
    public RandomisedFeature(RandomFeaturesList lst, String namecore, boolean placefeature) {
        this(lst.random.nextInt(), lst, namecore, placefeature);
    }

    public RandomisedFeature(int i, RandomFeaturesList lst, String namecore) {
        this(i, lst, namecore, true);
    }

    public RandomisedFeature(int i, RandomFeaturesList lst, String namecore, boolean placefeature) {
        random = new Random(i);
        id = namecore;
        name = namecore + "_" + i;
        parent = lst;
        PROVIDER = parent.PROVIDER;
        BLOCKS = new HashSet<>();
        place = placefeature;
    }

    public String fullName() {
        return InfinityMod.MOD_ID + ":" + name;
    }

    void save(Object... args) {
        NbtCompound data;
        String path = parent.storagePath;
        CommonIO.write(feature(), path + "/worldgen/configured_feature", name + ".json");
        if (place) {
            data = CommonIO.readCarefully(PROVIDER.configPath + "features/placements/" + type + ".json", args);
            data.putString("feature", fullName());
            CommonIO.write(data, path + "/worldgen/placed_feature", name + ".json");
        }
    }

    String genBlockOrFluid() {
        String block;
        if (RandomProvider.weighedRandom(random, 15, 1)) {
            block = PROVIDER.randomName(random, "blocks_features");
            BLOCKS.add(block);
        }
        else {
            block = PROVIDER.randomName(random, "fluids");
        }
        return block;
    }

    void addBlockCarefully(NbtCompound config, String key, String block) {
        config.put(key, RandomProvider.Block(block));
    }

    void addBlock(NbtCompound config, String key, String block) {
        addBlockCarefully(config, key, block);
        BLOCKS.add(block);
    }

    void addBlockProviderCarefully(NbtCompound config, String key, String block) {
        config.put(key, RandomProvider.blockToProvider(RandomProvider.Block(block)));
    }

    void addRandomBlockProvider(NbtCompound config, String key, String group) {
        String block = PROVIDER.randomName(random, group);
        addBlockProviderCarefully(config, key, block);
        BLOCKS.add(block);
    }

    void addRandomBlock(NbtCompound config, String key, String group) {
        addBlock(config, key, PROVIDER.randomName(random, group));
    }

    abstract NbtCompound feature();

    NbtCompound feature(NbtCompound config) {
        NbtCompound res = new NbtCompound();
        res.putString("type", id);
        res.put("config", config);
        return res;
    }
}
