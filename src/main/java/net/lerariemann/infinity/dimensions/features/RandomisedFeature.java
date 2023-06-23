package net.lerariemann.infinity.dimensions.features;

import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.dimensions.RandomDimension;
import net.lerariemann.infinity.util.CommonIO;
import net.lerariemann.infinity.dimensions.RandomFeaturesList;
import net.lerariemann.infinity.dimensions.RandomProvider;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;

import java.util.Random;

public abstract class RandomisedFeature {
    protected final RandomProvider PROVIDER;
    String type;
    String id;
    String name;
    Random random;
    RandomFeaturesList parent;
    RandomDimension daddy;
    boolean place;

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
        daddy = parent.parent.parent;
        PROVIDER = parent.PROVIDER;
        place = placefeature;
    }

    public String fullName() {
        return InfinityMod.MOD_ID + ":" + name;
    }

    <T> boolean does_not_contain(RegistryKey<? extends Registry<T>> key) {
        return daddy.does_not_contain(key, name);
    }

    void save(Object... args) {
        NbtCompound data;
        String path = parent.storagePath;
        if (does_not_contain(RegistryKeys.CONFIGURED_FEATURE)) CommonIO.write(feature(), path + "/worldgen/configured_feature", name + ".json");
        if (place && (does_not_contain(RegistryKeys.PLACED_FEATURE))) {
            data = CommonIO.readCarefully(PROVIDER.configPath + "features/placements/" + type + ".json", args);
            data.putString("feature", fullName());
            CommonIO.write(data, path + "/worldgen/placed_feature", name + ".json");
        }
    }

    NbtCompound genBlockOrFluid() {
        NbtCompound block;
        if (parent.roll("solid_lakes")) {
            block = PROVIDER.randomBlock(random, "blocks_features");
        }
        else {
            block = PROVIDER.randomBlock(random, "fluids");
        }
        return block;
    }

    void addRandomBlockProvider(NbtCompound config, String key, String group) {
        NbtCompound block = PROVIDER.randomBlock(random, group);
        config.put(key, PROVIDER.blockToProvider(block, random));
    }

    void addRandomBlock(NbtCompound config, String key, String group) {
        NbtCompound block = PROVIDER.randomBlock(random, group);
        config.put(key, block);
    }

    void addRandomIntProvider(NbtCompound config, String key, int lbound, int bound) {
        config.put(key, RandomProvider.intProvider(random, lbound, bound, true));
    }

    abstract NbtCompound feature();

    NbtCompound feature(NbtCompound config) {
        NbtCompound res = new NbtCompound();
        res.putString("type", id);
        res.put("config", config);
        return res;
    }
}
