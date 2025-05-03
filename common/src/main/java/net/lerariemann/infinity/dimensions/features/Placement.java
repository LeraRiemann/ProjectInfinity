package net.lerariemann.infinity.dimensions.features;

import net.minecraft.nbt.*;

import java.util.List;

public class Placement {
    public NbtList data;

    public Placement() {
        data = new NbtList();
    }

    public void addSingleRule(String name, String name_param, NbtElement b) {
        data.add(singleRule(name, name_param, b));
    }

    public void addEmptyRule(String name) {
        data.add(ofType(name));
    }

    public void addBlockPredicateFilter(NbtCompound predicate) {
        addSingleRule("block_predicate_filter", "predicate", predicate);
    }

    public void addCount(int b) {
        addSingleRule("count", "count", NbtInt.of(b));
    }

    public void addCountEveryLayer(int b) {
        addSingleRule("count_on_every_layer", "count", NbtInt.of(b));
    }

    public void addRarityFilter(int b) {
        addSingleRule("rarity_filter", "chance", NbtInt.of(b));
    }

    public void addWaterDepthFilter(int b) {
        addSingleRule("surface_water_depth_filter", "max_water_depth", NbtInt.of(b));
    }

    public void addInSquare() {
        addEmptyRule("in_square");
    }

    public void addBiome() {
        addEmptyRule("biome");
    }

    public void addHeightmap(String s) {
        addSingleRule("heightmap", "heightmap", NbtString.of(s));
    }

    public void addHeightRange(NbtCompound heightProvider) {
        addSingleRule("height_range", "height", heightProvider);
    }

    public static NbtList everylayerBiome(int count) {
        Placement res = new Placement();
        res.addCountEveryLayer(count);
        res.addBiome();
        return res.data;
    }

    public static NbtList uniform(int count) {
        Placement res = new Placement();
        res.addCount(count);
        res.addInSquare();
        res.addHeightRange(fullHeightRange());
        return res.data;
    }

    public static NbtList floating(int chance, int a, int b) {
        Placement res = new Placement();
        res.addRarityFilter(chance);
        if (a==b) a+=1;
        res.addHeightRange(uniformHeightRange(Math.min(a, b), Math.max(a, b)));
        res.addBlockPredicateFilter(not(ofType("solid")));
        res.addBiome();
        return res.data;
    }

    public static NbtCompound singleRule(String type, String name_param, NbtElement b) {
        NbtCompound res = ofType(type);
        res.put(name_param, b);
        return res;
    }

    public static NbtCompound ofType(String type) {
        NbtCompound res = new NbtCompound();
        res.putString("type", "minecraft:"+type);
        return res;
    }

    public static NbtCompound fullHeightRange() {
        NbtCompound res = ofType("uniform");
        NbtCompound max_inclusive = new NbtCompound();
        NbtCompound min_inclusive = new NbtCompound();
        max_inclusive.putInt("below_top", 0);
        min_inclusive.putInt("above_bottom", 0);
        res.put("max_inclusive", max_inclusive);
        res.put("min_inclusive", min_inclusive);
        return res;
    }

    public static NbtCompound uniformHeightRange(int min, int max) {
        return heightRange(min, max, "uniform");
    }

    public static NbtCompound heightRange(int min, int max, String type) {
        NbtCompound res = ofType(type);
        NbtCompound max_inclusive = new NbtCompound();
        NbtCompound min_inclusive = new NbtCompound();
        max_inclusive.putInt("absolute", max);
        min_inclusive.putInt("absolute", min);
        res.put("max_inclusive", max_inclusive);
        res.put("min_inclusive", min_inclusive);
        return res;
    }

    public static NbtCompound matchingBlocks(String block) {
        return singleRule("matching_blocks", "blocks", NbtString.of(block));
    }

    public static NbtCompound matchingFluids(String s) {
        return singleRule("matching_fluids", "fluids", NbtString.of(s));
    }
    public static NbtCompound not(NbtCompound predicate) {
        return singleRule("not", "predicate", predicate);
    }

    public static NbtList offsetToNbt(List<Integer> offset) {
        NbtList offsetnbt = new NbtList();
        offset.forEach(a -> offsetnbt.add(NbtInt.of(a)));
        return offsetnbt;
    }
}
