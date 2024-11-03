package net.lerariemann.infinity.mixin.options;

import net.lerariemann.infinity.access.InfinityOptionsAccess;
import net.lerariemann.infinity.access.Timebombable;
import net.lerariemann.infinity.options.InfinityOptions;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldGenerationProgressListener;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.random.RandomSequencesState;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.GameRules;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.level.ServerWorldProperties;
import net.minecraft.world.level.storage.LevelStorage;
import net.minecraft.world.spawner.Spawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin extends World implements StructureWorldAccess, InfinityOptionsAccess, Timebombable {
    @Unique
    public InfinityOptions infinityoptions;
    @Unique
    public int timebombed;

    protected ServerWorldMixin(MutableWorldProperties properties, RegistryKey<World> registryRef, DynamicRegistryManager registryManager, RegistryEntry<DimensionType> dimensionEntry, Supplier<Profiler> profiler, boolean isClient, boolean debugWorld, long biomeAccess, int maxChainedNeighborUpdates) {
        super(properties, registryRef, registryManager, dimensionEntry, profiler, isClient, debugWorld, biomeAccess, maxChainedNeighborUpdates);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void injected(MinecraftServer server, Executor workerExecutor, LevelStorage.Session session, ServerWorldProperties properties, RegistryKey<World> worldKey, DimensionOptions dimensionOptions, WorldGenerationProgressListener worldGenerationProgressListener, boolean debugWorld, long seed, List<Spawner> spawners, boolean shouldTickTime, RandomSequencesState randomSequencesState, CallbackInfo ci) {
        infinityoptions = InfinityOptions.generate(server, worldKey);
        DimensionType t = getDimension();
        ((InfinityOptionsAccess)(Object)t).infinity$setOptions(infinityoptions);
        timebombed = 0;
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void injected2(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        if (getRegistryKey().getValue().toString().contains("infinity") && timebombed > 0) timebombed++;
    }

    @Redirect(method="tickWeather", at=@At(value="INVOKE", target="Lnet/minecraft/world/GameRules;getBoolean(Lnet/minecraft/world/GameRules$Key;)Z"))
    private boolean injected3(GameRules instance, GameRules.Key<GameRules.BooleanRule> rule) {
        return instance.getBoolean(rule) && !getRegistryKey().getValue().toString().contains("infinity");
    }

    @Override
    public void infinity$timebomb(int i) {
        if(getRegistryKey().getValue().toString().contains("infinity")) timebombed = i;
    }

    @Override
    public int infinity$isTimebombed() {
        return timebombed;
    }

    @Override
    public InfinityOptions infinity$getOptions() {
        return infinityoptions;
    }
    @Override
    public void infinity$setOptions(InfinityOptions options) {
        infinityoptions = options;
    }
}
