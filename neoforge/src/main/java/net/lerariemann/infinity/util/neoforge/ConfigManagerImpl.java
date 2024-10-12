package net.lerariemann.infinity.util.neoforge;

import net.neoforged.fml.loading.FMLConfig;

import java.nio.file.Path;

public class ConfigManagerImpl {
    public static Path getBaseConfigDir() {
        return Path.of(FMLConfig.defaultConfigPath());
    }
}
