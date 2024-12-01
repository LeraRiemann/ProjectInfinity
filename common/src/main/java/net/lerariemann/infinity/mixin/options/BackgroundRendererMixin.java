package net.lerariemann.infinity.mixin.options;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.options.InfinityOptions;
import net.minecraft.block.enums.CameraSubmersionType;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.Color;

@Mixin(BackgroundRenderer.class)
public class BackgroundRendererMixin {
    @Shadow
    private static float red;
    @Shadow
    private static float green;
    @Shadow
    private static float blue;


    @Inject(method = "render(Lnet/minecraft/client/render/Camera;FLnet/minecraft/client/world/ClientWorld;IF)V",
            at= @At(value = "INVOKE", target = "Lnet/minecraft/client/render/BackgroundRenderer;getFogModifier(Lnet/minecraft/entity/Entity;F)Lnet/minecraft/client/render/BackgroundRenderer$StatusEffectFogModifier;"))
    private static void injected(Camera camera, float tickDelta, ClientWorld world, int viewDistance, float skyDarkness, CallbackInfo ci) {
        InfinityOptions options = InfinityOptions.access(world);
        if (options.getSkyType().equals("rainbow") && camera.getSubmersionType() == CameraSubmersionType.NONE) {
            infinity$applyRainbowFog(world, tickDelta);
        }
    }

    /* Disable void fog in custom dimensions. */
    @ModifyExpressionValue(method = "render(Lnet/minecraft/client/render/Camera;FLnet/minecraft/client/world/ClientWorld;IF)V",
    at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientWorld$Properties;getHorizonShadingRatio()F"))
    private static float inj(float original, @Local(argsOnly = true) ClientWorld world) {
        if (!InfinityMod.isInfinity(world)) return original;
        return InfinityOptions.access(world).getHorizonShadingRatio();
    }

    @Unique
    private static void infinity$applyRainbowFog(ClientWorld world, float tickDelta) {
        float main = world.getSkyAngle(tickDelta) * 2 + 0.5f;
        main -= (int)main;
        int color = Color.getHSBColor(main, 1.0f, 1.0f).getRGB();
        float f = MathHelper.clamp(MathHelper.cos(world.getSkyAngle(tickDelta) * ((float)Math.PI * 2)) * 2.0f + 0.5f, 0.2f, 1.0f);
        red = f * (float)(color >> 16 & 0xFF) / 255.0f;
        green = f * (float)(color >> 8 & 0xFF) / 255.0f;
        blue = f * (float)(color & 0xFF) / 255.0f;
    }
}
