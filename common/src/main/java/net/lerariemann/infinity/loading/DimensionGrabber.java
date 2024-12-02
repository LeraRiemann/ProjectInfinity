package net.lerariemann.infinity.loading;

import com.mojang.serialization.Codec;
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

import static net.lerariemann.infinity.util.PlatformMethods.unfreeze;

public class DimensionGrabber {
    RegistryOps.RegistryInfoGetter registryInfoGetter;
    DynamicRegistryManager baseRegistryManager;

    public DimensionGrabber(DynamicRegistryManager brm) {
        baseRegistryManager = brm;
        List<MutableRegistry<?>> entries = new ArrayList<>();
         baseRegistryManager.streamAllRegistries().forEach((entry) -> {
             unfreeze(entry.value());
             entries.add((MutableRegistry<?>)entry.value());
         });
        registryInfoGetter = getGetter(entries);
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
        return grab_dimension(rootdir, d.getName());
    }

    <T> JsonGrabber<T> buildGrabber(Codec<T> codec, RegistryKey<Registry<T>> key) {
        return (new JsonGrabber<>(registryInfoGetter, codec, (MutableRegistry<T>) (baseRegistryManager.get(key))));
    }

    <T> void grab_one_for_client(Codec<T> codec, RegistryKey<Registry<T>> key, Identifier id, NbtCompound optiondata) {
        if (!(baseRegistryManager.get(key).contains(RegistryKey.of(key, id)))) buildGrabber(codec, key).grab(id, optiondata, false);
    }

    public void grab_dim_for_client(Identifier id, NbtCompound dimdata) {
        if (!dimdata.isEmpty()) grab_one_for_client(DimensionType.CODEC, RegistryKeys.DIMENSION_TYPE, id, dimdata);
    }

    public void grab_biome_for_client(Identifier id, NbtCompound biomedata) {
        grab_one_for_client(Biome.CODEC, RegistryKeys.BIOME, id, biomedata);
    }

    DimensionOptions grab_dimension(Path rootdir, String i) {
        DimensionOptions ret = buildGrabber(DimensionOptions.CODEC, RegistryKeys.DIMENSION).grab_with_return(rootdir.toString()+"/dimension", i, false);
        close();
        return ret;
    }

    void close() {
        baseRegistryManager.streamAllRegistries().forEach((entry) -> entry.value().freeze());
    }

    public RegistryOps.RegistryInfoGetter getGetter(List<MutableRegistry<?>> additionalRegistries) {
        final Map<RegistryKey<? extends Registry<?>>, RegistryOps.RegistryInfo<?>> map = new HashMap<>();
        baseRegistryManager.streamAllRegistries().forEach((entry) -> map.put(entry.key(), createInfo((MutableRegistry<?>)(entry.value()))));
        additionalRegistries.forEach(first -> map.put(first.getKey(), createInfo(first)));
        return new RegistryOps.RegistryInfoGetter() {
            public <T> Optional<RegistryOps.RegistryInfo<T>> getRegistryInfo(RegistryKey<? extends Registry<? extends T>> registryRef) {
                return Optional.ofNullable((RegistryOps.RegistryInfo<T>)map.get(registryRef));
            }
        };
    }

    public static <T> RegistryOps.RegistryInfo<T> createInfo(MutableRegistry<T> registry) {
        return new RegistryOps.RegistryInfo<>(registry.getReadOnlyWrapper(), registry.createMutableEntryLookup(), registry.getLifecycle());
    }
}
