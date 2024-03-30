package net.lerariemann.infinity.client;

import net.lerariemann.infinity.util.CommonIO;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WorldSavePath;
import net.minecraft.world.World;

import java.io.File;

public record InfinityOptions(NbtCompound data) {
    public static InfinityOptions empty() {
        return new InfinityOptions(new NbtCompound());
    }
    public static InfinityOptions generate(MinecraftServer server, RegistryKey<World> worldKey) {
        String s = worldKey.getValue().toString();
        if (s.contains("generated_")) {
            String name = s.substring(s.lastIndexOf("generated_"));
            File f = server.getSavePath(WorldSavePath.DATAPACKS).resolve(name + "/data/infinity/options.json").toFile();
            if (f.exists()) {
                return new InfinityOptions(CommonIO.read(f));
            }
        }
        return empty();
    }

    public NbtCompound getShader() {
        return data.contains("shader") ? data.getCompound("shader") : new NbtCompound();
    }

    public float getSolarSize() {
        return data.contains("solar_size") ? data.getFloat("solar_size") : 30.0f;
    }

    public float getLunarSize() {
        return data.contains("lunar_size") ? data.getFloat("lunar_size") : 20.0f;
    }
}
