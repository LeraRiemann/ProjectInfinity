package net.lerariemann.infinity.util;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.fabricmc.loader.api.FabricLoader;
import net.lerariemann.infinity.InfinityMod;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import org.apache.logging.log4j.LogManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class ConfigManager {
    static boolean registerConfig(Path path) {
        boolean bl = false;
        String path1 = path.toString();
        String fullname = path1.substring(path1.lastIndexOf("config") + 6);
        Path endfile = Paths.get(getConfigDir() + fullname);
        try {
            if (!endfile.toFile().exists() && fullname.endsWith(".json")) {
                String separator;
                if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
                    separator = File.separator;
                }
                else {
                    separator = "/";
                }

                int i = fullname.lastIndexOf(separator);
                if (i>0) {
                    String directory_name = fullname.substring(0, i);
                    Files.createDirectories(Paths.get(getConfigDir()+ directory_name));
                }
                Files.copy(path, endfile);
                bl = true;
            }
            if (endfile.toFile().exists() && fullname.endsWith(".json")) {
                if (compareVersions(endfile, path)) {
                    Files.copy(path, endfile, REPLACE_EXISTING);
                    bl = true;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return bl;
    }

    public static boolean compareVersions(Path oldFile, Path newFile) throws IOException {
        Path tempfile = Paths.get(getBaseConfigDir()+"/infinity-temp.json");
        int version_old = CommonIO.getVersion(oldFile.toFile());
        Files.copy(newFile, tempfile, REPLACE_EXISTING);
        int version_new = CommonIO.getVersion(tempfile.toFile());
        return version_new > version_old;
    }

    public static void unpackDefaultConfigs() {
        AtomicBoolean bl2 = new AtomicBoolean(false);
        try {
            Path path = getConfigDir();
            if (!path.toFile().exists()) {
                Files.createDirectories(path);
            }

            Files.walk(InfinityMod.rootResPath.resolve("config")).forEach(p -> {
                if (!p.toString().contains("util")) {
                    boolean bl = registerConfig(p);
                    if (bl && p.toString().contains("evicted_files.json")) bl2.set(true);
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (bl2.get()) evictOldFiles();
    }

    public static void updateInvocationLock() {
        File invlock = InfinityMod.invocationLock.toFile();
        if (invlock.exists()) {
            try {
                if (compareVersions(InfinityMod.invocationLock, InfinityMod.utilPath.resolve( "invocation.lock"))) {
                    LogManager.getLogger().info("Deleting outdated modular configs");
                    Files.walk(getConfigDir().resolve("modular")).forEach(p -> {
                        if (p.toFile().isFile()) {
                            try {
                                Files.delete(p);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    });
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @ExpectPlatform
    public static Path getBaseConfigDir() {
        throw new AssertionError();
    }

    public static Path getConfigDir() {
        return Path.of("config", InfinityMod.MOD_ID);
    }

    public static void evictOldFiles() {
        LogManager.getLogger().info("Evicting old files");
        NbtCompound c = CommonIO.read(getConfigDir() + "/hardcoded/evicted_files.json");
        NbtList l = c.getList("content", NbtElement.STRING_TYPE);
        try {
            for (NbtElement e : l) {
                Path path1 = Paths.get(getConfigDir() + e.asString());
                LogManager.getLogger().info(path1);
                if (path1.toFile().exists()) {
                    Path path2 = Paths.get(getConfigDir() + "/evicted/" + e.asString());
                    Files.createDirectories(path2);
                    Files.copy(path1, path2, REPLACE_EXISTING);
                    Files.delete(path1);
                }
            }
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
