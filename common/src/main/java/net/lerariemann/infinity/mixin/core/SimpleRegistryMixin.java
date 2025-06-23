package net.lerariemann.infinity.mixin.core;

import com.mojang.serialization.Lifecycle;
import net.lerariemann.infinity.access.RegistryAccess;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.TagKey;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Needed for runtime dimension generation to inject generated content into the game.
 * Based on DynReg by BasiqueEvangelist.
 * */
@Mixin(SimpleRegistry.class)
public abstract class SimpleRegistryMixin<T> implements RegistryAccess, Registry<T> {
    @Shadow
    private boolean frozen;
    @Shadow
    @Nullable
    private Map<T, RegistryEntry.Reference<T>> intrusiveValueToEntry;
    @Mutable
    @Shadow @Final private Map<TagKey<T>, RegistryEntryList.Named<T>> tags;
    @Shadow private SimpleRegistry.TagLookup<T> tagLookup;
    @Unique
    private boolean infinity$intrusive;

    @Inject(method = "<init>(Lnet/minecraft/registry/RegistryKey;Lcom/mojang/serialization/Lifecycle;Z)V", at = @At("TAIL"))
    private void saveIntrusiveness(RegistryKey<?> key, Lifecycle lifecycle, boolean intrusive, CallbackInfo ci) {
        infinity$intrusive = intrusive;
    }

    @Override
    public void infinity$unfreeze() {
        frozen = false;
        tagLookup = SimpleRegistry.TagLookup.ofUnbound();
        if (infinity$intrusive) this.intrusiveValueToEntry = new IdentityHashMap<>();
    }
}
