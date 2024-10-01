package me.basiqueevangelist.dynreg.mixin.neoforge;

import com.mojang.serialization.Lifecycle;
import me.basiqueevangelist.dynreg.access.ExtendedRegistry;
import me.basiqueevangelist.dynreg.event.RegistryEntryDeletedCallback;
import me.basiqueevangelist.dynreg.event.RegistryFrozenCallback;
import me.basiqueevangelist.dynreg.util.neoforge.StackTracingMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;

@Mixin(value = SimpleRegistry.class)
public abstract class SimpleRegistryMixin<T> implements ExtendedRegistry<T>, Registry<T> {
    @Shadow private boolean frozen;
    @Shadow
    @Nullable
    private Map<T, RegistryEntry.Reference<T>> intrusiveValueToEntry;

    @Shadow
    public abstract Optional<RegistryEntry.Reference<T>> getEntry(RegistryKey<T> key);

    @Mutable
    @Shadow
    @Final
    private Map<Identifier, RegistryEntry.Reference<T>> idToEntry;

    @Unique
    @SuppressWarnings("unchecked") private final Event<RegistryEntryDeletedCallback<T>> dynreg$entryDeletedEvent = EventFactory.createArrayBacked(RegistryEntryDeletedCallback.class, callbacks -> (rawId, entry) -> {
        for (var callback : callbacks) {
            callback.onEntryDeleted(rawId, entry);
        }

        if (entry.value() instanceof RegistryEntryDeletedCallback<?> callback)
            ((RegistryEntryDeletedCallback<T>) callback).onEntryDeleted(rawId, entry);
    });
    @Unique
    private final Event<RegistryFrozenCallback<T>> dynreg$registryFrozenEvent = EventFactory.createArrayBacked(RegistryFrozenCallback.class, callbacks -> () -> {
        for (var callback : callbacks) {
            callback.onRegistryFrozen();
        }
    });
    @Unique
    private final IntList dynreg$freeIds = new IntArrayList();
    @Unique
    private boolean dynreg$intrusive;

    @Override
    public Event<RegistryEntryDeletedCallback<T>> dynreg$getEntryDeletedEvent() {
        return dynreg$entryDeletedEvent;
    }

    @Override
    public Event<RegistryFrozenCallback<T>> dynreg$getRegistryFrozenEvent() {
        return dynreg$registryFrozenEvent;
    }

    @Inject(method = "<init>(Lnet/minecraft/registry/RegistryKey;Lcom/mojang/serialization/Lifecycle;Z)V", at = @At("TAIL"))
    private void saveIntrusiveness(RegistryKey<?> key, Lifecycle lifecycle, boolean intrusive, CallbackInfo ci) {
        dynreg$intrusive = intrusive;
    }

    @Override
    public void dynreg$unfreeze() {
        frozen = false;
        if (dynreg$intrusive)
            this.intrusiveValueToEntry = new IdentityHashMap<>();
    }

    @Inject(method = "freeze", at = @At("HEAD"))
    private void onFreeze(CallbackInfoReturnable<Registry<T>> cir) {
        dynreg$registryFrozenEvent.invoker().onRegistryFrozen();
    }

    @Override
    public void dynreg$installStackTracingMap() {
        this.idToEntry = new StackTracingMap<>(this.idToEntry);
    }
}
