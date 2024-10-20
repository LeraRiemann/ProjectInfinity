package net.lerariemann.infinity.fabric;

import me.basiqueevangelist.dynreg.util.RegistryUtils;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
/**
 * See {@link net.lerariemann.infinity.PlatformMethods} for usages.
 */
@SuppressWarnings("unused")
public class PlatformMethodsImpl {
    public static boolean isModLoaded(String modID) {
        return FabricLoader.getInstance().isModLoaded(modID);
    }

    public static PacketByteBuf createPacketByteBufs() {
        return PacketByteBufs.create();
    }

    public static void onWorldLoad(Object mixin, ServerWorld world) {
        ServerWorldEvents.LOAD.invoker().onWorldLoad((MinecraftServer) mixin, world);
    }

    public static void unfreeze(Registry<?> registry) {
        RegistryUtils.unfreeze(registry);

    }

    public static void unfreeze(RegistryKey<?> registry) {
//        RegistryUtils.unfreeze(registry.getRegistryRef().getRegistry());
    }


    public static void freeze(Registry<?> registry) {
        registry.freeze();

    }

}
