package net.lerariemann.infinity.dimensions;

import net.lerariemann.infinity.dimensions.features.*;
import net.lerariemann.infinity.dimensions.features.fluid_springs.RandomSpring;
import net.lerariemann.infinity.dimensions.features.lakes.RandomLake;
import net.lerariemann.infinity.dimensions.features.local_modifications.RandomGeode;
import net.lerariemann.infinity.dimensions.features.local_modifications.RandomIceberg;
import net.lerariemann.infinity.dimensions.features.local_modifications.RandomRock;
import net.lerariemann.infinity.dimensions.features.top_layer.RandomBonusChest;
import net.lerariemann.infinity.dimensions.features.raw_generation.RandomEndIsland;
import net.lerariemann.infinity.dimensions.features.raw_generation.RandomShape;
import net.lerariemann.infinity.dimensions.features.surface_structures.*;
import net.lerariemann.infinity.dimensions.features.underground_decoration.RandomBlobs;
import net.lerariemann.infinity.dimensions.features.underground_decoration.RandomCeilingBlob;
import net.lerariemann.infinity.dimensions.features.underground_ores.RandomDisk;
import net.lerariemann.infinity.dimensions.features.underground_ores.RandomOre;
import net.lerariemann.infinity.dimensions.features.underground_structures.RandomDungeon;
import net.lerariemann.infinity.dimensions.features.underground_structures.RandomFossil;
import net.lerariemann.infinity.dimensions.features.vegetation.*;
import net.lerariemann.infinity.util.core.ConfigType;
import net.lerariemann.infinity.util.core.NbtUtils;
import net.lerariemann.infinity.util.core.RandomProvider;
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
        data.add(rawGeneration());
        data.add(lakes());
        data.add(localModifications());
        data.add(undergroundStructures());
        data.add(surfaceStructures());
        data.add(undergroundOres());
        data.add(undergroundDecoration());
        data.add(fluidSprings());
        data.add(vegetation());
        data.add(topLayer());
    }

    NbtList getAllElements(ConfigType name) {
        if (!useVanillaFeatures) return new NbtList();
        List<String> lst = PROVIDER.registry.get(name).getAllNames(random::nextDouble);
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

    NbtList rawGeneration() {
        NbtList res = new NbtList();
        addRandomFeature("end_island", res, RandomEndIsland::new);
        if (roll("shape")) addRandomFeature(res, new RandomShape(this, PROVIDER.randomName(random, ConfigType.SHAPE_TYPES)));
        return res;
    }

    NbtList lakes() {
        NbtList res = new NbtList();
        addRandomFeature("lake", res, RandomLake::new);
        return res;
    }

    NbtList localModifications() {
        NbtList res = getAllElements(ConfigType.LOCAL_MOD);
        addRandomFeature("iceberg", res, RandomIceberg::new);
        addRandomFeature("geode", res, RandomGeode::new);
        addRandomFeature("rock", res, RandomRock::new);
        return res;
    }

    NbtList undergroundStructures() {
        NbtList res = new NbtList();
        addRandomFeature("dungeon", res, RandomDungeon::new);
        addRandomFeature("fossil", res, RandomFossil::new);
        return res;
    }

    NbtList surfaceStructures() {
        NbtList res = new NbtList();
        addRandomFeature("end_spikes", res, RandomEndSpikes::new);
        addRandomFeature("end_gateway", res, RandomEndGateway::new);
        addRandomFeature("delta", res, RandomDelta::new);
        addRandomFeature("columns", res, RandomColumns::new);
        addRandomFeature("well", res, RandomWell::new);
        return res;
    }

    NbtList undergroundOres() {
        NbtList res = getAllElements(ConfigType.UNDERGROUND_ORES);
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
        NbtList res = getAllElements(ConfigType.UNDERGROUND_DEC);
        addRandomFeature("blobs", res, RandomBlobs::new);
        addRandomFeature("ceiling_blobs", res, RandomCeilingBlob::new);
        return res;
    }

    NbtList fluidSprings() {
        NbtList res = new NbtList();
        addRandomFeature("springs", res, RandomSpring::new);
        return res;
    }

    NbtList vegetation() {
        NbtList res = new NbtList();
        addRandomFeature("vegetation", res, RandomVegetation::new);
        res.addAll(getAllElements(ConfigType.VEG1));
        addRandomFeature(res, RandomFlowerPatch::new);
        res.add(NbtString.of(PROVIDER.randomName(random, ConfigType.GRASS)));
        res.addAll(getAllElements(ConfigType.VEG2));
        addRandomFeature("surface_patch", res, RandomSurfacePatch::new);
        addRandomFeature("floating_patch", res, RandomFloatingPatch::new);
        if (NbtUtils.getString(parent.parent.default_fluid, "Name").contains("water")) {
            addRandomFeature("water_plants", res, RandomSeagrass::new);
            addRandomFeature("water_plants", res, RandomPickle::new);
            addRandomFeature("water_plants", res, RandomKelp::new);
        }
        return res;
    }

    NbtList topLayer() {
        NbtList res = getAllElements(ConfigType.TOP_LAYER);
        addRandomFeature("bonus_chest", res, RandomBonusChest::new);
        return res;
    }

    interface FeatureRegistrar {
        RandomisedFeature op(RandomFeaturesList parent);
    }
}
