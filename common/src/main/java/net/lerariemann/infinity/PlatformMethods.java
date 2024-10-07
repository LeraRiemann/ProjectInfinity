package net.lerariemann.infinity;

import com.google.common.collect.ImmutableSet;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.poi.PointOfInterestType;

public class PlatformMethods {
    @ExpectPlatform
    public static void sendServerPlayerEntity(ServerPlayerEntity entity, CustomPayload payload) {
        throw new AssertionError();
    }
    @ExpectPlatform
    public static PacketByteBuf createPacketByteBufs() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static <E> PointOfInterestType registerPoi(Identifier id, int i, int i1, ImmutableSet<E> es) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void onWorldLoad(Object mixin, ServerWorld world) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void unfreeze(Registry<?> registry) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void unfreeze(RegistryKey<?> registry) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void freeze(Registry<?> registry) {
        throw new AssertionError();
    }
}
