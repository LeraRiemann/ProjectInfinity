package net.lerariemann.infinity.registry.core;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.features.*;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.FeatureConfig;
import net.minecraft.world.gen.feature.SingleStateFeatureConfig;

import static net.lerariemann.infinity.InfinityMod.MOD_ID;

public class ModFeatures {
    public static RegistrySupplier<RandomEndIslandFeature> RANDOM_END_ISLAND;
    public static RegistrySupplier<RandomDungeonFeature> RANDOM_DUNGEON;
    public static RegistrySupplier<RandomColumnsFeature> RANDOM_COLUMNS;
    public static RegistrySupplier<RandomFlatMushroomFeature> RANDOM_FLAT_MUSHROOM;
    public static RegistrySupplier<RandomRoundMushroomFeature> RANDOM_ROUND_MUSHROOM;
    public static RegistrySupplier<RandomCeilingBlobFeature> RANDOM_CEILING_BLOB;
    public static RegistrySupplier<RandomCubeFeature> RANDOM_CUBE;
    public static RegistrySupplier<RandomShapeFeature> RANDOM_STAR;
    public static RegistrySupplier<TextFeature> RANDOM_TEXT;
    public static RegistrySupplier<RandomPortalSetupper> PORTAL_SETUPPER;

    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(MOD_ID, RegistryKeys.FEATURE);

    public static <C extends FeatureConfig, F extends Feature<C>> RegistrySupplier<F> register(String name, F feature) {
        return FEATURES.register(name, () -> feature);
    }
    public static void registerFeatures() {
        InfinityMod.LOGGER.debug("Registering features for " + InfinityMod.MOD_ID);
        RANDOM_END_ISLAND = register("random_end_island", new RandomEndIslandFeature(SingleStateFeatureConfig.CODEC));
        RANDOM_DUNGEON = register("random_dungeon", new RandomDungeonFeature(RandomDungeonFeature.Config.CODEC));
        RANDOM_COLUMNS = register("random_columns", new RandomColumnsFeature(RandomColumnsFeature.Config.CODEC));
        RANDOM_FLAT_MUSHROOM = register("random_flat_mushroom", new RandomFlatMushroomFeature(RandomMushroomFeature.Config.CODEC));
        RANDOM_ROUND_MUSHROOM = register("random_round_mushroom", new RandomRoundMushroomFeature(RandomMushroomFeature.Config.CODEC));
        RANDOM_CEILING_BLOB = register("random_ceiling_blob", new RandomCeilingBlobFeature(RandomCeilingBlobFeature.Config.CODEC));
        RANDOM_CUBE = register("random_cube", new RandomCubeFeature(RandomCubeFeature.Config.CODEC));
        RANDOM_STAR = register("random_shape", new RandomShapeFeature(RandomShapeFeature.Config.CODEC));
        RANDOM_TEXT = register("random_text", new TextFeature(TextFeature.Config.CODEC));
        PORTAL_SETUPPER = register("portal_setupper", new RandomPortalSetupper((RandomPortalSetupper.Config.CODEC)));
        FEATURES.register();
    }
}