package net.lerariemann.infinity.mixin.fixes;

import com.mojang.serialization.Codec;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ChunkErrorHandler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.poi.PointOfInterestSet;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.minecraft.world.poi.PointOfInterestType;
import net.minecraft.world.storage.ChunkPosKeyedStorage;
import net.minecraft.world.storage.SerializingRegionBasedStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Function;

@Mixin(PointOfInterestStorage.class)
public abstract class PointOfInterestStorageMixin extends SerializingRegionBasedStorage<PointOfInterestSet> {
    public PointOfInterestStorageMixin(ChunkPosKeyedStorage storageAccess, Function<Runnable, Codec<PointOfInterestSet>> codecFactory, Function<Runnable, PointOfInterestSet> factory, DynamicRegistryManager registryManager, ChunkErrorHandler errorHandler, HeightLimitView world) {
        super(storageAccess, codecFactory, factory, registryManager, errorHandler, world);
    }

    /* There's null-unsafe inner-minecraft code that can crash if we don't do this */
    @Inject(method = "add", at = @At("HEAD"), cancellable = true)
    void inj(BlockPos pos, RegistryEntry<PointOfInterestType> type, CallbackInfo ci) {
        if (world.isOutOfHeightLimit(pos.getY())) ci.cancel();
    }
}
