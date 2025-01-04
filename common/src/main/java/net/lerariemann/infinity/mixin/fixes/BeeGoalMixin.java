package net.lerariemann.infinity.mixin.fixes;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.entity.passive.BeeEntity;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** By some lunacy Mojang made a thread-unsafe random call in a really inconvenient place,
 * which spams logs with warnings when a bee tries to spawn.
 * I fixed that :D */
@Mixin(BeeEntity.MoveToHiveGoal.class)
public abstract class BeeGoalMixin {
    @WrapOperation(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/random/Random;nextInt(I)I"))
    private int inj(Random instance, int i, Operation<Integer> original) {
        return 10;
    }

    @Mixin(BeeEntity.MoveToFlowerGoal.class)
    public abstract static class BeeGoalMixin2 {
        @WrapOperation(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/random/Random;nextInt(I)I"))
        private int inj(Random instance, int i, Operation<Integer> original) {
            return 10;
        }
    }
}
