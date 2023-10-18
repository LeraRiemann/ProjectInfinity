package net.lerariemann.infinity.mixin;

import com.google.common.collect.ImmutableList;
import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.access.MinecraftServerAccess;
import net.lerariemann.infinity.dimensions.RandomProvider;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerTask;
import net.minecraft.server.WorldGenerationProgressListenerFactory;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.WorldSavePath;
import net.minecraft.world.SaveProperties;
import net.minecraft.world.World;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.border.WorldBorderListener;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.level.ServerWorldProperties;
import net.minecraft.world.level.UnmodifiableLevelProperties;
import net.minecraft.world.level.storage.LevelStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin implements MinecraftServerAccess {
    @Final @Shadow
    private Map<RegistryKey<World>, ServerWorld> worlds;
    @Final @Shadow
    private Executor workerExecutor;
    @Final @Shadow
    protected LevelStorage.Session session;
    @Final @Shadow
    protected SaveProperties saveProperties;
    @Final @Shadow
    private WorldGenerationProgressListenerFactory worldGenerationProgressListenerFactory;
    @Shadow
    public ServerWorld getWorld(RegistryKey<World> key) {
        return null;
    }
    @Shadow
    protected ServerTask createTask(Runnable runnable) {
        return null;
    }
    @Shadow
    public Path getSavePath(WorldSavePath worldSavePath) {
        return null;
    }

    public Map<RegistryKey<World>, ServerWorld> worldsToAdd;
    public RandomProvider dimensionProvider;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void injected(CallbackInfo info) {
        worldsToAdd = new HashMap<>();
        dimensionProvider = new RandomProvider("config/" + InfinityMod.MOD_ID + "/",
                getSavePath(WorldSavePath.DATAPACKS).toString() + "/" + InfinityMod.MOD_ID);
    }

    @Override
    public RandomProvider getDimensionProvider() {
        return dimensionProvider;
    }

    @Override
    public void addWorld(RegistryKey<World> key, DimensionOptions options) {
        ServerWorldProperties serverWorldProperties = saveProperties.getMainWorldProperties();
        UnmodifiableLevelProperties unmodifiableLevelProperties = new UnmodifiableLevelProperties(saveProperties, serverWorldProperties);
        ServerWorld world = new ServerWorld(((MinecraftServer) (Object) this), workerExecutor, session, unmodifiableLevelProperties,
                key, options, worldGenerationProgressListenerFactory.create(11), saveProperties.isDebugWorld(),
                BiomeAccess.hashSeed(saveProperties.getGeneratorOptions().getSeed()), ImmutableList.of(), false, getWorld(World.OVERWORLD).getRandomSequences());
        getWorld(World.OVERWORLD).getWorldBorder().addListener(new WorldBorderListener.WorldBorderSyncer(world.getWorldBorder()));
        worldsToAdd.put(key, world);
        ((MinecraftServer) (Object) this).send(createTask(() -> {
            worlds.put(key, world);
            worldsToAdd.clear();
        }));
    }

    @Override
    public boolean hasToAdd(RegistryKey<World> key) {
        return (worldsToAdd.containsKey(key));
    }
}
