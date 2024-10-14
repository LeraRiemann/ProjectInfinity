package net.lerariemann.infinity.util.fabric;

import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;
/**
 * See {@link net.lerariemann.infinity.util.ConfigManager} for usages.
 */
@SuppressWarnings("unused")
public class ConfigManagerImpl {
    public static Path getBaseConfigDir() {
        return FabricLoader.getInstance().getConfigDir();
    }
}
