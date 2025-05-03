package net.lerariemann.infinity.util.loading;

import com.google.common.util.concurrent.AtomicDouble;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.lerariemann.infinity.access.GameRendererAccess;
import net.lerariemann.infinity.registry.var.ModPayloads;
import net.lerariemann.infinity.util.InfinityMethods;
import net.lerariemann.infinity.util.core.CommonIO;
import net.minecraft.client.MinecraftClient;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.resource.ResourcePackManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;

@Environment(EnvType.CLIENT)
public interface ShaderLoader {
    String FILENAME = "current.json";
    AtomicInteger iridLevel = new AtomicInteger(-1);
    AtomicDouble iridProgress = new AtomicDouble(0.0);

    static Path shaderDir(MinecraftClient client) {
        return client.getResourcePackDir().resolve("infinity/assets/infinity/shaders");
    }

    static void reloadShaders(MinecraftClient client, NbtCompound worldShader, boolean isIridescenceShaderPresent) {
        if (worldShader.isEmpty()) {
            reloadShaders(client, false, isIridescenceShaderPresent);
        }
        else {
            CommonIO.write(worldShader, shaderDir(client), ShaderLoader.FILENAME);
            reloadShaders(client, true, isIridescenceShaderPresent);
            if (!ModPayloads.resourcesReloaded) {
                client.reloadResources();
                ModPayloads.resourcesReloaded = true;
            }
        }
    }

    static void reloadShaders(MinecraftClient client, boolean isWorldShaderPresent, boolean isIridescenceShaderPresent) {
        if (client.player == null) return;
        if (isIridescenceShaderPresent) {
            ((GameRendererAccess)(client.gameRenderer)).infinity$loadPP(InfinityMethods.getId("shaders/post/iridescence.json"));
            return;
        }
        iridLevel.set(-1);
        iridProgress.set(0.0);
        try {
            updateWorldShaderFromDisk(client);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if(isWorldShaderPresent && shaderDir(client).resolve(FILENAME).toFile().exists()) {
            ((GameRendererAccess)(client.gameRenderer)).infinity$loadPP(InfinityMethods.getId("shaders/" + FILENAME));
            return;
        }
        client.gameRenderer.disablePostProcessor();
    }

    static void updateWorldShaderFromDisk(MinecraftClient client) throws IOException {
        ResourcePackManager m = client.getResourcePackManager();
        Path resourcepackPath = client.getResourcePackDir().resolve("infinity");
        Files.createDirectories(resourcepackPath.resolve("assets/infinity/shaders"));
        if (!resourcepackPath.resolve("pack.mcmeta").toFile().exists())
            CommonIO.write(packMcmeta(), resourcepackPath.toString(), "pack.mcmeta");
        m.scanPacks();
        m.enable("file/" + resourcepackPath.getFileName().toString());
    }

    static NbtCompound packMcmeta() {
        NbtCompound res = new NbtCompound();
        NbtCompound pack = new NbtCompound();
        pack.putInt("pack_format", 34);
        pack.putString("description", "Shader container");
        res.put("pack", pack);
        return res;
    }
}
