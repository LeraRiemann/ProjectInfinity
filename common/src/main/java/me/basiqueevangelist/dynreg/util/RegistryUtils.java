package me.basiqueevangelist.dynreg.util;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.registry.Registry;

public final class RegistryUtils {
    private RegistryUtils() {

    }
    @ExpectPlatform
    public static void unfreeze(Registry<?> registry) {
        throw new AssertionError();
    }
}
