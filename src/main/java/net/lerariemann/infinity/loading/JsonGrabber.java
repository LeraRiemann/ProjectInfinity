package net.lerariemann.infinity.loading;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import net.lerariemann.infinity.InfinityMod;
import net.minecraft.registry.*;
import net.minecraft.util.Identifier;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;

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
        try {
            walk(rootdir).forEach(this::grab);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    void grab(Path path) {
        String path1 = path.toString();
        if (path1.endsWith(".json")) {
            String fullname = path1.substring(path1.lastIndexOf("/") + 1, path1.length() - 5);
            LogManager.getLogger().info("Grabbing "+fullname+" at "+path1);
            RegistryKey<E> key = RegistryKey.of(registry.getKey(), new Identifier(InfinityMod.MOD_ID, fullname));
            grab(path1, key);
            LogManager.getLogger().info("Grabbed " + key.toString());
        }
    }

    void grab(String path, RegistryKey<E> registryKey) {
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
        registry.add(registryKey, object, Lifecycle.stable());
    }

    E grab_with_return(String rootdir, int i, boolean register) {
        String path = rootdir + "/generated_" + i + ".json";
        RegistryKey<E> key = RegistryKey.of(registry.getKey(), new Identifier(InfinityMod.MOD_ID, "generated_"+i));
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
