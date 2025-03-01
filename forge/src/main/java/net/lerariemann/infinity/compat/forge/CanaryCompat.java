package net.lerariemann.infinity.compat.forge;

import net.lerariemann.infinity.util.core.CommonIO;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class CanaryCompat {
    private static final String CONFIG_PATH = "config/canary.properties";
    private static final String CONFIG_RULE = "mixin.ai.poi.fast_portals";

    public static void writeCompatFile() {
        String configContents;
        try {
            configContents = FileUtils.readFileToString(new File(CONFIG_PATH), "utf-8");
        } catch (IOException e) {
            configContents = "";
        }
        if (!configContents.contains(CONFIG_RULE))
            configContents += CONFIG_RULE + "=false";
        CommonIO.write(configContents, Path.of("config"), "canary.properties");
    }
}
