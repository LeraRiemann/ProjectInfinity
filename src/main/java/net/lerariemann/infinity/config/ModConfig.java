package net.lerariemann.infinity.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.loader.api.FabricLoader;
import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.util.CommonIO;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.text.Text;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.function.Consumer;

public class ModConfig {
    private static ModConfig INSTANCE = new ModConfig();
    //General settings
    public boolean invocationLock = false;




    public static void load() {
        NbtCompound rootConfig = readRootConfig();



    }

    public static void save() {
        NbtCompound rootConfig = readRootConfig();
        var config = ModConfig.get();


        if (!config.invocationLock) {
            try {
                if (Files.exists(Path.of(configPath() + "/modular/invocation.lock"))) {
                    Files.delete(Path.of(configPath() + "/modular/invocation.lock"));
                }
                else config.invocationLock = true;
            } catch (IOException e) {
               InfinityMod.LOGGER.error("Unable to delete invocation.lock!");
               config.invocationLock = true;
            }
        }

        CommonIO.write(rootConfig, configPath(), "infinity.json");
    }


    public static boolean getBoolean(NbtCompound rootConfig, String compound, String bool) {
        return rootConfig.getCompound(compound).getBoolean(bool);
    }

    public static void putBoolean(NbtCompound rootConfig, String compound, String bool, boolean data) {
        rootConfig.getCompound(compound).putBoolean(bool, data);
    }

    public static int getInt(NbtCompound rootConfig, String compound, String key) {
        return rootConfig.getCompound(compound).getInt(key);
    }

    public static void putInt(NbtCompound rootConfig, String compound, String key, int data) {
        rootConfig.getCompound(compound).putInt(key, data);
    }

    public static String getString(NbtCompound rootConfig, String compound, String string) {
        return rootConfig.getCompound(compound).getString(string);
    }

    public static void putString(NbtCompound rootConfig, String compound, String key, String data) {
        rootConfig.getCompound(compound).putString(key, data);
    }

    public static String getString(NbtCompound rootConfig, String string) {
        return rootConfig.getString(string);
    }

    public static void putString(NbtCompound rootConfig, String key, String data) {
        rootConfig.putString(key, data);
    }

    public static ModConfig get() {
        if (INSTANCE == null) INSTANCE = new ModConfig();
        return INSTANCE;
    }

    static Path configPath() {
        return Path.of(FabricLoader.getInstance().getConfigDir() + "/infinity");
    }

    public static NbtCompound readRootConfig() {
        return read(configPath() + "/infinity.json");
    }

    public static JsonElement readRootConfigJSON() {
        return readJSON(configPath() + "/infinity.json");
    }

    public static NbtCompound read(String file) {
        File newFile = new File(file);
        String content;
        try {
            content = FileUtils.readFileToString(newFile, StandardCharsets.UTF_8);

            NbtCompound c = StringNbtReader.parse(content);
            return c;
        } catch (IOException | CommandSyntaxException e) {
            throw new RuntimeException(e);
        }
    }
    public static JsonElement readJSON(String file) {

        File newFile = new File(file);
        String content;
        try {
            content = FileUtils.readFileToString(newFile, StandardCharsets.UTF_8);
            var c = JsonParser.parseString(content);
            return c;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}