package net.lerariemann.infinity.dimensions;

import net.lerariemann.infinity.dimensions.features.*;
import net.lerariemann.infinity.util.RandomProvider;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;

import java.util.*;

public class RandomFeaturesList {
    public NbtList data;
    public final RandomProvider PROVIDER;
    public String storagePath;

    public Random random;
    public NbtCompound surface_block;
    public RandomBiome parent;
    public List<String> blocks;
    public boolean useVanillaFeatures;

    RandomFeaturesList(RandomBiome biome) {
        parent = biome;
        random = biome.random;
        PROVIDER = biome.PROVIDER;
        surface_block = parent.parent.top_blocks.get(parent.fullname);
        storagePath = biome.parent.getStoragePath();
        blocks = new ArrayList<>();
        useVanillaFeatures = roll("generate_vanilla_features");
        data = new NbtList();
        data.add(endIsland());
        data.add(lakes());
        data.add(localModifications());
        data.add(undergroundStructures());
        data.add(surfaceStructures());
        data.add(getAllElements("strongholds"));
        data.add(undergroundOres());
        data.add(undergroundDecoration());
        data.add(getAllElements("fluidsprings"));
        data.add(vegetation());
        data.add(getAllElements("toplayermodification"));
    }

    NbtString randomPlant(String path) {
        return NbtString.of(PROVIDER.randomName(random, path));
    }

    NbtList getAllElements(String name) {
        if (!useVanillaFeatures) return new NbtList();
        List<String> lst = PROVIDER.registry.get(name).getAllElements(random);
        NbtList res = new NbtList();
        for (String s : lst) res.add(NbtString.of(s));
        return res;
    }

    void addRandomFeature(NbtList res, RandomisedFeature feature) {
        res.add(NbtString.of(feature.fullName()));
    }
    void addRandomFeature(String key, NbtList res, FeatureRegistrar registrar) {
        if (roll(key)) addRandomFeature(res, registrar);
    }
    void addRandomFeature(NbtList res, FeatureRegistrar registrar) {
        res.add(NbtString.of(registrar.op(this).fullName()));
    }

    public boolean roll(String key) {
        return PROVIDER.roll(random, key);
    }

    NbtList endIsland() {
        NbtList res = getAllElements("rawgeneration");
        addRandomFeature("end_island", res, RandomEndIsland::new);
        if (roll("shape")) addRandomFeature(res, new RandomShape(this, PROVIDER.randomName(random, "shape_types")));
        addRandomFeature("text", res, RandomText::new);
        return res;
    }

    NbtList lakes() {
        //addRandomFeature("lake", res, RandomLake::new);
        return getAllElements("lakes");
    }

    NbtList localModifications() {
        NbtList res = getAllElements("localmodifications");
        addRandomFeature("iceberg", res, RandomIceberg::new);
        addRandomFeature("geode", res, RandomGeode::new);
        addRandomFeature("rock", res, RandomRock::new);
        return res;
    }

    NbtList undergroundStructures() {
        NbtList res = getAllElements("undergroundstructures");
        addRandomFeature("dungeon", res, RandomDungeon::new);
        return res;
    }

    NbtList surfaceStructures() {
        NbtList res = getAllElements("surfacestructures");
        addRandomFeature("end_spikes", res, RandomEndSpikes::new);
        addRandomFeature("end_gateway", res, RandomEndGateway::new);
        addRandomFeature("delta", res, RandomDelta::new);
        addRandomFeature("columns", res, RandomColumns::new);
        addRandomFeature("crops", res, RandomCrop::new);
        return res;
    }

    NbtList undergroundOres() {
        NbtList res = getAllElements("undergroundores");
        int num_ores = random.nextInt( 4);
        int num_disks = Math.max(3, (int) Math.floor(random.nextExponential()));
        for (int i = 0; i < num_ores; i++) {
            addRandomFeature(res, new RandomOre(this));
        }
        for (int i = 0; i < num_disks; i++) {
            addRandomFeature(res, new RandomDisk(this));
        }
        return res;
    }

    NbtList undergroundDecoration() {
        NbtList res = getAllElements("undergrounddecoration");
        addRandomFeature("blobs", res, RandomBlobs::new);
        addRandomFeature("ceiling_blobs", res, RandomCeilingBlob::new);
        return res;
    }

    NbtList vegetation() {
        NbtList res = new NbtList();
        addRandomFeature("vegetation", res, RandomVegetation::new);
        res.addAll(getAllElements("vegetation_part1"));
        if (!PROVIDER.roll(random, "random_flowers")) res.add(randomPlant("flowers_legacy"));
        else addRandomFeature(res, RandomFlowerPatch::new);
        res.add(randomPlant("grass"));
        res.addAll(getAllElements("vegetation_part2"));
        addRandomFeature("surface_patch", res, RandomSurfacePatch::new);
        addRandomFeature("floating_patch", res, RandomFloatingPatch::new);
        res.add(randomPlant("seagrass"));
        res.addAll(getAllElements("vegetation_part3"));
        return res;
    }

    interface FeatureRegistrar {
        RandomisedFeature op(RandomFeaturesList parent);
    }
}
