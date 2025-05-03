package net.lerariemann.infinity.mixin.options;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.lerariemann.infinity.access.WorldRendererAccess;
import net.lerariemann.infinity.options.InfinityOptions;
import net.lerariemann.infinity.options.SkyRenderer;
import net.lerariemann.infinity.util.InfinityMethods;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin implements WorldRendererAccess {
    @Shadow private ClientWorld world;
    @Final
    @Shadow private MinecraftClient client;

    @Shadow protected abstract boolean hasBlindnessOrDarkness(Camera camera);


    @Override
    public void infinity$setNeedsStars(boolean b) {
        renderStars();
    }

    @Inject(method = "renderSky",
            at=@At("HEAD"), cancellable=true)
    private void injected4(FrameGraphBuilder frameGraphBuilder, Camera camera, float tickDelta, Fog fog, CallbackInfo ci) {
        if (InfinityMethods.isInfinity(world)) {
            infinity$renderEntireSky(frameGraphBuilder, camera, tickDelta, fog, ci);
            ci.cancel();
        }
    }

    @ModifyConstant(method = "buildStarsBuffer(Lnet/minecraft/client/render/Tessellator;)Lnet/minecraft/client/render/BuiltBuffer;", constant = @Constant(intValue = 1500))
    private int injected(int constant) {
        return infinity$options().getNumStars();
    }
    @ModifyConstant(method = "buildStarsBuffer(Lnet/minecraft/client/render/Tessellator;)Lnet/minecraft/client/render/BuiltBuffer;", constant = @Constant(floatValue = 0.15f))
    private float injected2(float constant) {
        return infinity$options().getStarSizeBase();
    }
    @ModifyConstant(method = "buildStarsBuffer(Lnet/minecraft/client/render/Tessellator;)Lnet/minecraft/client/render/BuiltBuffer;", constant = @Constant(floatValue = 0.1f))
    private float injected3(float constant) {
        return infinity$options().getStarSizeModifier();
    }

    @Unique
    private void infinity$renderEntireSky(FrameGraphBuilder frameGraphBuilder, Camera camera, float tickDelta, Fog fog, CallbackInfo ci) {
        fogCallback.run();
        if (thickFog || hasBlindnessOrDarkness(camera) || SkyRenderer.testCameraCancels(camera)) return;
        MatrixStack matrices = new MatrixStack();
        matrices.multiplyPositionMatrix(matrix4f);
        SkyRenderer renderer = new SkyRenderer(infinity$options(), client, world,
                matrices, tickDelta, projectionMatrix,
                lightSkyBuffer, starsBuffer);
        renderer.render(fogCallback);
    }
    @Unique
    private InfinityOptions infinity$options() {
        return InfinityOptions.ofClient(client);
    }
}
