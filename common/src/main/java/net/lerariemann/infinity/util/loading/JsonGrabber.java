package net.lerariemann.infinity.util.loading;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.lerariemann.infinity.util.core.CommonIO;
import net.lerariemann.infinity.util.InfinityMethods;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.*;
import net.minecraft.registry.entry.RegistryEntryInfo;
import net.minecraft.util.Identifier;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.stream.Stream;

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

    public void grabAll(Path rootdir) {
        grabAll(rootdir, false);
    }

    void grabAll(Path rootdir, boolean bl) {
        if(!rootdir.toFile().exists()) return;
        try (Stream<Path> files = walk(rootdir)) {
            files.forEach(a -> grab(a, bl));
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
            RegistryKey<E> key = RegistryKey.of(registry.getKey(), InfinityMethods.getId(fullname));
            grab(path1, key, bl);
        }
    }

    void grab(Identifier id, NbtCompound compound, boolean bl) {
        grab(RegistryKey.of(registry.getKey(), id), JsonParser.parseString(CommonIO.compoundToString(compound)), bl);
    }

    void grab(RegistryKey<E> key, JsonElement jsonElement, boolean bl) {
        RegistryOps<JsonElement> registryOps = RegistryOps.of(JsonOps.INSTANCE, registryInfoGetter);
        try {
            DataResult<E> dataResult = decoder.parse(registryOps, jsonElement);
            if(dataResult.result().isPresent()) {
                E object = dataResult.result().get();
                if (bl || !registry.contains(key)) registry.add(key, object, RegistryEntryInfo.DEFAULT);
            }
            else {
                LogManager.getLogger().info(jsonElement);
            }
        }
        catch (Exception e) {
            LogManager.getLogger().info(jsonElement);
            throw new RuntimeException(e.getMessage() + " Element affected: " + jsonElement.toString());
        }
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

    E grabWithReturn(String rootdir, String i, boolean register) {
        String path = rootdir + "/" + i + ".json";
        RegistryKey<E> key = RegistryKey.of(registry.getKey(), InfinityMethods.getId(i));
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
        E object = dataResult.getOrThrow((error) -> null);
        if (register) registry.add(key, object, RegistryEntryInfo.DEFAULT);
        return object;
    }
}
