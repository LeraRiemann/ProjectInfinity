package net.lerariemann.infinity.util.config;

import net.minecraft.item.Item;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.biome.Biome;

import java.util.function.BiFunction;
import java.util.function.Function;

/** Handles scanning of game content listed in a registry of choice.
 * <p>Filtering and formatting of content is delegated to the provided extractor function; these are defined in {@link ConfigGenerator}.
 * <p>Sorting and saving of content is delegated to {@link DataCollection}.
 * @param <S> type of content being scanned for: i.e. {@link Item}, {@link Biome}, etc.
 * @param <T> data output format; either {@link String}, {@link NbtCompound} or {@link NbtList} */
public class ConfigFactory<S, T> {
    Registry<S> reg;
    Function<RegistryKey<S>, T> extractor;
    ConfigFactory(Registry<S> reg, Function<RegistryKey<S>, T> extractor) {
        this.reg = reg;
        this.extractor = extractor;
    }

    static <S> ConfigFactory<S, String> of(Registry<S> r) {
        return new ConfigFactory<>(r, key -> key.getValue().toString());
    }
    static <S, T> ConfigFactory<S, T> of(Registry<S> r, BiFunction<Registry<S>, RegistryKey<S>, T> extractor) {
        return new ConfigFactory<>(r, key -> extractor.apply(r, key));
    }
    static <S, T> ConfigFactory<S, T> of(Registry<S> reg, Function<RegistryKey<S>, T> extractor) {
        return new ConfigFactory<>(reg, extractor);
    }

    void generate(String additionalPath, String name) {
        DataCollection.Logged<T> collection = new DataCollection.Logged<>(additionalPath, name);
        reg.getKeys().forEach(key -> {
            T entry = extractor.apply(key);
            if (entry != null) collection.add(key.getValue().getNamespace(), entry);
        });
        collection.save();
    }
}
