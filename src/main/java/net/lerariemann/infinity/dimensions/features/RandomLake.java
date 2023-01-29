package net.lerariemann.infinity.dimensions.features;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.dimensions.CommonIO;
import net.lerariemann.infinity.dimensions.RandomProvider;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;

import java.io.IOException;
import java.util.Random;

public class RandomLake {
    NbtCompound data;
    private final RandomProvider PROVIDER;
    String type;
    String name;
    public String fullname;    Random random;

    public RandomLake(int i, RandomProvider provider, String path) {
        random = new Random(i);
        PROVIDER = provider;
        name = "lake_" + i;
        fullname = InfinityMod.MOD_ID + ":" + name;
        boolean surface = random.nextBoolean();
        type = (surface ? "lake_surface" : "lake_underground");
        try {
            data = CommonIO.readCarefully(provider.PATH + "features/placements/" + type + ".json", 1 + random.nextInt(surface ? 50 : 9));
        } catch (IOException | CommandSyntaxException e) {
            throw new RuntimeException(e);
        }
        data.put("feature", NbtString.of(fullname));
        CommonIO.write(feature(), path + "/datapacks/" + InfinityMod.MOD_ID + "/data/" + InfinityMod.MOD_ID + "/worldgen/configured_feature", name + ".json");
        CommonIO.write(data, path + "/datapacks/" + InfinityMod.MOD_ID + "/data/" + InfinityMod.MOD_ID + "/worldgen/placed_feature", name + ".json");
    }

    NbtCompound feature() {
        NbtCompound res = new NbtCompound();
        res.putString("type", "lake");
        NbtCompound config = new NbtCompound();
        config.put("fluid", PROVIDER.randomBlockProvider(random, PROVIDER.FLUIDS));
        config.put("barrier", PROVIDER.randomBlockProvider(random, PROVIDER.FULL_BLOCKS));
        res.put("config", config);
        return res;
    }
}
