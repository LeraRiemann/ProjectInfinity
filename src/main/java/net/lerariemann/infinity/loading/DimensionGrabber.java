package net.lerariemann.infinity.loading;

import me.basiqueevangelist.dynreg.util.RegistryUtils;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.*;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.carver.ConfiguredCarver;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.PlacedFeature;
import org.apache.logging.log4j.LogManager;

import java.nio.file.Path;
import java.util.*;

public class DimensionGrabber {
    RegistryOps.RegistryInfoGetter registryInfoGetter;
    DynamicRegistryManager baseRegistryManager;

    public DimensionGrabber(DynamicRegistryManager brm) {
        baseRegistryManager = brm;
        List<MutableRegistry<?>> entries = new ArrayList<>();
        baseRegistryManager.streamAllRegistries().forEach((entry) -> {
            RegistryUtils.unfreeze(entry.value());
            entries.add((MutableRegistry<?>)entry.value());
        });
        registryInfoGetter = getGetter(entries);
    }

    public DimensionOptions grab_all(Path rootdir, int i) {
        (new JsonGrabber<>(registryInfoGetter, ConfiguredFeature.CODEC,
                (MutableRegistry<ConfiguredFeature<?, ?>>) (baseRegistryManager.get(RegistryKeys.CONFIGURED_FEATURE)))).grab_all(
                rootdir.resolve("worldgen/configured_feature"));
        (new JsonGrabber<>(registryInfoGetter, PlacedFeature.CODEC,
                (MutableRegistry<PlacedFeature>) (baseRegistryManager.get(RegistryKeys.PLACED_FEATURE)))).grab_all(rootdir.resolve("worldgen/placed_feature"));
        (new JsonGrabber<>(registryInfoGetter, ConfiguredCarver.CODEC,
                (MutableRegistry<ConfiguredCarver<?>>) (baseRegistryManager.get(RegistryKeys.CONFIGURED_CARVER)))).grab_all(rootdir.resolve("worldgen/configured_carver"));
        (new JsonGrabber<>(registryInfoGetter, Biome.CODEC,
                (MutableRegistry<Biome>) (baseRegistryManager.get(RegistryKeys.BIOME)))).grab_all(rootdir.resolve("worldgen/biome"));
        (new JsonGrabber<>(registryInfoGetter, ChunkGeneratorSettings.CODEC,
                (MutableRegistry<ChunkGeneratorSettings>) (baseRegistryManager.get(RegistryKeys.CHUNK_GENERATOR_SETTINGS)))).grab_all(
                rootdir.resolve("worldgen/noise_settings"));
        (new JsonGrabber<>(registryInfoGetter, DimensionType.CODEC,
                (MutableRegistry<DimensionType>) (baseRegistryManager.get(RegistryKeys.DIMENSION_TYPE)))).grab_all(rootdir.resolve("dimension_type"));
        return grab_dimension(rootdir, i);
    }

    public void grab_for_client(Identifier id, NbtCompound optiondata, List<Identifier> biomeids, List<NbtCompound> biomes) {
        if (!(baseRegistryManager.get(RegistryKeys.DIMENSION_TYPE).contains(RegistryKey.of(RegistryKeys.DIMENSION_TYPE, id)))) {
            (new JsonGrabber<>(registryInfoGetter, DimensionType.CODEC,
                    (MutableRegistry<DimensionType>) (baseRegistryManager.get(RegistryKeys.DIMENSION_TYPE)))).grab(id, optiondata);
            LogManager.getLogger().info("Dimension registered");
        }
        int i = biomes.size();
        for (int j = 0; j<i; j++) {
            if (!(baseRegistryManager.get(RegistryKeys.BIOME).contains(RegistryKey.of(RegistryKeys.BIOME, id)))) {
                (new JsonGrabber<>(registryInfoGetter, Biome.CODEC,
                        (MutableRegistry<Biome>) (baseRegistryManager.get(RegistryKeys.BIOME)))).grab(biomeids.get(j), biomes.get(j));
            }
        }
    }

    DimensionOptions grab_dimension(Path rootdir, int i) {
        DimensionOptions ret = (new JsonGrabber<>(registryInfoGetter, DimensionOptions.CODEC,
                (MutableRegistry<DimensionOptions>) (baseRegistryManager.get(RegistryKeys.DIMENSION)))).grab_with_return(rootdir.toString()+"/dimension", i, false);
        baseRegistryManager.streamAllRegistries().forEach((entry) -> entry.value().freeze());
        return ret;
    }

    public RegistryOps.RegistryInfoGetter getGetter(List<MutableRegistry<?>> additionalRegistries) {
        final Map<RegistryKey<? extends Registry<?>>, RegistryOps.RegistryInfo<?>> map = new HashMap<>();
        baseRegistryManager.streamAllRegistries().forEach((entry) -> map.put(entry.key(), createInfo((MutableRegistry<?>)(entry.value()))));
        additionalRegistries.forEach(first -> map.put(first.getKey(), createInfo(first)));
        return new RegistryOps.RegistryInfoGetter() {
            public <T> Optional<RegistryOps.RegistryInfo<T>> getRegistryInfo(RegistryKey<? extends Registry<? extends T>> registryRef) {
                return Optional.ofNullable((RegistryOps.RegistryInfo)map.get(registryRef));
            }
        };
    }

    public static <T> RegistryOps.RegistryInfo<T> createInfo(MutableRegistry<T> registry) {
        return new RegistryOps.RegistryInfo<>(registry.getReadOnlyWrapper(), registry.createMutableEntryLookup(), registry.getLifecycle());
    }
}
