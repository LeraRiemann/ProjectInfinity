package net.lerariemann.infinity.mixin.options;

import net.lerariemann.infinity.access.InfinityOptionsAccess;
import net.lerariemann.infinity.options.InfinityOptions;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.function.Supplier;

@Mixin(ClientWorld.class)
public abstract class ClientWorldMixin extends World implements InfinityOptionsAccess {
    @Unique
    public InfinityOptions infinityoptions;

    protected ClientWorldMixin(MutableWorldProperties properties, RegistryKey<World> registryRef, DynamicRegistryManager registryManager, RegistryEntry<DimensionType> dimensionEntry, Supplier<Profiler> profiler, boolean isClient, boolean debugWorld, long biomeAccess, int maxChainedNeighborUpdates) {
        super(properties, registryRef, registryManager, dimensionEntry, isClient, debugWorld, biomeAccess, maxChainedNeighborUpdates);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void injected(ClientPlayNetworkHandler networkHandler, ClientWorld.Properties properties, RegistryKey registryRef, RegistryEntry dimensionType, int loadDistance, int simulationDistance, WorldRenderer worldRenderer, boolean debugWorld, long seed, int seaLevel, CallbackInfo ci) {
        DimensionType t = getDimension();
        infinityoptions = ((InfinityOptionsAccess)MinecraftClient.getInstance()).projectInfinity$getInfinityOptions();
        ((InfinityOptionsAccess)(Object)t).projectInfinity$setInfinityOptions(infinityoptions);
    }

    @Override
    public InfinityOptions projectInfinity$getInfinityOptions() {
        return infinityoptions;
    }
    @Override
    public void projectInfinity$setInfinityOptions(InfinityOptions options) {
        infinityoptions = options;
    }
}
