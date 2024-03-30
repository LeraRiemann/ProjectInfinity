package net.lerariemann.infinity.mixin;

import net.lerariemann.infinity.access.MinecraftClientAccess;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {
    @ModifyConstant(method = "renderSky(Lnet/minecraft/client/util/math/MatrixStack;Lorg/joml/Matrix4f;FLnet/minecraft/client/render/Camera;ZLjava/lang/Runnable;)V", constant = @Constant(floatValue = 30.0f))
    private float injected(float constant) {
        return ((MinecraftClientAccess)MinecraftClient.getInstance()).getInfinityOptions().getSolarSize();
    }

    @ModifyConstant(method = "renderSky(Lnet/minecraft/client/util/math/MatrixStack;Lorg/joml/Matrix4f;FLnet/minecraft/client/render/Camera;ZLjava/lang/Runnable;)V", constant = @Constant(floatValue = 20.0f))
    private float injected2(float constant) {
        return ((MinecraftClientAccess)MinecraftClient.getInstance()).getInfinityOptions().getLunarSize();
    }
}
