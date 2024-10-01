package me.basiqueevangelist.dynreg.util.neoforge;

import me.basiqueevangelist.dynreg.access.ExtendedRegistry;
import net.minecraft.registry.Registry;

public final class RegistryUtilsImpl {
    private RegistryUtilsImpl() {

    }
    public static void unfreeze(Registry<?> registry) {
        ((ExtendedRegistry<?>) registry).dynreg$unfreeze();
    }
}
