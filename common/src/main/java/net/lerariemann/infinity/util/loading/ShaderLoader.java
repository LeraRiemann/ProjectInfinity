package net.lerariemann.infinity.util.loading;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.lerariemann.infinity.access.GameRendererAccess;
import net.lerariemann.infinity.iridescence.Iridescence;
import net.lerariemann.infinity.util.InfinityMethods;
import net.lerariemann.infinity.util.core.CommonIO;
import net.minecraft.client.MinecraftClient;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.resource.ResourcePackManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Environment(EnvType.CLIENT)
public interface ShaderLoader {
    String FILENAME = "current.json";
    static Path shaderDir(MinecraftClient client) {
        return client.getResourcePackDir().resolve("infinity/assets/infinity/shaders");
    }

    static void reloadShaders(MinecraftClient client, boolean bl) {
        reloadShaders(client, bl, Iridescence.shouldApplyShader(client.player));
    }

    static void reloadShaders(MinecraftClient client, boolean bl, boolean iridescence) {
        if (client.world == null) return;
        if (iridescence) {
            ((GameRendererAccess)(client.gameRenderer)).infinity$loadPP(InfinityMethods.getId("shaders/post/iridescence.json"));
            return;
        }
        try {
            load(client);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if(bl && shaderDir(client).resolve(FILENAME).toFile().exists()) {
            ((GameRendererAccess)(client.gameRenderer)).infinity$loadPP(InfinityMethods.getId("shaders/" + FILENAME));
            return;
        }
        client.gameRenderer.disablePostProcessor();
    }

    static void load(MinecraftClient client) throws IOException {
        ResourcePackManager m = client.getResourcePackManager();
        Path path = client.getResourcePackDir().resolve("infinity");
        String name = "file/" + path.getFileName().toString();
        Files.createDirectories(path.resolve("assets/infinity/shaders"));
        if (!path.resolve("pack.mcmeta").toFile().exists()) CommonIO.write(packMcmeta(), path.toString(), "pack.mcmeta");
        m.scanPacks();
        m.enable(name);
    }

    static NbtCompound packMcmeta() {
        NbtCompound res = new NbtCompound();
        NbtCompound pack = new NbtCompound();
        pack.putInt("pack_format", 15);
        pack.putString("description", "Shader container");
        res.put("pack", pack);
        return res;
    }
}
