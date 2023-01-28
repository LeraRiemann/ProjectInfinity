package net.lerariemann.infinity.dimensions;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

import java.io.IOException;

public class RandomProvider {
    public WeighedStructure<String> FULL_BLOCKS;
    public WeighedStructure<String> ALL_BLOCKS;
    public WeighedStructure<String> BIOMES;
    public WeighedStructure<String> NOISE_PRESETS;
    public WeighedStructure<String> INFINIBURN;

    public RandomProvider(String path) throws IOException, CommandSyntaxException {
        ALL_BLOCKS = CommonIO.commonListReader(path + "allblocks.json");
        FULL_BLOCKS = CommonIO.commonListReader(path + "fullblocks.json");
        BIOMES = CommonIO.commonListReader(path + "biomes.json");
        NOISE_PRESETS = CommonIO.commonListReader(path + "noise_presets.json");
        INFINIBURN = CommonIO.commonListReader(path + "infiniburn.json");
    }
}
