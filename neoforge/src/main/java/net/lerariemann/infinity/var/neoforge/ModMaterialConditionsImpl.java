package net.lerariemann.infinity.var.neoforge;

import net.fabricmc.fabric.mixin.registry.sync.BaseMappedRegistryAccessor;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class ModMaterialConditionsImpl {
    public static void registerConditions() {
        ((BaseMappedRegistryAccessor) Registries.MATERIAL_CONDITION).invokeUnfreeze();
        Registry.register(Registries.MATERIAL_CONDITION, "infinity:linear", net.lerariemann.infinity.var.ModMaterialConditions.LinearCondition.CODEC.codec());
        Registry.register(Registries.MATERIAL_CONDITION, "infinity:checkerboard", net.lerariemann.infinity.var.ModMaterialConditions.CheckerboardCondition.CODEC.codec());
        Registry.register(Registries.MATERIAL_CONDITION, "infinity:text", net.lerariemann.infinity.var.ModMaterialConditions.TextCondition.CODEC.codec());
        Registries.MATERIAL_CONDITION.freeze();
    }
}
