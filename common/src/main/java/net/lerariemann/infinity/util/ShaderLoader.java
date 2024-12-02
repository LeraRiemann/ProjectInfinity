package net.lerariemann.infinity.util;

import dev.architectury.platform.Platform;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.lerariemann.infinity.access.GameRendererAccess;
import net.lerariemann.infinity.iridescence.Iridescence;
import net.minecraft.client.MinecraftClient;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static net.lerariemann.infinity.var.ModPayloads.resourcesReloaded;

@Environment(EnvType.CLIENT)
public interface ShaderLoader {
    String FILENAME = "current.json";
    static Path shaderDir(MinecraftClient client) {
        return client.getResourcePackDir().resolve("infinity/assets/infinity/shaders");
    }

    static void reloadShaders(MinecraftClient client, boolean bl) {
        try {
            load(client);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Identifier iridShader = Iridescence.shouldApplyShader(client.player);
        if (iridShader != null) {
            ((GameRendererAccess)(client.gameRenderer)).infinity$loadPP(iridShader);
            return;
        }
        if(bl && shaderDir(client).resolve(FILENAME).toFile().exists()) {
            ((GameRendererAccess)(client.gameRenderer)).infinity$loadPP(InfinityMethods.getId("shaders/" + FILENAME));
            return;
        }
        client.gameRenderer.disablePostProcessor();
        if (!resourcesReloaded) {
            client.reloadResources();
            resourcesReloaded = true;
        }
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
