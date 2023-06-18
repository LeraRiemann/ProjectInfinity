package me.basiqueevangelist.dynreg.event;

import me.basiqueevangelist.dynreg.access.ExtendedRegistry;
import net.fabricmc.fabric.api.event.Event;
import net.minecraft.registry.Registry;

public interface RegistryFrozenCallback<T> {
    void onRegistryFrozen();

    @SuppressWarnings("unchecked")
    static <T> Event<RegistryFrozenCallback<T>> event(Registry<T> registry) {
        return ((ExtendedRegistry<T>) registry).dynreg$getRegistryFrozenEvent();
    }
}
