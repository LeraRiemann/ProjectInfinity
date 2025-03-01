package net.lerariemann.infinity.mixin.options;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.lerariemann.infinity.access.InfinityOptionsAccess;
import net.lerariemann.infinity.access.Timebombable;
import net.lerariemann.infinity.options.InfinityOptions;
import net.lerariemann.infinity.util.InfinityMethods;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldGenerationProgressListener;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.RandomSequencesState;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.level.ServerWorldProperties;
import net.minecraft.world.level.storage.LevelStorage;
import net.minecraft.world.spawner.Spawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin extends World implements StructureWorldAccess, InfinityOptionsAccess, Timebombable {
    @Unique
    public InfinityOptions infinity$options;
    @Unique
    public int infinity$timebombProgress;

    protected ServerWorldMixin(MutableWorldProperties properties, RegistryKey<World> registryRef, DynamicRegistryManager registryManager, RegistryEntry<DimensionType> dimensionEntry, Supplier<Profiler> profiler, boolean isClient, boolean debugWorld, long biomeAccess, int maxChainedNeighborUpdates) {
        super(properties, registryRef, registryManager, dimensionEntry, profiler, isClient, debugWorld, biomeAccess, maxChainedNeighborUpdates);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void injected(MinecraftServer server, Executor workerExecutor, LevelStorage.Session session, ServerWorldProperties properties, RegistryKey<World> worldKey, DimensionOptions dimensionOptions, WorldGenerationProgressListener worldGenerationProgressListener, boolean debugWorld, long seed, List<Spawner> spawners, boolean shouldTickTime, RandomSequencesState randomSequencesState, CallbackInfo ci) {
        infinity$options = InfinityOptions.generate(server, worldKey.getValue());
        DimensionType t = getDimension();
        ((InfinityOptionsAccess)(Object)t).infinity$setOptions(infinity$options);
        infinity$timebombProgress = 0;
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void injected2(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        if (InfinityMethods.isInfinity(getRegistryKey()) && infinity$timebombProgress > 0) infinity$timebombProgress++;
    }

    /** InfDims share the weather with the Overworld, but if we don't do this, they will also tick it, causing the weather cycle to be very rapid */
    @ModifyExpressionValue(method="tickWeather", at=@At(value="INVOKE", target="Lnet/minecraft/world/GameRules;getBoolean(Lnet/minecraft/world/GameRules$Key;)Z"))
    private boolean mev(boolean original) {
        return original && !InfinityMethods.isInfinity(getRegistryKey());
    }

    @ModifyExpressionValue(method = "tickChunk", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/ChunkSection;hasRandomTicks()Z"))
    boolean mev2(boolean original, @Local ChunkSection cs) {
        return original || (infinity$options.isHaunted() && !cs.isEmpty());
    }

    @Inject(method = "tickChunk", at = @At(value = "INVOKE", target = "Lnet/minecraft/fluid/FluidState;hasRandomTicks()Z"))
    void inj(WorldChunk chunk, int randomTickSpeed, CallbackInfo ci, @Local BlockState bs, @Local BlockPos blockPos2, @Local FluidState fs) {
        if (infinity$options.isHaunted() && random.nextInt(200) < (int)(Math.floor(getMoonSize() * 4))) {
            if (bs.contains(Properties.NOTE)) {
                setBlockState(blockPos2, bs.with(Properties.NOTE, random.nextInt(24)));
            }
        }
    }

    @Override
    public void infinity$timebomb() {
        if(InfinityMethods.isInfinity(getRegistryKey())) {
            infinity$timebombProgress = 1;
        }
    }
    @Override
    public boolean infinity$tryRestore() {
        if(InfinityMethods.isInfinity(getRegistryKey()) && (infinity$timebombProgress > Timebombable.getCooldownTicks())) {
            infinity$timebombProgress = 0;
            return true;
        }
        return false;
    }

    @Override
    public boolean infinity$isTimebombed() {
        return infinity$timebombProgress > 0;
    }
    @Override
    public int infinity$getTimebombProgress() {
        return infinity$timebombProgress;
    }

    @Override
    public InfinityOptions infinity$getOptions() {
        return InfinityOptions.nullSafe(infinity$options);
    }
    @Override
    public void infinity$setOptions(InfinityOptions options) {
        infinity$options = options;
    }
}
