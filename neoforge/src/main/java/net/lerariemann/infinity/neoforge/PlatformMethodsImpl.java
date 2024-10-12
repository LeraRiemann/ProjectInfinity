package net.lerariemann.infinity.neoforge;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.mixin.registry.sync.BaseMappedRegistryAccessor;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.LevelEvent;

public class PlatformMethodsImpl {
    public static void sendServerPlayerEntity(ServerPlayerEntity entity, CustomPayload payload) {
        net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(entity, payload);
    }

    public static PacketByteBuf createPacketByteBufs() {
        return PacketByteBufs.create();
    }

    public static void onWorldLoad(Object mixin, ServerWorld world) {
        MinecraftServer server = world.getServer();
        server.forgeGetWorldMap().put(world.getRegistryKey(),world);
        server.markWorldsDirty();
        NeoForge.EVENT_BUS.post(new LevelEvent.Load(world));


    }

    public static void unfreeze(Registry<?> registry) {
        ((BaseMappedRegistryAccessor) registry).invokeUnfreeze();

    }

    public static void unfreeze(RegistryKey<?> registry) {
        ((BaseMappedRegistryAccessor) registry.getRegistryRef()).invokeUnfreeze();

    }


    public static void freeze(Registry<?> registry) {
//        registry.freeze();

    }

}
