package net.lerariemann.infinity.util.config;

import net.lerariemann.infinity.util.core.ConfigType;
import net.minecraft.item.Item;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;

import java.util.function.BiFunction;
import java.util.function.Function;

/** Handles scanning of game content listed in a registry of choice.
 * <p>Filtering and formatting of content is delegated to the provided extractor function; these are defined in {@link ConfigGenerator}.
 * <p>Sorting and saving of content is delegated to {@link DataCollection}.
 * @param <S> type of content being scanned for: i.e. {@link Item}, {@link Biome}, etc. */
public class ConfigFactory<S> {
    Registry<S> reg;
    Function<RegistryKey<S>, NbtCompound> dataExtractor;
    ConfigFactory(Registry<S> reg, Function<RegistryKey<S>, NbtCompound> extractor) {
        this.reg = reg;
        this.dataExtractor = extractor;
    }

    static <S> ConfigFactory<S> of(Registry<S> r) {
        return new ConfigFactory<>(r, key -> new NbtCompound());
    }
    static <S> ConfigFactory<S> of(Registry<S> r, BiFunction<Registry<S>, RegistryKey<S>, NbtCompound> extractor) {
        return new ConfigFactory<>(r, key -> extractor.apply(r, key));
    }
    static <S> ConfigFactory<S> of(Registry<S> reg, Function<RegistryKey<S>, NbtCompound> extractor) {
        return new ConfigFactory<>(reg, extractor);
    }

    void generate(ConfigType type) {
        DataCollection.Logged collection = new DataCollection.Logged(type);
        reg.getKeys().forEach(key -> {
            NbtCompound data = dataExtractor.apply(key);
            if (data != null) {
                Identifier id = key.getValue();
                if (data.isEmpty()) collection.addIdentifier(id);
                else collection.add(id.getNamespace(), id.toString(), data);
            }
        });
        collection.save();
    }
}
