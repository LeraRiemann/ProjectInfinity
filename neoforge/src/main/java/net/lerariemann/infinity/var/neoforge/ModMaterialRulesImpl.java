package net.lerariemann.infinity.var.neoforge;

import net.fabricmc.fabric.mixin.registry.sync.BaseMappedRegistryAccessor;
import net.lerariemann.infinity.InfinityMod;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.dynamic.CodecHolder;
import net.minecraft.world.gen.surfacebuilder.MaterialRules;

public class ModMaterialRulesImpl {
    public static <T extends CodecHolder<? extends MaterialRules.MaterialRule>> void register(String name, T holder) {
        ((BaseMappedRegistryAccessor) Registries.MATERIAL_RULE).invokeUnfreeze();
        Registry.register(Registries.MATERIAL_RULE, InfinityMod.MOD_ID + ":" + name, holder.codec());
        Registries.MATERIAL_RULE.freeze();
    }
}
