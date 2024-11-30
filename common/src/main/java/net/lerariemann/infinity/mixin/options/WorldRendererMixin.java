package net.lerariemann.infinity.mixin.options;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
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
import org.spongepowered.asm.mixin.injection.Inject;
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

    @Shadow public abstract void render(RenderTickCounter tickCounter, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, Matrix4f matrix4f2);

    @Unique
    public boolean infinity$needsStars;
    @Override
    public void infinity$setNeedsStars(boolean b) {
        infinity$needsStars = b;
    }

    @Inject(method = "renderSky(Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;FLnet/minecraft/client/render/Camera;ZLjava/lang/Runnable;)V",
            at=@At("HEAD"), cancellable=true)
    private void injected4(Matrix4f matrix4f, Matrix4f projectionMatrix, float tickDelta, Camera camera, boolean thickFog, Runnable fogCallback, CallbackInfo ci) {
        if (!infinity$options().isEmpty()) {
            infinity$renderEntireSky(matrix4f, projectionMatrix, tickDelta, camera, thickFog, fogCallback);
            ci.cancel();
        }
    }
    @Unique
    private void infinity$renderEntireSky(Matrix4f matrix4f, Matrix4f projectionMatrix, float tickDelta, Camera camera, boolean thickFog, Runnable fogCallback) {
        fogCallback.run();
        if (thickFog || hasBlindnessOrDarkness(camera) || SkyRenderer.testCameraCancels(camera)) return;
        MatrixStack matrices = new MatrixStack();
        matrices.multiplyPositionMatrix(matrix4f);
        SkyRenderer renderer = new SkyRenderer(infinity$options(), client, world,
                matrices, tickDelta, projectionMatrix,
                lightSkyBuffer, starsBuffer);
        if (renderer.testAndRenderNonOverworldySkies()) return;
        infinity$createStarsIfNeeded();
        renderer.setupOverworldySky();
        renderer.renderAllCelestialBodies(fogCallback);
        renderer.finish();
    }
    @Unique
    public void infinity$createStarsIfNeeded() {
        if (infinity$needsStars) {
            if (this.starsBuffer != null) {
                this.starsBuffer.close();
            }
            this.starsBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
            this.starsBuffer.bind();
            this.starsBuffer.upload(SkyRenderer.buildStarsBuffer(Tessellator.getInstance(), infinity$options()));
            VertexBuffer.unbind();
            infinity$needsStars = false;
        }
    }
    @Unique
    private InfinityOptions infinity$options() {
        return InfinityOptions.ofClient(client);
    }
}
