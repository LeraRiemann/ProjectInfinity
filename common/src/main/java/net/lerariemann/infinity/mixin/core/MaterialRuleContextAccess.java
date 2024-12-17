package net.lerariemann.infinity.mixin.core;

import net.minecraft.world.gen.surfacebuilder.MaterialRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/* Allows modded material rules to work. */
@Mixin(MaterialRules.MaterialRuleContext.class)
public interface MaterialRuleContextAccess {
    @Accessor
    int getBlockX();
    @Accessor
    int getBlockY();
    @Accessor
    int getBlockZ();
}
