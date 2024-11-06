package net.lerariemann.infinity.options;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.lerariemann.infinity.PlatformMethods;
import net.lerariemann.infinity.access.InfinityOptionsAccess;
import net.lerariemann.infinity.access.WorldRendererAccess;
import net.lerariemann.infinity.util.CommonIO;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.world.ServerWorld;

public class PacketTransiever {
    public static PacketByteBuf buildPacket(ServerWorld destination) {
        PacketByteBuf buf = PlatformMethods.createPacketByteBufs();
        buf.writeNbt(((InfinityOptionsAccess)(destination)).infinity$getOptions().data());
        return buf;
    }

    public static void receive(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        InfinityOptions options = new InfinityOptions(buf.readNbt());
        ((InfinityOptionsAccess)client).infinity$setOptions(options);
        NbtCompound shader = options.getShader();
        boolean bl = shader.isEmpty();
        if (bl) client.execute(() -> ShaderLoader.reloadShaders(client, false));
        else {
            client.execute(() -> {
                CommonIO.write(shader, ShaderLoader.shaderDir(client), ShaderLoader.FILENAME);
                ShaderLoader.reloadShaders(client, true);
            });
        }
    }

    public static void receiveStars(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        ((WorldRendererAccess)(client.worldRenderer)).projectInfinity$setNeedsStars(true);
    }
}
