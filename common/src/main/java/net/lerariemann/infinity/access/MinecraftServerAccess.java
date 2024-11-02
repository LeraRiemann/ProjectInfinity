package net.lerariemann.infinity.access;

import net.lerariemann.infinity.util.RandomProvider;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionOptions;

public interface MinecraftServerAccess {

    void projectInfinity$addWorld(RegistryKey<World> key, DimensionOptions options);

    boolean projectInfinity$hasToAdd(RegistryKey<World> key);

    boolean projectInfinity$needsInvocation();
    void projectInfinity$onInvocation();

    RandomProvider projectInfinity$getDimensionProvider();
    void projectInfinity$setDimensionProvider();
}
