package net.lerariemann.infinity.loading;

import com.mojang.serialization.Codec;
import net.lerariemann.infinity.util.PlatformMethods;
import net.lerariemann.infinity.dimensions.RandomDimension;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.*;
import net.minecraft.structure.StructureSet;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.carver.ConfiguredCarver;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.PlacedFeature;
import net.minecraft.world.gen.structure.Structure;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class DimensionGrabber {
    RegistryOps.RegistryInfoGetter registryInfoGetter;
    DynamicRegistryManager baseRegistryManager;
    Map<RegistryKey<? extends Registry<?>>, MutableRegistry<?>> mutableRegistries;

    public DimensionGrabber(DynamicRegistryManager brm) {
        baseRegistryManager = brm;
        mutableRegistries = new HashMap<>();
        baseRegistryManager.streamAllRegistries().forEach((entry) -> {
            Registry<?> reg = entry.value();
            PlatformMethods.unfreeze(reg);
            mutableRegistries.put(entry.key(), (MutableRegistry<?>)reg);
        });
        registryInfoGetter = getGetter();
    }

    public DimensionOptions grab_all(RandomDimension d) {
        Path rootdir = Paths.get(d.getStoragePath());
        buildGrabber(ConfiguredFeature.CODEC, RegistryKeys.CONFIGURED_FEATURE).grab_all(rootdir.resolve("worldgen/configured_feature"));
        buildGrabber(PlacedFeature.CODEC, RegistryKeys.PLACED_FEATURE).grab_all(rootdir.resolve("worldgen/placed_feature"), true);
        buildGrabber(ConfiguredCarver.CODEC, RegistryKeys.CONFIGURED_CARVER).grab_all(rootdir.resolve("worldgen/configured_carver"));
        buildGrabber(Biome.CODEC, RegistryKeys.BIOME).grab_all(rootdir.resolve("worldgen/biome"));
        buildGrabber(Structure.STRUCTURE_CODEC, RegistryKeys.STRUCTURE).grab_all(rootdir.resolve("worldgen/structure"));
        buildGrabber(StructureSet.CODEC, RegistryKeys.STRUCTURE_SET).grab_all(rootdir.resolve("worldgen/structure_set"));
        buildGrabber(ChunkGeneratorSettings.CODEC, RegistryKeys.CHUNK_GENERATOR_SETTINGS).grab_all(rootdir.resolve("worldgen/noise_settings"));
        buildGrabber(DimensionType.CODEC, RegistryKeys.DIMENSION_TYPE).grab_all(rootdir.resolve("dimension_type"));
        DimensionOptions ret = buildGrabber(DimensionOptions.CODEC, RegistryKeys.DIMENSION)
                .grab_with_return(rootdir + "/dimension", d.getName(), false);
        close();
        return ret;
    }

    <T> JsonGrabber<T> buildGrabber(Codec<T> codec, RegistryKey<Registry<T>> key) {
        return (new JsonGrabber<>(registryInfoGetter, codec, (MutableRegistry<T>)(mutableRegistries.get(key))));
    }

    <T> void grab_one_for_client(Codec<T> codec, RegistryKey<Registry<T>> key, Identifier id, NbtCompound optiondata) {
        if (!(baseRegistryManager.get(key).contains(RegistryKey.of(key, id)))) buildGrabber(codec, key).grab(id, optiondata, false);
    }

    public void grab_for_client(Identifier id, NbtCompound optiondata, List<Identifier> biomeids, List<NbtCompound> biomes) {
        if (!optiondata.isEmpty()) grab_one_for_client(DimensionType.CODEC, RegistryKeys.DIMENSION_TYPE, id, optiondata);
        int i = biomes.size();
        for (int j = 0; j<i; j++) grab_one_for_client(Biome.CODEC, RegistryKeys.BIOME, biomeids.get(j), biomes.get(j));
        close();
    }

    void close() {
        mutableRegistries.values().forEach(Registry::freeze);
    }

    public RegistryOps.RegistryInfoGetter getGetter() {
        //baseRegistryManager.streamAllRegistries().forEach((entry) -> map.put(entry.key(), createInfo((MutableRegistry<?>)(entry.value()))));
        return new RegistryOps.RegistryInfoGetter() {
            public <T> Optional<RegistryOps.RegistryInfo<T>> getRegistryInfo(RegistryKey<? extends Registry<? extends T>> registryRef) {
                return Optional.ofNullable((RegistryOps.RegistryInfo<T>)createInfo(mutableRegistries.get(registryRef)));
            }
        };
    }

    public static <T> RegistryOps.RegistryInfo<T> createInfo(MutableRegistry<T> registry) {
        return new RegistryOps.RegistryInfo<>(registry.getReadOnlyWrapper(), registry.createMutableEntryLookup(), registry.getLifecycle());
    }
}
