package net.lerariemann.infinity.features;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.lerariemann.infinity.InfinityMod;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.SingleStateFeatureConfig;

import static net.lerariemann.infinity.InfinityMod.MOD_ID;

public class ModFeatures {
    public static RegistrySupplier<RandomEndIslandFeature> RANDOM_END_ISLAND;
    public static RegistrySupplier<RandomDungeonFeature> RANDOM_DUNGEON;
    public static RegistrySupplier<RandomColumnsFeature> RANDOM_COLUMNS;
    public static RegistrySupplier<RandomMushroomFeature> RANDOM_FLAT_MUSHROOM;
    public static RegistrySupplier<RandomRoundMushroomFeature> RANDOM_ROUND_MUSHROOM;
    public static RegistrySupplier<RandomCeilingBlobFeature> RANDOM_CEILING_BLOB;
    public static RegistrySupplier<RandomCubeFeature> RANDOM_CUBE;
    public static RegistrySupplier<RandomShapeFeature> RANDOM_STAR;
    public static RegistrySupplier<TextFeature> RANDOM_TEXT;

    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(MOD_ID, RegistryKeys.FEATURE);

    public static void registerFeatures() {
        InfinityMod.LOGGER.debug("Registering features for " + InfinityMod.MOD_ID);
        RANDOM_END_ISLAND = FEATURES.register("random_end_island", () -> new RandomEndIslandFeature(SingleStateFeatureConfig.CODEC));
        RANDOM_DUNGEON = FEATURES.register("random_dungeon",() -> new RandomDungeonFeature(RandomDungeonFeatureConfig.CODEC));
        RANDOM_COLUMNS = FEATURES.register("random_columns",() -> new RandomColumnsFeature(RandomColumnsFeatureConfig.CODEC));
        RANDOM_FLAT_MUSHROOM = FEATURES.register("random_flat_mushroom",() -> new RandomFlatMushroomFeature(RandomMushroomFeatureConfig.CODEC));
        RANDOM_ROUND_MUSHROOM = FEATURES.register("random_round_mushroom",() -> new RandomRoundMushroomFeature(RandomMushroomFeatureConfig.CODEC));
        RANDOM_CEILING_BLOB = FEATURES.register("random_ceiling_blob",() -> new RandomCeilingBlobFeature(RandomCeilingBlobFeatureConfig.CODEC));
        RANDOM_CUBE = FEATURES.register("random_cube",() -> new RandomCubeFeature(RandomCubeFeatureConfig.CODEC));
        RANDOM_STAR = FEATURES.register("random_shape",() -> new RandomShapeFeature(RandomShapeFeatureConfig.CODEC));
        RANDOM_TEXT = FEATURES.register("random_text",() -> new TextFeature(TextFeatureConfig.CODEC));
    }
}
