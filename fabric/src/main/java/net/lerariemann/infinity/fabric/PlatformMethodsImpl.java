package net.lerariemann.infinity.fabric;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;

public class PlatformMethodsImpl {
    public static void sendServerPlayerEntity(ServerPlayerEntity entity, CustomPayload payload) {
        net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(entity, payload);
    }

    public static PacketByteBuf createPacketByteBufs() {
        return PacketByteBufs.create();
    }

}
