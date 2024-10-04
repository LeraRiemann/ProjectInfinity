package net.lerariemann.infinity.config;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.loader.api.FabricLoader;
import net.lerariemann.infinity.util.CommonIO;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.StringNbtReader;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public class ModConfig {
    private static ModConfig INSTANCE = new ModConfig();
    //General settings
    public boolean gamerules_consumePortalKey = true;
    public boolean gamerules_chaosMobsEnabled = true;
    public boolean gamerules_forceSolidSurface = false;


    public static void load() {
        NbtCompound rootConfig = readRootConfig();
        ModConfig.get().gamerules_chaosMobsEnabled = getBoolean(rootConfig, "gameRules", "chaosMobsEnabled");
        ModConfig.get().gamerules_forceSolidSurface = getBoolean(rootConfig, "gameRules", "forceSolidSurface");
        ModConfig.get().gamerules_consumePortalKey = getBoolean(rootConfig, "gameRules", "consumePortalKey");
    }

    public static void save() {
        NbtCompound rootConfig = readRootConfig();
        putBoolean(rootConfig, "gameRules", "chaosMobsEnabled", ModConfig.get().gamerules_chaosMobsEnabled);
        putBoolean(rootConfig, "gameRules", "forceSolidSurface", ModConfig.get().gamerules_forceSolidSurface);
        putBoolean(rootConfig, "gameRules", "consumePortalKey", ModConfig.get().gamerules_consumePortalKey);
        CommonIO.write(rootConfig, configPath(), "infinity.json");
    }

    public static boolean getBoolean(NbtCompound rootConfig, String compound, String bool) {
        return rootConfig.getCompound(compound).getBoolean(bool);
    }

    public static void putBoolean(NbtCompound rootConfig, String compound, String bool, boolean data) {
        rootConfig.getCompound(compound).putBoolean(bool, data);
    }

    public static String getString(NbtCompound rootConfig, String compound, String string) {
        return rootConfig.getCompound(compound).getString(string);
    }

    public static void putString(NbtCompound rootConfig, String compound, String bool, String data) {
        rootConfig.getCompound(compound).putString(bool, data);
    }

    public static String getString(NbtCompound rootConfig, String string) {
        return rootConfig.getString(string);
    }

    public static void putString(NbtCompound rootConfig, String bool, String data) {
        rootConfig.putString(bool, data);
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
}