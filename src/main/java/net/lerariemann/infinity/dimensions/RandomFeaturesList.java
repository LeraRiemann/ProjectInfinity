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
        data.add(endIsland());
        data.add(lakes());
        data.add(localModifications());
        data.add(undergroundStructures());
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
        if (random.nextBoolean()) res.add(NbtString.of(new RandomSurfacePatch(this).fullName()));
        if (random.nextBoolean()) res.add(NbtString.of(new RandomFloatingPatch(this).fullName()));
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
            case "minecraft:end_island_decorated" -> blocks.add("minecraft:endstone");
            case "minecraft:small_basalt_columns", "minecraft:large_basalt_columns" -> blocks.add("minecraft:basalt");
        }
    }

    void addRandomFeature(NbtList res, RandomisedFeature feature) {
        res.add(NbtString.of(feature.fullName()));
        for (String i : feature.BLOCKS) if (i != null) blocks.add(i);
    }

    NbtList endIsland() {
        NbtList res = getAllElements("rawgeneration");
        if (random.nextBoolean()) {
            res.add(NbtString.of((new RandomEndIsland(this)).fullName()));
            blocks.add("minecraft:endstone");
        }
        return res;
    }

    NbtList lakes() {
        NbtList res = getAllElements("lakes");
        if (random.nextBoolean()) res.add(NbtString.of((new RandomLake(this)).fullName()));
        return res;
    }

    NbtList localModifications() {
        NbtList res = getAllElements("localmodifications");
        if (random.nextBoolean()) addRandomFeature(res, new RandomIceberg(this));
        if (random.nextBoolean()) addRandomFeature(res, new RandomGeode(this));
        if (random.nextBoolean()) addRandomFeature(res, new RandomRock(this));
        return res;
    }

    NbtList undergroundStructures() {
        NbtList res = getAllElements("undergroundstructures");
        addRandomFeature(res, new RandomDungeon(this));
        return res;
    }

    NbtList surfaceStructures() {
        NbtList res = getAllElements("surfacestructures");
        if (random.nextBoolean()) addRandomFeature(res, new RandomDelta(this));
        return res;
    }

    NbtString randomTree() {
        int a = (Objects.equals(surface_block, "minecraft:grass_block")) ? 4 : 3;
        switch (random.nextInt(a)) {
            case 3 -> {
                return randomPlant("trees");
            }
            case 2 -> {
                RandomFungus fungus = new RandomFungus(this);
                return NbtString.of(fungus.fullName());
            }
            case 1 -> {
                RandomTree tree = new RandomTree(this, true);
                return NbtString.of(tree.fullName());
            }
            case 0 -> {
                RandomMushroom mushroom = new RandomMushroom(this, true);
                return NbtString.of(mushroom.fullName());
            }
        }
        return NbtString.of("minecraft:oak");
    }
}
