package net.lerariemann.infinity.mixin.options;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.lerariemann.infinity.access.InfinityOptionsAccess;
import net.lerariemann.infinity.access.WorldRendererAccess;
import net.lerariemann.infinity.options.InfinityOptions;
import net.lerariemann.infinity.options.SkyRenderer;
import net.minecraft.client.MinecraftClient;
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
    @Shadow private VertexBuffer lightSkyBuffer;
    @Shadow private VertexBuffer starsBuffer;

    @Shadow protected abstract boolean hasBlindnessOrDarkness(Camera camera);

    @Unique
    public boolean infinity$needsStars;
    @Shadow protected abstract void renderStars();


    @Unique
    public void infinity$testRerenderStars() {
        if (infinity$needsStars) {
            renderStars();
            infinity$needsStars = false;
        }
    }
    @Override
    public void projectInfinity$setNeedsStars(boolean b) {
        infinity$needsStars = b;
    }

    @Inject(method = "renderSky(Lnet/minecraft/client/util/math/MatrixStack;Lorg/joml/Matrix4f;FLnet/minecraft/client/render/Camera;ZLjava/lang/Runnable;)V",
            at=@At("HEAD"), cancellable=true)
    private void injected4(MatrixStack matrices, Matrix4f projectionMatrix, float tickDelta, Camera camera, boolean thickFog, Runnable fogCallback, CallbackInfo ci) {
        if (!infinity$options().isEmpty()) {
            infinity$renderEntireSky(matrices, projectionMatrix, tickDelta, camera, thickFog, fogCallback);
            ci.cancel();
        }
    }
    @ModifyConstant(method = "renderStars(Lnet/minecraft/client/render/BufferBuilder;)Lnet/minecraft/client/render/BufferBuilder$BuiltBuffer;", constant = @Constant(intValue = 1500))
    private int injected(int constant) {
        return infinity$options().getNumStars();
    }
    @ModifyConstant(method = "renderStars(Lnet/minecraft/client/render/BufferBuilder;)Lnet/minecraft/client/render/BufferBuilder$BuiltBuffer;", constant = @Constant(floatValue = 0.15f))
    private float injected2(float constant) {
        return infinity$options().getStarSizeBase();
    }
    @ModifyConstant(method = "renderStars(Lnet/minecraft/client/render/BufferBuilder;)Lnet/minecraft/client/render/BufferBuilder$BuiltBuffer;", constant = @Constant(floatValue = 0.1f))
    private float injected3(float constant) {
        return infinity$options().getStarSizeModifier();
    }

    @Unique
    private void infinity$renderEntireSky(MatrixStack matrices, Matrix4f projectionMatrix, float tickDelta, Camera camera, boolean thickFog, Runnable fogCallback) {
        fogCallback.run();
        if (thickFog || hasBlindnessOrDarkness(camera) || SkyRenderer.testCameraCancels(camera)) return;
        MatrixStack matrices = new MatrixStack();
        matrices.multiplyPositionMatrix(matrix4f);
        SkyRenderer renderer = new SkyRenderer(infinity$options(), client, world,
                matrices, tickDelta, projectionMatrix,
                lightSkyBuffer, starsBuffer);
        if (renderer.testAndRenderNonOverworldySkies()) return;
        infinity$testRerenderStars();
        renderer.setupOverworldySky();
        renderer.renderAllCelestialBodies(fogCallback);
        renderer.finish();
    }

    @Unique
    private InfinityOptions infinity$options() {
        InfinityOptions options = ((InfinityOptionsAccess)client).infinity$getOptions();
        if (options == null) options = InfinityOptions.empty();
        return options;
    }
}
