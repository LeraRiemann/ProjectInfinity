package me.basiqueevangelist.dynreg.util;

import me.basiqueevangelist.dynreg.access.ExtendedRegistry;
import net.minecraft.registry.Registry;

public final class RegistryUtils {
    private RegistryUtils() {

    }
    public static void unfreeze(Registry<?> registry) {
        ((ExtendedRegistry<?>) registry).dynreg$unfreeze();
    }
}
