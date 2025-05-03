package net.lerariemann.infinity.util.neoforge;

import net.lerariemann.infinity.util.config.ConfigManager;
import net.neoforged.fml.loading.FMLConfig;

import java.nio.file.Path;
/**
 * See {@link ConfigManager} for usages.
 */
@SuppressWarnings("unused")
public class ConfigManagerImpl {
    public static Path getBaseConfigDir() {
        return Path.of(FMLConfig.defaultConfigPath());
    }
}
