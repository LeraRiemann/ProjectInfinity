package net.lerariemann.infinity.util;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.lerariemann.infinity.InfinityMod;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class ConfigManager {

    static void registerConfig(Path path) {
        String path1 = path.toString();
        String fullname = path1.substring(path1.lastIndexOf("config") + 6);
        Path endfile = Paths.get("config/" + InfinityMod.MOD_ID + fullname);
        try {
            if (!endfile.toFile().exists() && fullname.endsWith(".json")) {
                int i = fullname.lastIndexOf("/");
                if (i>0) {
                    String directory_name = fullname.substring(0, i);
                    Files.createDirectories(Paths.get("config/" + InfinityMod.MOD_ID + directory_name));
                }
                Files.copy(path, endfile);
            }
            if (endfile.toFile().exists() && fullname.endsWith(".json")) {
                int version_old = CommonIO.getVersion(endfile.toFile());
                int version_new = CommonIO.getVersion(path.toFile());
                if (version_new > version_old) {
                    Files.copy(path, endfile, REPLACE_EXISTING);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static void registerAllConfigs() {
        ModContainer modContainer = FabricLoader.getInstance().getModContainer(InfinityMod.MOD_ID).orElse(null);
        try {
            Path path = Paths.get("config/" + InfinityMod.MOD_ID);
            if (!path.toFile().exists()) {
                Files.createDirectories(path);
            }

            Files.walk(modContainer.getRootPaths().get(0).resolve("config")).forEach(ConfigManager::registerConfig);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
