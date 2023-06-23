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

    RandomFeaturesList(RandomBiome biome) {
        parent = biome;
        random = biome.random;
        PROVIDER = biome.PROVIDER;
        surface_block = parent.parent.top_blocks.get(parent.fullname);
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

    NbtList endIsland() {
        NbtList res = getAllElements("rawgeneration");
        if (roll("end_island")) addRandomFeature(res, new RandomEndIsland(this));
        if (roll("shape")) addRandomFeature(res, new RandomShape(this, PROVIDER.randomName(random, "shape_types")));
        return res;
    }

    NbtList lakes() {
        NbtList res = getAllElements("lakes");
        if (roll("lake")) addRandomFeature(res, new RandomLake(this));
        return res;
    }

    NbtList localModifications() {
        NbtList res = getAllElements("localmodifications");
        if (roll("iceberg")) addRandomFeature(res, new RandomIceberg(this));
        if (roll("geode")) addRandomFeature(res, new RandomGeode(this));
        if (roll("rock")) addRandomFeature(res, new RandomRock(this));
        return res;
    }

    NbtList undergroundStructures() {
        NbtList res = getAllElements("undergroundstructures");
        if (roll("dungeon")) addRandomFeature(res, new RandomDungeon(this));
        return res;
    }

    NbtList surfaceStructures() {
        NbtList res = getAllElements("surfacestructures");
        if (roll("end_spikes")) addRandomFeature(res, new RandomEndSpikes(this));
        if (roll("end_gateway")) addRandomFeature(res, new RandomEndGateway(this));
        if (roll("delta")) addRandomFeature(res, new RandomDelta(this));
        if (roll("columns")) addRandomFeature(res, new RandomColumns(this));
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
        if (roll("blobs"))  addRandomFeature(res, new RandomBlobs(this));
        if (roll("ceiling_blobs"))  addRandomFeature(res, new RandomCeilingBlob(this));
        return res;
    }

    NbtList vegetation() {
        NbtList res = new NbtList();
        if (roll("vegetation")) addRandomFeature(res, new RandomVegetation(this));
        res.addAll(getAllElements("vegetation/part1"));
        res.add(randomPlant("flowers"));
        res.add(randomPlant("grass"));
        res.addAll(getAllElements("vegetation/part2"));
        if (roll("surface_patch")) addRandomFeature(res, new RandomSurfacePatch(this));
        if (roll("floating_patch")) addRandomFeature(res, new RandomFloatingPatch(this));
        res.add(randomPlant("seagrass"));
        res.addAll(getAllElements("vegetation/part3"));
        return res;
    }
}
