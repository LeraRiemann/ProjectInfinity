package net.lerariemann.infinity.fabric;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

public class PlatformMethodsImpl {
    public static void sendServerPlayerEntity(ServerPlayerEntity entity, CustomPayload payload) {
        net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(entity, payload);
    }

    public static PacketByteBuf createPacketByteBufs() {
        return PacketByteBufs.create();
    }

    public static void onWorldLoad(Object mixin, ServerWorld world) {
        ServerWorldEvents.LOAD.invoker().onWorldLoad((MinecraftServer) mixin, world);
    }

}
