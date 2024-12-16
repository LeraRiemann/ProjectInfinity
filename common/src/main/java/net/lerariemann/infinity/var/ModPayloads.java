package net.lerariemann.infinity.var;

import dev.architectury.platform.Platform;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.lerariemann.infinity.loading.DimensionGrabber;
import net.lerariemann.infinity.util.PlatformMethods;
import net.lerariemann.infinity.access.InfinityOptionsAccess;
import net.lerariemann.infinity.access.WorldRendererAccess;
import net.lerariemann.infinity.options.InfinityOptions;
import net.lerariemann.infinity.util.ShaderLoader;
import net.lerariemann.infinity.util.CommonIO;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static net.lerariemann.infinity.InfinityMod.getId;

public class ModPayloads {

    public static final Identifier WORLD_ADD = getId("reload_worlds");
    public static final Identifier SHADER_RELOAD = getId("reload_shader");
    public static final Identifier STARS_RELOAD = getId("reload_stars");

    public static PacketByteBuf buildPacket(ServerWorld destination) {
        PacketByteBuf buf = PlatformMethods.createPacketByteBufs();
        if (destination == null) buf.writeNbt(new NbtCompound());
        else buf.writeNbt(((InfinityOptionsAccess)(destination)).infinity$getOptions().data());
        return buf;
    }

    public static void receiveShader(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        InfinityOptions options = new InfinityOptions(buf.readNbt());
        ((InfinityOptionsAccess)client).infinity$setOptions(options);
        NbtCompound shader = options.getShader();
        boolean bl = shader.isEmpty();
        if (bl) client.execute(() -> ShaderLoader.reloadShaders(client, false));
        else {
            client.execute(() -> {
                CommonIO.write(shader, ShaderLoader.shaderDir(client), ShaderLoader.FILENAME);
                ShaderLoader.reloadShaders(client, true);
                if (!resourcesReloaded) {
                    client.reloadResources();
                    resourcesReloaded = true;
                }
            });
        }
    }

    public static boolean resourcesReloaded = Path.of(Platform.getGameFolder() + "/resourcepacks/infinity/assets/infinity/shaders").toFile().exists();

    public static void receiveStars(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        ((WorldRendererAccess)(client.worldRenderer)).infinity$setNeedsStars(true);
    }

    public static void registerPayloadsClient() {
        ClientPlayNetworking.registerGlobalReceiver(ModPayloads.WORLD_ADD, (client, handler, buf, responseSender) -> {
            Identifier id = buf.readIdentifier();
            NbtCompound optiondata = buf.readNbt();
            int i = buf.readInt();
            List<Identifier> biomeids = new ArrayList<>();
            List<NbtCompound> biomes = new ArrayList<>();
            for (int j = 0; j < i; j++) {
                biomeids.add(buf.readIdentifier());
                biomes.add(buf.readNbt());
            }
            client.execute(() -> (new DimensionGrabber(client.getNetworkHandler().getRegistryManager())).grab_for_client(id, optiondata, biomeids, biomes));
        });
        ClientPlayNetworking.registerGlobalReceiver(ModPayloads.SHADER_RELOAD, ModPayloads::receiveShader);
        ClientPlayNetworking.registerGlobalReceiver(ModPayloads.STARS_RELOAD, ModPayloads::receiveStars);
    }
}