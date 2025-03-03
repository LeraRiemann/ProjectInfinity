package net.lerariemann.infinity.util.loading;

import com.mojang.serialization.Codec;
import net.lerariemann.infinity.access.RegistryAccess;
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
    Set<MutableRegistry<?>> mutableRegistries = new HashSet<>();
    static Set<RegistryKey<? extends Registry<?>>> unfrozenKeys = Set.of(RegistryKeys.CONFIGURED_FEATURE,
            RegistryKeys.PLACED_FEATURE,
            RegistryKeys.CONFIGURED_CARVER,
            RegistryKeys.BIOME,
            RegistryKeys.STRUCTURE,
            RegistryKeys.STRUCTURE_SET,
            RegistryKeys.CHUNK_GENERATOR_SETTINGS,
            RegistryKeys.DIMENSION_TYPE,
            RegistryKeys.DIMENSION);

    public DimensionGrabber(DynamicRegistryManager brm) {
        baseRegistryManager = brm;
        baseRegistryManager.streamAllRegistries().forEach((entry) -> {
            if (unfrozenKeys.contains(entry.key())) {
                ((RegistryAccess)entry.value()).infinity$unfreeze();
                mutableRegistries.add((MutableRegistry<?>)entry.value());
            }
        });
        registryInfoGetter = getGetter();
    }

    public DimensionOptions grabAllRelatedData(RandomDimension d) {
        Path rootdir = Paths.get(d.getStoragePath());
        buildGrabber(ConfiguredFeature.CODEC, RegistryKeys.CONFIGURED_FEATURE).grabAll(rootdir.resolve("worldgen/configured_feature"));
        buildGrabber(PlacedFeature.CODEC, RegistryKeys.PLACED_FEATURE).grabAll(rootdir.resolve("worldgen/placed_feature"), true);
        buildGrabber(ConfiguredCarver.CODEC, RegistryKeys.CONFIGURED_CARVER).grabAll(rootdir.resolve("worldgen/configured_carver"));
        buildGrabber(Biome.CODEC, RegistryKeys.BIOME).grabAll(rootdir.resolve("worldgen/biome"));
        buildGrabber(Structure.STRUCTURE_CODEC, RegistryKeys.STRUCTURE).grabAll(rootdir.resolve("worldgen/structure"));
        buildGrabber(StructureSet.CODEC, RegistryKeys.STRUCTURE_SET).grabAll(rootdir.resolve("worldgen/structure_set"));
        buildGrabber(ChunkGeneratorSettings.CODEC, RegistryKeys.CHUNK_GENERATOR_SETTINGS).grabAll(rootdir.resolve("worldgen/noise_settings"));
        buildGrabber(DimensionType.CODEC, RegistryKeys.DIMENSION_TYPE).grabAll(rootdir.resolve("dimension_type"));
        return grabDimension(rootdir, d.getName());
    }

    public <T> JsonGrabber<T> buildGrabber(Codec<T> codec, RegistryKey<Registry<T>> key) {
        return (new JsonGrabber<>(registryInfoGetter, codec, (MutableRegistry<T>) (baseRegistryManager.get(key))));
    }

    <T> void grabObjectForClient(Codec<T> codec, RegistryKey<Registry<T>> key, Identifier id, NbtCompound optiondata) {
        if (!(baseRegistryManager.get(key).contains(RegistryKey.of(key, id)))) buildGrabber(codec, key).grab(id, optiondata, false);
    }

    public void grabDimensionForClient(Identifier id, NbtCompound dimdata) {
        if (!dimdata.isEmpty()) grabObjectForClient(DimensionType.CODEC, RegistryKeys.DIMENSION_TYPE, id, dimdata);
    }

    public void grabBiomeForClient(Identifier id, NbtCompound biomedata) {
        grabObjectForClient(Biome.CODEC, RegistryKeys.BIOME, id, biomedata);
    }

    DimensionOptions grabDimension(Path rootdir, String i) {
        DimensionOptions ret = buildGrabber(DimensionOptions.CODEC, RegistryKeys.DIMENSION).grab_with_return(rootdir.toString()+"/dimension", i, false);
        close();
        return ret;
    }

    public void close() {
        baseRegistryManager.streamAllRegistries().forEach((entry) -> entry.value().freeze());
    }

    public RegistryOps.RegistryInfoGetter getGetter() {
        final Map<RegistryKey<? extends Registry<?>>, RegistryOps.RegistryInfo<?>> map = new HashMap<>();
        baseRegistryManager.streamAllRegistries().forEach((entry) -> map.put(entry.key(), createInfo(entry.value())));
        mutableRegistries.forEach(registry -> map.put(registry.getKey(), createMutableInfo(registry)));
        return new RegistryOps.RegistryInfoGetter() {
            public <T> Optional<RegistryOps.RegistryInfo<T>> getRegistryInfo(RegistryKey<? extends Registry<? extends T>> registryRef) {
                return Optional.ofNullable((RegistryOps.RegistryInfo<T>)map.get(registryRef));
            }
        };
    }

    public static <T> RegistryOps.RegistryInfo<T> createMutableInfo(MutableRegistry<T> registry) {
        return new RegistryOps.RegistryInfo<>(registry.getReadOnlyWrapper(), registry.createMutableEntryLookup(), registry.getLifecycle());
    }
    public static <T> RegistryOps.RegistryInfo<T> createInfo(Registry<T> registry) {
        return new RegistryOps.RegistryInfo<>(registry.getReadOnlyWrapper(), registry.getTagCreatingWrapper(), registry.getLifecycle());
    }
}