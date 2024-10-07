package net.lerariemann.infinity.var.neoforge;

import net.fabricmc.fabric.mixin.registry.sync.BaseMappedRegistryAccessor;
import net.lerariemann.infinity.InfinityMod;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.dynamic.CodecHolder;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class ModDensityFunctionTypesImpl {

    public static <T extends DensityFunction> void register(String name, CodecHolder<T> holder) {
        ((BaseMappedRegistryAccessor) Registries.DENSITY_FUNCTION_TYPE).invokeUnfreeze();
        Registry.register(Registries.DENSITY_FUNCTION_TYPE, InfinityMod.MOD_ID + ":" + name, holder.codec());
        Registries.DENSITY_FUNCTION_TYPE.freeze();

    }
}
