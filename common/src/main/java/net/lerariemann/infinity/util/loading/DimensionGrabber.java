package net.lerariemann.infinity.util.loading;

import com.mojang.serialization.Codec;
import net.lerariemann.infinity.access.RegistryAccess;
import net.lerariemann.infinity.dimensions.RandomDimension;
import net.minecraft.client.MinecraftClient;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.*;
import net.minecraft.server.MinecraftServer;
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

/**
 * Prepares Minecraft's registries for runtime injection of generated content.
 * Delegates the act of injection itself to per-registry {@link JsonGrabber}s.
 * @see net.lerariemann.infinity.mixin.core.SimpleRegistryMixin
 */
public class DimensionGrabber {
    DynamicRegistryManager baseRegistryManager;
    Set<MutableRegistry<?>> mutableRegistries = new HashSet<>();
    RegistryOps.RegistryInfoGetter registryInfoGetter;

    public DimensionGrabber(DynamicRegistryManager brm) {
        this(brm, Set.of(RegistryKeys.CONFIGURED_FEATURE,
                RegistryKeys.PLACED_FEATURE,
                RegistryKeys.CONFIGURED_CARVER,
                RegistryKeys.BIOME,
                RegistryKeys.STRUCTURE,
                RegistryKeys.STRUCTURE_SET,
                RegistryKeys.CHUNK_GENERATOR_SETTINGS,
                RegistryKeys.DIMENSION_TYPE,
                RegistryKeys.DIMENSION));
    }

    /**
     * @param unfrozenKeys a set of keys for all registries we want to modify.
     * unfortunately, unfreezing and refreezing literally everything leads to problems on neoforge servers
     */
    public DimensionGrabber(DynamicRegistryManager brm, Set<RegistryKey<? extends Registry<?>>> unfrozenKeys) {
        baseRegistryManager = brm;
        baseRegistryManager.streamAllRegistries().forEach((entry) -> {
            if (unfrozenKeys.contains(entry.key())) {
                ((RegistryAccess)entry.value()).infinity$unfreeze();
                mutableRegistries.add((MutableRegistry<?>)entry.value());
            }
        });
        registryInfoGetter = getGetter();
    }

    /**
     * Grabs everything within a dimension's datapack.
     * @return a {@link DimensionOptions} object containing all necessary headers for creating the actual playable {@link net.minecraft.world.World}.
     */
    public static DimensionOptions readDimensionFromDisk(RandomDimension d) {
        DimensionGrabber grabber = new DimensionGrabber(d.server.getRegistryManager());
        Path rootdir = Paths.get(d.getStoragePath());
        grabber.grabAllRelatedData(rootdir);
        DimensionOptions options = grabber.grabDimension(rootdir, d.getName());
        grabber.close();
        return options;
    }

    /**
     * Grabs contents of all files in a directory. Used by {@link net.lerariemann.infinity.util.config.SoundScanner} to runtime-inject jukebox song definition data.
     */
    public static <T> void readCategoryFromDisk(MinecraftServer server, Codec<T> codec, RegistryKey<Registry<T>> registryKey, Path path) {
        DimensionGrabber grabber = new DimensionGrabber(server.getRegistryManager(), Set.of(registryKey));
        grabber.buildGrabber(codec, registryKey).grabAll(path);
        grabber.close();
    }

    /**
     * Inserts one object into one registry. Used on the client to insert individual dimension types and biomes when the server asks it to, since the client
     * does not know or care about everything else a dimension datapack holds.
     * @see net.lerariemann.infinity.registry.var.ModPayloads
     */
    public static <T> void grabObjectForClient(MinecraftClient client, Codec<T> codec, RegistryKey<Registry<T>> registryKey,
                                           Identifier id, NbtCompound data) {
        if (data.isEmpty()) return;
        DimensionGrabber grabber = new DimensionGrabber(
                Objects.requireNonNull(client.getNetworkHandler()).getRegistryManager(),
                Set.of(registryKey)
        );
        grabber.grabObjectForClient(codec, registryKey, id, data);
        grabber.close();
    }

    private void grabAllRelatedData(Path rootdir) {
        buildGrabber(ConfiguredFeature.CODEC, RegistryKeys.CONFIGURED_FEATURE).grabAll(rootdir.resolve("worldgen/configured_feature"));
        buildGrabber(PlacedFeature.CODEC, RegistryKeys.PLACED_FEATURE).grabAll(rootdir.resolve("worldgen/placed_feature"), true);
        buildGrabber(ConfiguredCarver.CODEC, RegistryKeys.CONFIGURED_CARVER).grabAll(rootdir.resolve("worldgen/configured_carver"));
        buildGrabber(Biome.CODEC, RegistryKeys.BIOME).grabAll(rootdir.resolve("worldgen/biome"));
        buildGrabber(Structure.STRUCTURE_CODEC, RegistryKeys.STRUCTURE).grabAll(rootdir.resolve("worldgen/structure"));
        buildGrabber(StructureSet.CODEC, RegistryKeys.STRUCTURE_SET).grabAll(rootdir.resolve("worldgen/structure_set"));
        buildGrabber(ChunkGeneratorSettings.CODEC, RegistryKeys.CHUNK_GENERATOR_SETTINGS).grabAll(rootdir.resolve("worldgen/noise_settings"));
        buildGrabber(DimensionType.CODEC, RegistryKeys.DIMENSION_TYPE).grabAll(rootdir.resolve("dimension_type"));
    }

    private DimensionOptions grabDimension(Path rootdir, String i) {
        DimensionOptions ret = buildGrabber(DimensionOptions.CODEC, RegistryKeys.DIMENSION).grabWithReturn(rootdir.toString()+"/dimension", i, false);
        close();
        return ret;
    }

    private <T> JsonGrabber<T> buildGrabber(Codec<T> codec, RegistryKey<Registry<T>> key) {
        return (new JsonGrabber<>(registryInfoGetter, codec, (MutableRegistry<T>) (baseRegistryManager.getOrThrow(key))));
    }

    private <T> void grabObjectForClient(Codec<T> codec, RegistryKey<Registry<T>> key, Identifier id, NbtCompound optiondata) {
        if (!(baseRegistryManager.getOrThrow(key).contains(RegistryKey.of(key, id)))) buildGrabber(codec, key).grab(id, optiondata, false);
    }

    /** Freezes all the registries after the grabbing is done, since registries can't be left unfrozen when the game's running. */
    private void close() {
        baseRegistryManager.streamAllRegistries().forEach((entry) -> entry.value().freeze());
    }

    /**
     * Creates and returns a {@link RegistryOps.RegistryInfoGetter}, which will be used by {@link JsonGrabber}s to translate raw json into all sorts of objects.
     */
    private RegistryOps.RegistryInfoGetter getGetter() {
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
        return new RegistryOps.RegistryInfo<>(registry, registry.createMutableRegistryLookup(), registry.getLifecycle());
    }
    public static <T> RegistryOps.RegistryInfo<T> createInfo(Registry<T> registry) {
        return new RegistryOps.RegistryInfo<>(registry, registry, registry.getLifecycle());
    }
}