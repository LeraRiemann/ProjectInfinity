package net.lerariemann.infinity.dimensions;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

import java.io.IOException;
import java.nio.file.Path;

public class RandomProvider {
    public WeighedStructure<String> FULL_BLOCKS;
    public WeighedStructure<String> ALL_BLOCKS;
    public WeighedStructure<String> BIOMES;
    public WeighedStructure<String> NOISE_PRESETS;

    public RandomProvider(String path) throws IOException, CommandSyntaxException {
        ALL_BLOCKS = CommonIO.commonListReader(path + "/blocks/allblocks.json");
        FULL_BLOCKS = CommonIO.commonListReader(path + "/blocks/fullblocks.json");
        BIOMES = CommonIO.commonListReader(path + "/biomes/biomes.json");
        NOISE_PRESETS = CommonIO.commonListReader(path + "/noise_presets/noise_presets.json");
    }
}
