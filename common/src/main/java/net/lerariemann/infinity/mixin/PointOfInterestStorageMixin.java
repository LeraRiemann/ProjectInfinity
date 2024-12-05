package net.lerariemann.infinity.mixin;

import com.mojang.datafixers.DataFixer;
import net.minecraft.datafixer.DataFixTypes;
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
import net.minecraft.world.storage.StorageKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.file.Path;

@Mixin(PointOfInterestStorage.class)
public class PointOfInterestStorageMixin extends SerializingRegionBasedStorage<PointOfInterestSet, PointOfInterestSet.Serialized> {
    public PointOfInterestStorageMixin(StorageKey storageKey, Path directory, DataFixer dataFixer, boolean dsync, DynamicRegistryManager registryManager, ChunkErrorHandler errorHandler, HeightLimitView world) {
        super(new ChunkPosKeyedStorage(storageKey, directory, dataFixer, dsync, DataFixTypes.POI_CHUNK), PointOfInterestSet.Serialized.CODEC, PointOfInterestSet::toSerialized, PointOfInterestSet.Serialized::toPointOfInterestSet, PointOfInterestSet::new, registryManager, errorHandler, world);
    }

    @Inject(method = "add", at = @At("HEAD"), cancellable = true)
    void inj(BlockPos pos, RegistryEntry<PointOfInterestType> type, CallbackInfo ci) {
        if (world.isOutOfHeightLimit(pos.getY())) ci.cancel();
    }
}
