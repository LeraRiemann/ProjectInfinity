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
    public boolean gamerules_longArithmeticEnabled = false;
    public int gamerules_maxBiomeCount = 6;
    public boolean gamerules_chaosMobsEnabled = true;
    public boolean gamerules_returnPortalsEnabled = true;
    public boolean gamerules_runtimeGenerationEnabled = true;
    public boolean gamerules_forceSolidSurface = false;
    public String salt = "";
    public String altarKey = "minecraft:diamond";
    public String portalKey = "";



    public static void load() {
        NbtCompound rootConfig = readRootConfig();
        ModConfig.get().gamerules_chaosMobsEnabled = getBoolean(rootConfig, "gameRules", "chaosMobsEnabled");
        ModConfig.get().gamerules_forceSolidSurface = getBoolean(rootConfig, "gameRules", "forceSolidSurface");
        ModConfig.get().gamerules_consumePortalKey = getBoolean(rootConfig, "gameRules", "consumePortalKey");
        ModConfig.get().gamerules_longArithmeticEnabled = getBoolean(rootConfig, "gameRules", "longArithmeticEnabled");
        ModConfig.get().gamerules_runtimeGenerationEnabled = getBoolean(rootConfig, "gameRules", "runtimeGenerationEnabled");
        ModConfig.get().gamerules_returnPortalsEnabled = getBoolean(rootConfig, "gameRules", "returnPortalsEnabled");
        ModConfig.get().salt = getString(rootConfig, "salt");
        ModConfig.get().altarKey = getString(rootConfig, "altarKey");
        ModConfig.get().portalKey = getString(rootConfig, "portalKey");
        ModConfig.get().gamerules_maxBiomeCount = getInt(rootConfig, "gameRules", "maxBiomeCount");
    }

    public static void save() {
        NbtCompound rootConfig = readRootConfig();
        putBoolean(rootConfig, "gameRules", "chaosMobsEnabled", ModConfig.get().gamerules_chaosMobsEnabled);
        putBoolean(rootConfig, "gameRules", "forceSolidSurface", ModConfig.get().gamerules_forceSolidSurface);
        putBoolean(rootConfig, "gameRules", "consumePortalKey", ModConfig.get().gamerules_consumePortalKey);
        putBoolean(rootConfig, "gameRules", "longArithmeticEnabled", ModConfig.get().gamerules_longArithmeticEnabled);
        putBoolean(rootConfig, "gameRules", "runtimeGenerationEnabled", ModConfig.get().gamerules_runtimeGenerationEnabled);
        putBoolean(rootConfig, "gameRules", "returnPortalsEnabled", ModConfig.get().gamerules_returnPortalsEnabled);
        putString(rootConfig,  "salt", ModConfig.get().salt);
        putString(rootConfig, "altarKey", ModConfig.get().altarKey);
        putString(rootConfig, "portalKey", ModConfig.get().portalKey);
        putInt(rootConfig, "gameRules", "maxBiomeCount", ModConfig.get().gamerules_maxBiomeCount);

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