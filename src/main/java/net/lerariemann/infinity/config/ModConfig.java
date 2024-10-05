package net.lerariemann.infinity.config;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.loader.api.FabricLoader;
import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.util.CommonIO;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.StringNbtReader;
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
    public boolean invocationLock = false;




    public static void load() {
        NbtCompound rootConfig = readRootConfig();
        var config = ModConfig.get();
        config.gamerules_chaosMobsEnabled = getBoolean(rootConfig, "gameRules", "chaosMobsEnabled");
        config.gamerules_forceSolidSurface = getBoolean(rootConfig, "gameRules", "forceSolidSurface");
        config.gamerules_consumePortalKey = getBoolean(rootConfig, "gameRules", "consumePortalKey");
        config.gamerules_longArithmeticEnabled = getBoolean(rootConfig, "gameRules", "longArithmeticEnabled");
        config.gamerules_runtimeGenerationEnabled = getBoolean(rootConfig, "gameRules", "runtimeGenerationEnabled");
        config.gamerules_returnPortalsEnabled = getBoolean(rootConfig, "gameRules", "returnPortalsEnabled");
        config.salt = getString(rootConfig, "salt");
        config.altarKey = getString(rootConfig, "altarKey");
        config.portalKey = getString(rootConfig, "portalKey");
        config.gamerules_maxBiomeCount = getInt(rootConfig, "gameRules", "maxBiomeCount");
        config.invocationLock = Files.exists(Path.of(configPath() + "/modular/invocation.lock"));
    }

    public static void save() {
        NbtCompound rootConfig = readRootConfig();
        var config = ModConfig.get();
        putBoolean(rootConfig, "gameRules", "chaosMobsEnabled", config.gamerules_chaosMobsEnabled);
        putBoolean(rootConfig, "gameRules", "forceSolidSurface", config.gamerules_forceSolidSurface);
        putBoolean(rootConfig, "gameRules", "consumePortalKey", config.gamerules_consumePortalKey);
        putBoolean(rootConfig, "gameRules", "longArithmeticEnabled", config.gamerules_longArithmeticEnabled);
        putBoolean(rootConfig, "gameRules", "runtimeGenerationEnabled", config.gamerules_runtimeGenerationEnabled);
        putBoolean(rootConfig, "gameRules", "returnPortalsEnabled", config.gamerules_returnPortalsEnabled);
        putString(rootConfig,  "salt", config.salt);
        putString(rootConfig, "altarKey", config.altarKey);
        putString(rootConfig, "portalKey", config.portalKey);
        putInt(rootConfig, "gameRules", "maxBiomeCount", config.gamerules_maxBiomeCount);

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

    // Enable and disable Easter Egg dimensions.
    public static <T> Consumer<T> mapSetter(HashMap.Entry<String, Boolean> field) {
        return t -> {
            boolean b = (Boolean)t;
            field.setValue(b);
        };
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