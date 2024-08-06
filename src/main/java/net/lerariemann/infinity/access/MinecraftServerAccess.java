package net.lerariemann.infinity.access;

import net.lerariemann.infinity.dimensions.RandomProvider;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionOptions;

public interface MinecraftServerAccess {

    void addWorld(RegistryKey<World> key, DimensionOptions options);

    boolean hasToAdd(RegistryKey<World> key);

    boolean needsInvocation();
    void onInvocation();

    RandomProvider getDimensionProvider();
    void setDimensionProvider();
}
