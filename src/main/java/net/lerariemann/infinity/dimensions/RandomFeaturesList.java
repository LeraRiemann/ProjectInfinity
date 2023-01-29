package net.lerariemann.infinity.dimensions;

import net.lerariemann.infinity.dimensions.features.RandomLake;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;

import java.io.IOException;
import java.util.Random;

public class RandomFeaturesList {
    public NbtList data;
    private final RandomProvider PROVIDER;
    String configsPath;
    String PATH;
    Random random;

    RandomFeaturesList(int i, RandomProvider provider, String path) {
        random = new Random(i);
        PROVIDER = provider;
        configsPath = PROVIDER.PATH + "features/";
        PATH = path;
        data = new NbtList();
        data.add(getAllElements("rawgeneration"));
        data.add(lakes());
        data.add(getAllElements("localmodifications"));
        data.add(getAllElements("undergroundstructures"));
        data.add(getAllElements("surfacestructures"));
        data.add(getAllElements("strongholds"));
        data.add(getAllElements("undergroundores"));
        data.add(getAllElements("undergrounddecoration"));
        data.add(getAllElements("fluidsprings"));
        data.add(vegetation());
        data.add(getAllElements("toplayermodification"));
    }

    NbtList vegetation() {
        NbtList res = new NbtList();
        res.addAll(getAllElements("vegetation/part1"));
        res.add(randomPlant("trees"));
        res.add(randomPlant("flowers"));
        res.add(randomPlant("grass"));
        res.addAll(getAllElements("vegetation/part2"));
        res.add(randomPlant("seagrass"));
        res.addAll(getAllElements("vegetation/part3"));
        return res;
    }

    NbtString randomPlant(String path) {
        String plant = null;
        try {
            plant = CommonIO.commonListReader(configsPath + "vegetation/" + path + ".json").getRandomElement(random);
        } catch (IOException | CommandSyntaxException e) {
            throw new RuntimeException(e);
        }
        return NbtString.of(plant);
    }

    NbtList getAllElements(String name) {
        NbtList content = null;
        try {
            content = CommonIO.read(configsPath + name + ".json").getList("elements", NbtElement.COMPOUND_TYPE);
        } catch (IOException | CommandSyntaxException e) {
            throw new RuntimeException(e);
        }
        NbtList res = new NbtList();
        for (int i = 0; i < content.size(); i++) {
            NbtCompound element = (NbtCompound)content.get(i);
            if (random.nextDouble() < element.getDouble("weight")) res.add(element.get("key"));
        }
        return res;
    }

    NbtList lakes() {
        NbtList res = getAllElements("lakes");
        if (random.nextBoolean()) {
            String lake = (new RandomLake(random.nextInt(), PROVIDER, PATH)).fullname;
            res.add(NbtString.of(lake));
        }
        return res;
    }
}
