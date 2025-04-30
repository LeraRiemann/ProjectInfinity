package net.lerariemann.infinity.dimensions;

import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.util.config.ConfigManager;
import net.lerariemann.infinity.util.core.ConfigType;
import net.lerariemann.infinity.util.core.NbtUtils;
import net.minecraft.nbt.NbtCompound;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class RandomText extends RandomStructure {
    public static List<Path> mod_resources;
    public static void walkPaths() {
        mod_resources = new ArrayList<>();
        try (Stream<Path> files = Files.walk(InfinityMod.rootConfigPathInJar)) {
            files.filter(p -> p.toString().endsWith(".json")).forEach(p -> mod_resources.add(p));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    RandomText(int i, RandomBiome b) {
        super(i, b);
    }

    @Override
    void addData() {
        RandomDimension daddy = parent.parent;
        type = "infinity:text";
        name = "text_" + id;
        data = new NbtCompound();
        data.putString("type", "infinity:text");
        data.putString("step", "surface_structures");
        data.put("spawn_overrides", new NbtCompound());
        data.putString("biomes", parent.fullname);
        data.put("block", daddy.PROVIDER.randomBlockProvider(random, ConfigType.FULL_BLOCKS_WG));
        data.put("y", NbtUtils.randomHeightProvider(random,
                daddy.sea_level, daddy.min_y+daddy.height,
                true, true));
        data.putString("text", genText(random));
    }

    public static String genText(Random random) {
        try {
            return genTextFromModResources(random);
        }
        catch (Exception e) {
            return genTextRandomly(random);
        }
    }

    static String genTextFromModResources(Random random) throws IOException {
        Path tempfile = ConfigManager.tempFile;
        Path p = mod_resources.get(random.nextInt(mod_resources.size()));
        Files.copy(p, tempfile, REPLACE_EXISTING);
        return genTextFromFile(random, tempfile.toFile());
    }

    static String genTextFromFile(Random random, File f) throws IOException {
        try {
            return select(random, FileUtils.readFileToString(f, StandardCharsets.UTF_8));
        }
        catch (Exception e) {
            throw new IOException();
        }
    }

    static String select(Random random, String str) {
        String[] lst = str.split("\n");
        int i2 = random.nextInt(1, 16);
        if (lst.length <= i2) return str;
        int i = random.nextInt(0, lst.length - i2);
        int j;
        StringBuilder res = new StringBuilder();
        res.append(lst[i]);
        for (j = 1; j<i2; j++) {
            res.append("$n").append(lst[i+j]);
        }
        return res.toString();
    }

    public static String genTextRandomly(Random random) {
        return genTextRandomly(random, 64);
    }

    public static String genTextRandomly(Random random, int bound) {
        String s = "1234567890QWERTYUIOPASDFGHJKLZXCVBNMqwertyuiopasdfghjklzxcvbnm";
        StringBuilder res = new StringBuilder();
        for (int j = 0; j<bound; j++) res.append(s.charAt(random.nextInt(s.length())));
        return res.toString();
    }
}
