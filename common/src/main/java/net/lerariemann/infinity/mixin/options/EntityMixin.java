package net.lerariemann.infinity.mixin.options;

import net.lerariemann.infinity.options.InfinityOptions;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class EntityMixin {
    @Shadow
    private World world;

    @Inject(method = "getFinalGravity", at = @At("RETURN"), cancellable = true)
    private void injected(CallbackInfoReturnable<Double> cir) {
        double mavity = InfinityOptions.access(world).getMavity();
        cir.setReturnValue(cir.getReturnValue() * mavity);
    }
}
