package net.lerariemann.infinity.neoforge;

import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.mixin.registry.sync.BaseMappedRegistryAccessor;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.LevelEvent;
/**
 * See {@link net.lerariemann.infinity.PlatformMethods} for usages.
 */
@SuppressWarnings("unused")
public class PlatformMethodsImpl {
    public static boolean isModLoaded(String modID) {
       return ModList.get().isLoaded(modID);
    }

    public static PacketByteBuf createPacketByteBufs() {
        return new PacketByteBuf(Unpooled.buffer());
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
