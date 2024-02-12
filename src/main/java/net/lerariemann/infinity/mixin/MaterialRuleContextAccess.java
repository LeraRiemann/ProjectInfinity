package net.lerariemann.infinity.mixin;

import net.minecraft.world.gen.surfacebuilder.MaterialRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MaterialRules.MaterialRuleContext.class)
public interface MaterialRuleContextAccess {
    @Accessor
    int getBlockX();
    @Accessor
    int getBlockY();
    @Accessor
    int getBlockZ();
}
