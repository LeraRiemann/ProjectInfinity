package net.lerariemann.infinity.mixin;

import net.lerariemann.infinity.entity.custom.DimensionalSlime;
import net.minecraft.entity.mob.SlimeEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import static net.lerariemann.infinity.entity.custom.DimensionalSlime.color;

@Mixin(SlimeEntity.class)
public class SlimeEntityMixin {
    @Redirect(method = "remove(Lnet/minecraft/entity/Entity$RemovalReason;)V", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/entity/mob/SlimeEntity;setAiDisabled(Z)V"))
    private void injected(SlimeEntity instance, boolean b) {
        instance.setAiDisabled(b);
        SlimeEntity e = ((SlimeEntity)(Object)(this));
        if (e instanceof DimensionalSlime slime_mom) {
            DimensionalSlime slime_son = (DimensionalSlime)instance;
            slime_son.setCore(slime_mom.getCoreForChild());
            slime_son.setColor(slime_mom.getDataTracker().get(color));
        }
    }
}
