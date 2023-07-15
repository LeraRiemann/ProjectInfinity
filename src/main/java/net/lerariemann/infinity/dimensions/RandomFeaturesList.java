package net.lerariemann.infinity.dimensions;

import net.lerariemann.infinity.dimensions.features.*;
import net.lerariemann.infinity.util.CommonIO;
import net.lerariemann.infinity.util.WeighedStructure;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import org.apache.logging.log4j.LogManager;

import java.util.*;

public class RandomFeaturesList {
    public NbtList data;
    public final RandomProvider PROVIDER;
    public String configPath;
    public String storagePath;

    public Random random;
    public NbtCompound surface_block;
    public WeighedStructure<String> trees;
    public RandomBiome parent;
    public List<String> blocks;
    public boolean useVanillaFeatures;

    RandomFeaturesList(RandomBiome biome) {
        parent = biome;
        random = biome.random;
        PROVIDER = biome.PROVIDER;
        surface_block = parent.parent.top_blocks.get(parent.fullname);
        configPath = PROVIDER.configPath + "features/";
        trees = CommonIO.weighedListReader(configPath + "vegetation/trees_checked.json");
        storagePath = biome.parent.storagePath;
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
        String plant = CommonIO.weighedListReader(configPath + "vegetation/" + path + ".json").getRandomElement(random);
        return NbtString.of(plant);
    }

    NbtList getAllElements(String name) {
        if (!useVanillaFeatures) return new NbtList();
        NbtList content = CommonIO.read(configPath + name + ".json").getList("elements", NbtElement.COMPOUND_TYPE);
        NbtList res = new NbtList();
        for (NbtElement nbtElement : content) {
            NbtCompound element = (NbtCompound) nbtElement;
            if (random.nextDouble() < element.getDouble("weight")) {
                NbtElement featuretoadd = element.get("key");
                res.add(featuretoadd);
            }
        }
        return res;
    }

    void addRandomFeature(NbtList res, RandomisedFeature feature) {
        res.add(NbtString.of(feature.fullName()));
    }
    void addRandomFeature(String key, NbtList res, FeatureRegistrar registrar) {
        if (roll(key)) res.add(NbtString.of(registrar.op(this).fullName()));
    }

    public boolean roll(String key) {
        return PROVIDER.roll(random, key);
    }

    NbtList endIsland() {
        NbtList res = getAllElements("rawgeneration");
        addRandomFeature("end_island", res, RandomEndIsland::new);
        if (roll("shape")) addRandomFeature(res, new RandomShape(this, PROVIDER.randomName(random, "shape_types")));
        return res;
    }

    NbtList lakes() {
        NbtList res = getAllElements("lakes");
        addRandomFeature("lake", res, RandomLake::new);
        return res;
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
        return res;
    }

    NbtList undergroundOres() {
        NbtList res = getAllElements("undergroundores");
        for (String s : parent.parent.underwater.keySet()) LogManager.getLogger().info(s);
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
        res.addAll(getAllElements("vegetation/part1"));
        res.add(randomPlant("flowers"));
        res.add(randomPlant("grass"));
        res.addAll(getAllElements("vegetation/part2"));
        addRandomFeature("surface_patch", res, RandomSurfacePatch::new);
        addRandomFeature("floating_patch", res, RandomFloatingPatch::new);
        res.add(randomPlant("seagrass"));
        res.addAll(getAllElements("vegetation/part3"));
        return res;
    }
}
interface FeatureRegistrar {
    RandomisedFeature op(RandomFeaturesList parent);
}
