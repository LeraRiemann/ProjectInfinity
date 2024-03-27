package net.lerariemann.infinity.loading;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.util.CommonIO;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.*;
import net.minecraft.util.Identifier;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import static java.nio.file.Files.walk;

public class JsonGrabber<E> {
    Codec<E> decoder;
    MutableRegistry<E> registry;
    RegistryOps.RegistryInfoGetter registryInfoGetter;

    JsonGrabber(RegistryOps.RegistryInfoGetter get, Codec<E> dec, MutableRegistry<E> reg) {
        decoder = dec;
        registry = reg;
        registryInfoGetter = get;
    }

    void grab_all(Path rootdir) {
        grab_all(rootdir, false);
    }

    void grab_all(Path rootdir, boolean bl) {
        try {
            walk(rootdir).forEach(a -> grab(a, bl));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    void grab(Path path, boolean bl) {
        String path1 = path.toString();
        if (path1.endsWith(".json")) {
            String fullname = path1.substring(path1.lastIndexOf("/") + 1, path1.length() - 5);
            int i = fullname.lastIndexOf("\\");
            if (i>=0) fullname = fullname.substring(i + 1);
            RegistryKey<E> key = RegistryKey.of(registry.getKey(), InfinityMod.getId(fullname));
            grab(path1, key, bl);
        }
    }

    void grab(Identifier id, NbtCompound compound, boolean bl) {
        grab(RegistryKey.of(registry.getKey(), id), JsonParser.parseString(CommonIO.CompoundToString(compound, 0)), bl);
    }

    void grab(RegistryKey<E> key, JsonElement jsonElement, boolean bl) {
        RegistryOps<JsonElement> registryOps = RegistryOps.of(JsonOps.INSTANCE, registryInfoGetter);
        DataResult<E> dataResult = decoder.parse(registryOps, jsonElement);
        E object = dataResult.getOrThrow(false, (error) -> {
        });
        if (bl || !registry.contains(key)) registry.add(key, object, Lifecycle.stable());
    }

    void grab(String path, RegistryKey<E> registryKey, boolean bl) {
        File file = new File(path);
        String content;
        try {
            content = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        JsonElement jsonElement = JsonParser.parseString(content);
        grab(registryKey, jsonElement, bl);
    }

    E grab_with_return(String rootdir, String i, boolean register) {
        String path = rootdir + "/" + i + ".json";
        RegistryKey<E> key = RegistryKey.of(registry.getKey(), InfinityMod.getId("generated_"+i));
        File file = new File(path);
        String content;
        try {
            content = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        JsonElement jsonElement = JsonParser.parseString(content);
        RegistryOps<JsonElement> registryOps = RegistryOps.of(JsonOps.INSTANCE, registryInfoGetter);
        DataResult<E> dataResult = decoder.parse(registryOps, jsonElement);
        E object = dataResult.getOrThrow(false, (error) -> {
        });
        if (register) registry.add(key, object, Lifecycle.stable());
        return object;
    }
}
