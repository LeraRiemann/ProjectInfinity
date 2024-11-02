package net.lerariemann.infinity.util;

import com.google.gson.JsonElement;
import net.lerariemann.infinity.InfinityMod;
import net.minecraft.loot.LootDataType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.WorldSavePath;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

public class RandomLootDrops {
    public static void genAll(int seed, MinecraftServer s) {
        List<Identifier> a = s.getLootManager().getIds(LootDataType.LOOT_TABLES).stream().toList();
        List<Integer> l = new java.util.ArrayList<>(IntStream.rangeClosed(0, a.size() - 1).boxed().toList());
        Collections.shuffle(l, new Random(seed));
        for (int i = 0; i < a.size(); i++) {
            try {
                gen(s, a.get(i), a.get(l.get(i)));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    static void gen(MinecraftServer s, Identifier dataFrom, Identifier nameFrom) throws IOException {
        JsonElement e = LootDataType.LOOT_TABLES.getGson().toJsonTree(s.getLootManager().getLootTable(dataFrom));
        int i = nameFrom.getPath().lastIndexOf("/");
        String before = i < 0 ? "" : "/" + nameFrom.getPath().substring(0, i);
        String after = i < 0 ? nameFrom.getPath() : nameFrom.getPath().substring(i+1);
        String directory = s.getSavePath(WorldSavePath.DATAPACKS).toString() + "/" + InfinityMod.MOD_ID +
                "/data/" + nameFrom.getNamespace() + "/loot_tables" + before;
        Files.createDirectories(Paths.get(directory));
        Files.write(Paths.get(directory + "/" + after + ".json"), Collections.singletonList(e.toString()), StandardCharsets.UTF_8);
    }
}
