package net.lerariemann.infinity.mixin.fixes;

import net.lerariemann.infinity.access.MobEntityAccess;
import net.minecraft.entity.mob.MobEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(MobEntity.class)
public class MobEntityMixin implements MobEntityAccess {
    @Shadow
    private boolean persistent;

    public void infinity$setPersistent(boolean bl) {
        this.persistent = bl;
    }
}
