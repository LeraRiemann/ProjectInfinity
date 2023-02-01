package net.lerariemann.infinity.dimensions;

import net.lerariemann.infinity.dimensions.features.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;

import java.util.*;

public class RandomFeaturesList {
    public NbtList data;
    public final RandomProvider PROVIDER;
    public String configPath;
    public String storagePath;

    public Random random;
    public List<String> blocks;
    public String surface_block;
    public WeighedStructure <String> trees;
    public RandomBiome parent;

    RandomFeaturesList(RandomBiome biome) {
        parent = biome;
        random = biome.random;
        PROVIDER = biome.PROVIDER;
        blocks = new ArrayList<>();
        surface_block = parent.parent.top_blocks.get(parent.fullname);
        configPath = PROVIDER.configPath + "features/";
        trees = CommonIO.commonListReader(configPath + "vegetation/trees_checked.json");
        storagePath = biome.parent.storagePath;
        data = new NbtList();
        data.add(getAllElements("rawgeneration"));
        data.add(lakes());
        data.add(localModifications());
        data.add(getAllElements("undergroundstructures"));
        data.add(surfaceStructures());
        data.add(getAllElements("strongholds"));
        data.add(getAllElements("undergroundores"));
        data.add(getAllElements("undergrounddecoration"));
        data.add(getAllElements("fluidsprings"));
        data.add(vegetation());
        data.add(getAllElements("toplayermodification"));
    }

    NbtList vegetation() {
        boolean useTree = random.nextBoolean();
        NbtList res = new NbtList();
        if (!useTree) res.add(NbtString.of((new RandomVegetation(this)).fullName()));
        res.addAll(getAllElements("vegetation/part1"));
        if (useTree) res.add(randomTree());
        res.add(randomPlant("flowers"));
        res.add(randomPlant("grass"));
        res.addAll(getAllElements("vegetation/part2"));
        res.add(randomPlant("seagrass"));
        res.addAll(getAllElements("vegetation/part3"));
        return res;
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
                checkForBlocks(featuretoadd.toString());
            }
        }
        return res;
    }

    void checkForBlocks(String feature) {
        switch (feature) {
            case "minecraft:end_island_decorated" -> {
                blocks.add("minecraft:endstone");
                return;
            }
            case "minecraft:small_basalt_columns", "minecraft:large_basalt_columns" -> {
                blocks.add("minecraft:basalt");
                return;
            }
        }
    }

    void addRandomFeature(NbtList res, RandomisedFeature feature) {
        res.add(NbtString.of(feature.fullName()));
        for (String i : feature.BLOCKS) if (i != null) {
            blocks.add(i);
        }
    }

    NbtList lakes() {
        NbtList res = getAllElements("lakes");
        if (random.nextBoolean()) res.add(NbtString.of((new RandomLake(random.nextInt(), PROVIDER, storagePath)).fullName()));
        return res;
    }

    NbtList localModifications() {
        NbtList res = getAllElements("localmodifications");
        if (random.nextBoolean()) addRandomFeature(res, new RandomIceberg(random.nextInt(), PROVIDER, storagePath));
        if (random.nextBoolean()) addRandomFeature(res, new RandomGeode(random.nextInt(), PROVIDER, storagePath));
        if (random.nextBoolean()) addRandomFeature(res, new RandomRock(random.nextInt(), PROVIDER, storagePath));
        return res;
    }

    NbtList surfaceStructures() {
        NbtList res = getAllElements("surfacestructures");
        if (random.nextBoolean()) addRandomFeature(res, new RandomDelta(random.nextInt(), PROVIDER, storagePath));
        return res;
    }

    NbtString randomTree() {
        if (random.nextBoolean()) return randomPlant("trees");
        else {
            RandomFungus fungus = new RandomFungus(random.nextInt(), PROVIDER, storagePath, blocks, surface_block);
            blocks.addAll(fungus.BLOCKS);
            return NbtString.of((fungus.fullName()));
        }
    }
}
