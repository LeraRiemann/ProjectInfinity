package net.lerariemann.infinity.access;

import net.lerariemann.infinity.util.RandomProvider;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionOptions;

public interface MinecraftServerAccess {

    void infinity$addWorld(RegistryKey<World> key, DimensionOptions options);

    boolean infinity$hasToAdd(RegistryKey<World> key);

    boolean infinity$needsInvocation();
    void infinity$onInvocation();

    RandomProvider infinity$getDimensionProvider();
    void infinity$setDimensionProvider();
}
