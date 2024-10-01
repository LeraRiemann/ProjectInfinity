package net.lerariemann.infinity.mixin.fabric;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.lerariemann.infinity.mixin.MinecraftServerMixin;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;

public class MinecraftServerMixinImpl {
    public void onWorldLoad(Object mixin, ServerWorld world) {
        ServerWorldEvents.LOAD.invoker().onWorldLoad((MinecraftServer) mixin, world);
    }
}
