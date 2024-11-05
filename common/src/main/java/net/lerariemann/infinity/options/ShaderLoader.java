package net.lerariemann.infinity.options;

import dev.architectury.platform.Platform;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.access.GameRendererAccess;
import net.lerariemann.infinity.util.CommonIO;
import net.minecraft.client.MinecraftClient;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.resource.ResourcePackManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Environment(EnvType.CLIENT)
public class ShaderLoader {
    public static String FILENAME = "current.json";
    public static Path shaderDir(MinecraftClient client) {
        return client.getResourcePackDir().resolve("infinity/assets/infinity/shaders");
    }

    public static void reloadShaders(MinecraftClient client, boolean bl) {
        try {
            load(client);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if(bl && shaderDir(client).resolve(FILENAME).toFile().exists()) {
            ((GameRendererAccess)(client.gameRenderer)).projectInfinity$loadPP(InfinityMod.getId("shaders/" + FILENAME));
            return;
        }
        client.gameRenderer.disablePostProcessor();
        if (!resourcesReloaded) {
            client.reloadResources();
            resourcesReloaded = true;
        }
    }

    public static boolean resourcesReloaded = Path.of(Platform.getGameFolder() + "/resourcepacks/infinity/assets/infinity/shaders").toFile().exists();

    static void load(MinecraftClient client) throws IOException {
        ResourcePackManager m = client.getResourcePackManager();
        Path path = client.getResourcePackDir().resolve("infinity");
        String name = "file/" + path.getFileName().toString();
        Files.createDirectories(path.resolve("assets/infinity/shaders"));
        if (!path.resolve("pack.mcmeta").toFile().exists()) CommonIO.write(packMcmeta(), path.toString(), "pack.mcmeta");
        m.scanPacks();
        m.enable(name);
    }

    private static NbtCompound packMcmeta() {
        NbtCompound res = new NbtCompound();
        NbtCompound pack = new NbtCompound();
        pack.putInt("pack_format", 15);
        pack.putString("description", "Shader container");
        res.put("pack", pack);
        return res;
    }
}
