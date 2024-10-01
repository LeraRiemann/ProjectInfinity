package net.lerariemann.infinity.util.fabric;

import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;

public class ConfigManagerImpl {
    public static Path getBaseConfigDir() {
        return FabricLoader.getInstance().getConfigDir();
    }
}
