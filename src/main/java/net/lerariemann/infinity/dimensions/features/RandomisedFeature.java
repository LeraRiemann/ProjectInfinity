package net.lerariemann.infinity.dimensions.features;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.dimensions.CommonIO;
import net.lerariemann.infinity.dimensions.RandomProvider;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtString;

import java.io.IOException;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public abstract class RandomisedFeature {
    protected final RandomProvider PROVIDER;
    String type;
    String id;
    String name;
    Random random;
    boolean place;
    public Set<String> BLOCKS;

    public RandomisedFeature(int i, RandomProvider provider) {
        this(i, provider, true);
    }

    public RandomisedFeature(int i, RandomProvider provider, boolean placefeature) {
        random = new Random(i);
        PROVIDER = provider;
        BLOCKS = new HashSet<>();
        place = placefeature;
    }

    public String fullName() {
        return InfinityMod.MOD_ID + ":" + name;
    }

    void save(String path, int replacement) {
        NbtCompound data;
        try {
            CommonIO.write(feature(), path + "/worldgen/configured_feature", name + ".json");
            if (place) {
                data = CommonIO.readCarefully(PROVIDER.PATH + "features/placements/" + type + ".json", replacement);
                data.put("feature", NbtString.of(fullName()));
                CommonIO.write(data, path + "/worldgen/placed_feature", name + ".json");
            }
        } catch (IOException | CommandSyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    String genBlockOrFluid() {
        String block;
        if (random.nextBoolean()) {
            block = PROVIDER.randomName(random, PROVIDER.FULL_BLOCKS);
            BLOCKS.add(block);
        }
        else {
            block = PROVIDER.randomName(random, PROVIDER.FLUIDS);
        }
        return block;
    }

    void addBlockCarefully(NbtCompound config, String key, String block) {
        config.put(key, RandomProvider.Block(block));
    }

    void addBlock(NbtCompound config, String key, String block) {
        addBlockCarefully(config, key, block);
        BLOCKS.add(block);
    }

    void addBlockProviderCarefully(NbtCompound config, String key, String block) {
        config.put(key, RandomProvider.blockToProvider(RandomProvider.Block(block)));
    }

    void addRandomBlockProvider(NbtCompound config, String key) {
        String block = PROVIDER.randomName(random, PROVIDER.FULL_BLOCKS);
        addBlockProviderCarefully(config, key, block);
        BLOCKS.add(block);
    }

    void addRandomBlock(NbtCompound config, String key) {
        addBlock(config, key, PROVIDER.randomName(random, PROVIDER.FULL_BLOCKS));
    }

    abstract NbtCompound feature();

    NbtCompound feature(NbtCompound config) {
        NbtCompound res = new NbtCompound();
        res.putString("type", id);
        res.put("config", config);
        return res;
    }
}
