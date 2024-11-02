package net.lerariemann.infinity.dimensions.features;

import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.dimensions.RandomDimension;
import net.lerariemann.infinity.util.CommonIO;
import net.lerariemann.infinity.dimensions.RandomFeaturesList;
import net.lerariemann.infinity.util.RandomProvider;
import net.minecraft.nbt.*;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;

import java.util.List;
import java.util.Random;

public abstract class RandomisedFeature {
    protected final RandomProvider PROVIDER;
    String id;
    String name;
    Random random;
    RandomFeaturesList parent;
    RandomDimension daddy;
    NbtList placement_data;

    public RandomisedFeature(RandomFeaturesList lst, String namecore) {
        this(lst.PROVIDER.rule("longArithmeticEnabled") ? lst.random.nextLong() : lst.random.nextInt(), lst, namecore);
    }

    public RandomisedFeature(long i, RandomFeaturesList lst, String namecore) {
        random = new Random(i);
        id = namecore;
        name = namecore + "_" + i;
        parent = lst;
        daddy = parent.parent.parent;
        PROVIDER = parent.PROVIDER;
        placement_data = new NbtList();
    }

    public String fullName() {
        return InfinityMod.MOD_ID + ":" + name;
    }

    public String fullNameConfigured() {
        return InfinityMod.MOD_ID + ":configured_" + name;
    }

    <T> boolean does_not_contain(RegistryKey<? extends Registry<T>> key) {
        return daddy.does_not_contain(key, name);
    }

    void save_with_placement() {
        if (does_not_contain(RegistryKeys.CONFIGURED_FEATURE)) CommonIO.write(feature(),
                parent.storagePath + "/worldgen/configured_feature", "configured_" + name + ".json");
        NbtCompound moredata = new NbtCompound();
        moredata.putString("feature", fullNameConfigured());
        placement();
        moredata.put("placement", placement_data);
        CommonIO.write(moredata, parent.storagePath + "/worldgen/placed_feature", name + ".json");
    }

    NbtCompound genBlockOrFluid() {
        NbtCompound block, block2;
        if (parent.roll("solid_lakes")) {
            block2 = PROVIDER.randomBlock(random, "blocks_features");
        }
        else {
            block = PROVIDER.randomBlock(random, "fluids");
            block2 = RandomProvider.Block(block.getString("Name"));
        }
        return block2;
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

    abstract void placement();

    void placement_everylayer_biome(int count) {
        addCountEveryLayer(count);
        addBiome();
    }

    void placement_uniform(int count) {
        addCount(count);
        addInSquare();
        addHeightRange(fullHeightRange());
    }

    void placement_floating(int chance, int a, int b) {
        addRarityFilter(chance);
        if (a==b) a+=1;
        addHeightRange(uniformHeightRange(Math.min(a, b), Math.max(a, b)));
        addBlockPredicateFilter(not(ofType("solid")));
        addBiome();
    }

    NbtCompound feature(NbtCompound config) {
        NbtCompound res = new NbtCompound();
        res.putString("type", id);
        res.put("config", config);
        return res;
    }

    static NbtCompound singleRule(String type, String name_param, NbtElement b) {
        NbtCompound res = ofType(type);
        res.put(name_param, b);
        return res;
    }

    static NbtCompound ofType(String type) {
        NbtCompound res = new NbtCompound();
        res.putString("type", "minecraft:"+type);
        return res;
    }

    void addSingleRule(String name, String name_param, NbtElement b) {
        placement_data.add(singleRule(name, name_param, b));
    }

    void addEmptyRule(String name) {
        placement_data.add(ofType(name));
    }

    void addBlockPredicateFilter(NbtCompound predicate) {
        addSingleRule("block_predicate_filter", "predicate", predicate);
    }

    void addCount(int b) {
        addSingleRule("count", "count", NbtInt.of(b));
    }

    void addCountEveryLayer(int b) {
        addSingleRule("count_on_every_layer", "count", NbtInt.of(b));
    }

    void addRarityFilter(int b) {
        addSingleRule("rarity_filter", "chance", NbtInt.of(b));
    }

    void addWaterDepthFilter(int b) {
        addSingleRule("surface_water_depth_filter", "max_water_depth", NbtInt.of(b));
    }

    void addInSquare() {
        addEmptyRule("in_square");
    }

    void addBiome() {
        addEmptyRule("biome");
    }

    void addHeightmap(String s) {
        addSingleRule("heightmap", "heightmap", NbtString.of(s));
    }

    void addHeightRange(NbtCompound heightProvider) {
        addSingleRule("height_range", "height", heightProvider);
    }

    static NbtCompound fullHeightRange() {
        NbtCompound res = ofType("uniform");
        NbtCompound max_inclusive = new NbtCompound();
        NbtCompound min_inclusive = new NbtCompound();
        max_inclusive.putInt("below_top", 0);
        min_inclusive.putInt("above_bottom", 0);
        res.put("max_inclusive", max_inclusive);
        res.put("min_inclusive", min_inclusive);
        return res;
    }

    static NbtCompound uniformHeightRange(int min, int max) {
        return heightRange(min, max, "uniform");
    }

    static NbtCompound heightRange(int min, int max, String type) {
        NbtCompound res = ofType(type);
        NbtCompound max_inclusive = new NbtCompound();
        NbtCompound min_inclusive = new NbtCompound();
        max_inclusive.putInt("absolute", max);
        min_inclusive.putInt("absolute", min);
        res.put("max_inclusive", max_inclusive);
        res.put("min_inclusive", min_inclusive);
        return res;
    }

    static NbtCompound matchingBlocks(String block) {
        return singleRule("matching_blocks", "blocks", NbtString.of(block));
    }

    static NbtCompound matchingFluids(String s) {
        return singleRule("matching_fluids", "fluids", NbtString.of(s));
    }

    static NbtCompound matchingWater() {
        return matchingFluids("minecraft:water");
    }

    static NbtCompound matchingWaterOffset(NbtList offset) {
        NbtCompound res = matchingWater();
        res.put("offset", offset);
        return res;
    }
    static NbtCompound not(NbtCompound predicate) {
        return singleRule("not", "predicate", predicate);
    }

    static NbtList offsetToNbt(List<Integer> offset) {
        NbtList offsetnbt = new NbtList();
        offset.forEach(a -> offsetnbt.add(NbtInt.of(a)));
        return offsetnbt;
    }
}