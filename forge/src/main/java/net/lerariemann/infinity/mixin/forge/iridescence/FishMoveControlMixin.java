package net.lerariemann.infinity.mixin.forge.iridescence;

import net.lerariemann.infinity.iridescence.Iridescence;
import net.minecraft.entity.ai.control.MoveControl;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.FishEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(FishEntity.FishMoveControl.class)
public class FishMoveControlMixin extends MoveControl {
    @Final
    @Shadow
    private FishEntity fish;

    public FishMoveControlMixin(MobEntity entity) {
        super(entity);
    }

    @ModifyArgs(method = "tick", at= @At(value = "INVOKE",
            target = "Lnet/minecraft/util/math/MathHelper;atan2(DD)D"))
    void inj(Args args) {
        if (Iridescence.isUnderEffect(fish)) {
            double g = args.get(0);
            double d = args.get(1);
            args.set(0, -1*g);
            args.set(1, -1*d);
        }
    }
}
