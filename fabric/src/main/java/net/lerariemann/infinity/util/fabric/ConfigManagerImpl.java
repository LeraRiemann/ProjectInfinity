package net.lerariemann.infinity.util.fabric;

import net.fabricmc.loader.api.FabricLoader;
import net.lerariemann.infinity.util.config.ConfigManager;

import java.nio.file.Path;
/**
 * See {@link ConfigManager} for usages.
 */
@SuppressWarnings("unused")
public class ConfigManagerImpl {
    public static Path getBaseConfigDir() {
        return FabricLoader.getInstance().getConfigDir();
    }
}
