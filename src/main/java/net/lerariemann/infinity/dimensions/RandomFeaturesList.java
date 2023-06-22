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
    public String surface_block;
    public WeighedStructure<String> trees;
    public RandomBiome parent;
    public List<String> blocks;

    RandomFeaturesList(RandomBiome biome) {
        parent = biome;
        random = biome.random;
        PROVIDER = biome.PROVIDER;
        surface_block = parent.parent.top_blocks.get(parent.fullname).getString("Name");
        configPath = PROVIDER.configPath + "features/";
        trees = CommonIO.commonListReader(configPath + "vegetation/trees_checked.json");
        storagePath = biome.parent.storagePath;
        blocks = new ArrayList<>();
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
        String plant = CommonIO.commonListReader(configPath + "vegetation/" + path + ".json").getRandomElement(random);
        return NbtString.of(plant);
    }

    NbtList getAllElements(String name) {
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

    public boolean roll(String key) {
        return PROVIDER.roll(random, key);
    }

    void addRandomFeature(NbtList res, RandomisedFeature feature, String key) {
        if (roll(key)) addRandomFeature(res, feature);
    }

    NbtList endIsland() {
        NbtList res = getAllElements("rawgeneration");
        addRandomFeature(res, new RandomEndIsland(this), "end_island");
        return res;
    }

    NbtList lakes() {
        NbtList res = getAllElements("lakes");
        addRandomFeature(res, new RandomLake(this), "lake");
        return res;
    }

    NbtList localModifications() {
        NbtList res = getAllElements("localmodifications");
        addRandomFeature(res, new RandomIceberg(this), "iceberg");
        addRandomFeature(res, new RandomGeode(this), "geode");
        addRandomFeature(res, new RandomRock(this), "rock");
        return res;
    }

    NbtList undergroundStructures() {
        NbtList res = getAllElements("undergroundstructures");
        addRandomFeature(res, new RandomDungeon(this), "dungeon");
        return res;
    }

    NbtList surfaceStructures() {
        NbtList res = getAllElements("surfacestructures");
        addRandomFeature(res, new RandomEndSpikes(this), "end_spikes");
        addRandomFeature(res, new RandomEndGateway(this), "end_gateway");
        addRandomFeature(res, new RandomDelta(this), "delta");
        addRandomFeature(res, new RandomColumns(this), "columns");
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
        addRandomFeature(res, new RandomBlobs(this), "blobs");
        addRandomFeature(res, new RandomCeilingBlob(this), "ceiling_blobs");
        return res;
    }

    NbtList vegetation() {
        boolean useTree = PROVIDER.roll(random, "use_one_tree");
        NbtList res = new NbtList();
        if (!useTree) addRandomFeature(res, new RandomVegetation(this), "vegetation");
        res.addAll(getAllElements("vegetation/part1"));
        if (useTree && roll("vegetation")) res.add(randomTree());
        res.add(randomPlant("flowers"));
        res.add(randomPlant("grass"));
        res.addAll(getAllElements("vegetation/part2"));
        addRandomFeature(res, new RandomSurfacePatch(this), "surface_patch");
        addRandomFeature(res, new RandomFloatingPatch(this), "floating_patch");
        res.add(randomPlant("seagrass"));
        res.addAll(getAllElements("vegetation/part3"));
        return res;
    }

    NbtString randomTree() {
        if (Objects.equals(surface_block, "minecraft:grass_block") && roll("use_vanilla_trees")) {
            return randomPlant("trees");
        }
        else switch (PROVIDER.floralDistribution.getRandomElement(random)) {
            case "fungi" -> {
                RandomFungus fungus = new RandomFungus(this);
                return NbtString.of(fungus.fullName());
            }
            case "trees" -> {
                RandomTree tree = new RandomTree(this, true);
                return NbtString.of(tree.fullName());
            }
            case "mushrooms" -> {
                RandomMushroom mushroom = new RandomMushroom(this, true);
                return NbtString.of(mushroom.fullName());
            }
        }
        return NbtString.of("minecraft:oak");
    }
}
