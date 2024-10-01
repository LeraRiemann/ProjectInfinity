package net.lerariemann.infinity.features;

import net.lerariemann.infinity.InfinityMod;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.FeatureConfig;
import net.minecraft.world.gen.feature.SingleStateFeatureConfig;

public class ModFeatures {
    public static Feature<SingleStateFeatureConfig> RANDOM_END_ISLAND;
    public static Feature<RandomDungeonFeatureConfig> RANDOM_DUNGEON;
    public static Feature<RandomColumnsFeatureConfig> RANDOM_COLUMNS;
    public static Feature<RandomMushroomFeatureConfig> RANDOM_FLAT_MUSHROOM;
    public static Feature<RandomMushroomFeatureConfig> RANDOM_ROUND_MUSHROOM;
    public static Feature<RandomCeilingBlobFeatureConfig> RANDOM_CEILING_BLOB;
    public static Feature<RandomCubeFeatureConfig> RANDOM_CUBE;
    public static Feature<RandomShapeFeatureConfig> RANDOM_STAR;
    public static Feature<TextFeatureConfig> RANDOM_TEXT;

    public static <C extends FeatureConfig, F extends Feature<C>> F register(String name, F feature) {
        return Registry.register(Registries.FEATURE, name, feature);
    }

    public static void registerFeatures() {
        InfinityMod.LOGGER.debug("Registering features for " + InfinityMod.MOD_ID);
        RANDOM_END_ISLAND = register("random_end_island", new RandomEndIslandFeature(SingleStateFeatureConfig.CODEC));
        RANDOM_DUNGEON = register("random_dungeon", new RandomDungeonFeature(RandomDungeonFeatureConfig.CODEC));
        RANDOM_COLUMNS = register("random_columns", new RandomColumnsFeature(RandomColumnsFeatureConfig.CODEC));
        RANDOM_FLAT_MUSHROOM = register("random_flat_mushroom", new RandomFlatMushroomFeature(RandomMushroomFeatureConfig.CODEC));
        RANDOM_ROUND_MUSHROOM = register("random_round_mushroom", new RandomRoundMushroomFeature(RandomMushroomFeatureConfig.CODEC));
        RANDOM_CEILING_BLOB = register("random_ceiling_blob", new RandomCeilingBlobFeature(RandomCeilingBlobFeatureConfig.CODEC));
        RANDOM_CUBE = register("random_cube", new RandomCubeFeature(RandomCubeFeatureConfig.CODEC));
        RANDOM_STAR = register("random_shape", new RandomShapeFeature(RandomShapeFeatureConfig.CODEC));
        RANDOM_TEXT = register("random_text", new TextFeature(TextFeatureConfig.CODEC));
    }
}
