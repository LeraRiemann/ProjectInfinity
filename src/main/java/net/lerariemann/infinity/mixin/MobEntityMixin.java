package net.lerariemann.infinity.mixin;

import net.lerariemann.infinity.access.MobEntityAccess;
import net.minecraft.entity.mob.MobEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(MobEntity.class)
public class MobEntityMixin implements MobEntityAccess {
    @Shadow
    private boolean persistent;

    public void setPersistent(boolean bl) {
        this.persistent = bl;
    }
}
