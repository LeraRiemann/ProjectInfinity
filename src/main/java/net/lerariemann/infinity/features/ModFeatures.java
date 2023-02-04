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

    public static <C extends FeatureConfig, F extends Feature<C>> F register(String name, F feature) {
        return Registry.register(Registries.FEATURE, name, feature);
    }

    public static void registerFeatures() {
        InfinityMod.LOGGER.debug("Registering features for " + InfinityMod.MOD_ID);
        RANDOM_END_ISLAND = register("random_end_island", new RandomEndIslandFeature(SingleStateFeatureConfig.CODEC));
        RANDOM_DUNGEON = register("random_dungeon", new RandomDungeonFeature(RandomDungeonFeatureConfig.CODEC));
    }
}
