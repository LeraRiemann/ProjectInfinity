package net.lerariemann.infinity.mixin.qol;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.Keyboard;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Keyboard.class)
public class KeyboardMixin {
    @WrapOperation(method="onKey", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/GameRenderer;togglePostProcessorEnabled()V"))
    void inj(GameRenderer instance, Operation<Void> original) {
    }
}
