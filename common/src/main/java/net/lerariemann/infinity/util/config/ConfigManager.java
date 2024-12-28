package net.lerariemann.infinity.util.config;

import dev.architectury.platform.Platform;
import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.util.core.CommonIO;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import org.apache.logging.log4j.LogManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static net.lerariemann.infinity.InfinityMod.configPath;

/**
 * Methods for unpacking and updating config files from the mod's jar.
 * @author LeraRiemann
 */
public interface ConfigManager {
    /** Allows to bypass inability to read files directly from the jar by first copying them to here. */
    Path tempFile = Platform.getConfigFolder().resolve(".infinity-temp.json");

    /** If the game was last started in a version of the mod that uses a different modular config format,
     * this deletes the old configs ensuring they will be regenerated in the correct format
     * by {@link ConfigFactory} on next world load. */
    static void updateInvocationLock() {
        File invlock = InfinityMod.invocationLock.toFile();
        if (!invlock.exists()) return;
        try {
            if (!compareVersions(InfinityMod.invocationLock, InfinityMod.rootConfigPathInJar.resolve( ".util/invocation.lock"))) return;
            try (Stream<Path> files = Files.walk(configPath.resolve("modular"))) {
                InfinityMod.LOGGER.info("Deleting outdated modular configs");
                files.map(Path::toFile).filter(File::isFile).forEach(f -> {
                    if (!f.delete()) InfinityMod.LOGGER.info("Cannot delete file {}", f);
                });
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static void unpackDefaultConfigs() {
        try(Stream<Path> files = Files.walk(InfinityMod.rootConfigPathInJar)) {
            if (!configPath.toFile().exists()) Files.createDirectories(configPath);
            files.filter(p -> p.toString().endsWith(".json")).forEach(p -> registerConfig(p, configPath));
            Files.deleteIfExists(tempFile); //being a good method cleaning up after itself :3
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates or updates an individual config file.
     */
    static boolean registerConfig(Path fromPath, Path toDirectory) {
        String path1 = fromPath.toString();
        String fullname = path1.substring(path1.lastIndexOf("config") + 6);
        Path endfile = Paths.get(toDirectory + fullname);
        try {
            if (!endfile.toFile().exists()) {
                String separator;
                if (Platform.isDevelopmentEnvironment()) {
                    separator = File.separator;
                }
                else {
                    separator = "/";
                }

                int i = fullname.lastIndexOf(separator);
                if (i>0) {
                    String directory_name = fullname.substring(0, i);
                    Files.createDirectories(Paths.get(configPath + directory_name));
                }
                Files.copy(fromPath, endfile);
                return true;
            }
            if (compareVersions(endfile, fromPath)) {
                Files.copy(fromPath, endfile, REPLACE_EXISTING);
                return true;
            }
            return false;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * If a config file that exists on the disk is in a deprecated format, this method detects that fact,
     * allowing it to be updated by {@link net.lerariemann.infinity.util.config.ConfigManager#registerConfig(java.nio.file.Path, java.nio.file.Path)}.
     */
    static boolean compareVersions(Path oldFile, Path newFile) throws IOException {
        int version_old = CommonIO.getVersion(oldFile.toFile());
        Files.copy(newFile, tempFile, REPLACE_EXISTING);
        int version_new = CommonIO.getVersion(tempFile.toFile());
        return version_new > version_old;
    }

    @Deprecated
    static void evictOldFiles() {
        InfinityMod.LOGGER.info("Evicting old files");
        NbtCompound c = CommonIO.read(InfinityMod.utilPath.resolve("evicted_files.json"));
        NbtList l = c.getList("content", NbtElement.STRING_TYPE);
        try {
            for (NbtElement e : l) {
                Path path1 = configPath.resolve(e.asString());
                LogManager.getLogger().info(path1);
                if (path1.toFile().exists()) {
                    Path path2 = configPath.resolve("evicted").resolve(e.asString());
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
