package net.lerariemann.infinity.mixin.options;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.access.InfinityOptionsAccess;
import net.lerariemann.infinity.access.WorldRendererAccess;
import net.lerariemann.infinity.options.InfinityOptions;
import net.minecraft.block.enums.CameraSubmersionType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;

@Environment(EnvType.CLIENT)
@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin implements WorldRendererAccess {
    @Shadow private ClientWorld world;
    @Final
    @Shadow private MinecraftClient client;
    @Shadow private VertexBuffer lightSkyBuffer;
    @Shadow private VertexBuffer starsBuffer;

    @Shadow protected abstract boolean hasBlindnessOrDarkness(Camera camera);

    @Shadow protected abstract void renderStars();

    @Unique
    public boolean needsStars;

    @Unique
    public void testRerenderStars() {
        if (needsStars) {
            renderStars();
            needsStars = false;
        }
    }
    @Override
    public void projectInfinity$setNeedsStars(boolean b) {
        needsStars = b;
    }

    @Inject(method = "renderSky(Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;FLnet/minecraft/client/render/Camera;ZLjava/lang/Runnable;)V",
            at=@At("HEAD"), cancellable=true)
    private void injected4(Matrix4f matrix4f, Matrix4f projectionMatrix, float tickDelta, Camera camera, boolean thickFog, Runnable fogCallback, CallbackInfo ci) {
        if (!options().isEmpty()) {
            renderEntireSky(matrix4f, projectionMatrix, tickDelta, camera, thickFog, fogCallback);
            ci.cancel();
        }
    }
    @ModifyConstant(method = "buildStarsBuffer(Lnet/minecraft/client/render/Tessellator;)Lnet/minecraft/client/render/BuiltBuffer;", constant = @Constant(intValue = 1500))
    private int injected(int constant) {
        return options().getNumStars();
    }
    @ModifyConstant(method = "buildStarsBuffer(Lnet/minecraft/client/render/Tessellator;)Lnet/minecraft/client/render/BuiltBuffer;", constant = @Constant(floatValue = 0.15f))
    private float injected2(float constant) {
        return options().getStarSizeBase();
    }
    @ModifyConstant(method = "buildStarsBuffer(Lnet/minecraft/client/render/Tessellator;)Lnet/minecraft/client/render/BuiltBuffer;", constant = @Constant(floatValue = 0.1f))
    private float injected3(float constant) {
        return options().getStarSizeModifier();
    }

    @Unique
    private void renderEntireSky(Matrix4f matrix4f, Matrix4f projectionMatrix, float tickDelta, Camera camera, boolean thickFog, Runnable fogCallback) {
        fogCallback.run();
        if (thickFog) {
            return;
        }
        CameraSubmersionType cameraSubmersionType = camera.getSubmersionType();
        if (cameraSubmersionType == CameraSubmersionType.POWDER_SNOW || cameraSubmersionType == CameraSubmersionType.LAVA || hasBlindnessOrDarkness(camera)) {
            return;
        }
        MatrixStack matrices = new MatrixStack();
        matrices.multiplyPositionMatrix(matrix4f);
        if (client.world!=null && client.world.getDimensionEffects().getSkyType() == DimensionEffects.SkyType.END) {
            this.renderCustomSky(matrices, Identifier.of("textures/environment/end_sky.png"), 16.0f, 40, 255, tickDelta, false);
            return;
        }
        if (options().endSkyLike()) {
            handleSkyBackground(matrices, projectionMatrix, tickDelta);
            return;
        }
        if (client.world!=null && client.world.getDimensionEffects().getSkyType() != DimensionEffects.SkyType.NORMAL) {
            return;
        }

        BackgroundRenderer.applyFogColor();
        Tessellator tessellator = Tessellator.getInstance();
        RenderSystem.depthMask(false);
        handleSkyBackground(matrices, projectionMatrix, tickDelta);
        handleFog(matrices, tessellator, tickDelta);
        matrices.push();

        float rain_alpha = 1.0f - this.world.getRainGradient(tickDelta);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, rain_alpha);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(options().getSolarTilt()));
        rotate_with_velocity(matrices, tickDelta, 1);
        Matrix4f matrix4f2 = matrices.peek().getPositionMatrix(); //creates the rotating layer for stellar bodies

        renderSun(tessellator, matrix4f2, options().getSolarTexture(), options().getSolarSize(), 100.0f, options().getSolarTint());
        for (int i = 0; i < options().getNumMoons(); i++) {
            renderSingleMoon(matrices, tessellator, tickDelta, options().getLunarSize(i), options().getLunarTiltY(i), options().getLunarTiltZ(i),
                    options().getLunarVelocity(i), options().getLunarOffset(i), options().getLunarTint(i), options().getLunarTexture(i));
        }
        testRerenderStars();
        renderStars(matrix4f2, tickDelta, projectionMatrix, fogCallback, rain_alpha);

        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
        matrices.pop();
        RenderSystem.depthMask(true);
    }

    @Unique
    private void rotate_with_velocity(MatrixStack matrices, float tickDelta, float v, float offset) {
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees((this.world.getSkyAngle(tickDelta) + offset) * 360.0f * v));
    }

    @Unique
    private void rotate_with_velocity(MatrixStack matrices, float tickDelta, float v) {
        rotate_with_velocity(matrices, tickDelta, v, 0.0f);
    }

    @Unique
    private void handleSkyBackground(MatrixStack matrices, Matrix4f projectionMatrix, float tickDelta) {
        String skyType = options().getSkyType();
        if (skyType.equals("empty")) {
            Vec3d vec3d = this.world.getSkyColor(this.client.gameRenderer.getCamera().getPos(), tickDelta);
            renderSingleColorSky(matrices, projectionMatrix, (float)vec3d.x, (float)vec3d.y, (float)vec3d.z, 1.0f);
        }
        else if (skyType.equals("rainbow")) {
            renderRainbowSky(matrices, tickDelta, projectionMatrix);
        }
        else {
            if (options().getCelestialVelocity() != 0.0f) {
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(options().getCelestialTilt()));
                rotate_with_velocity(matrices, tickDelta, options().getCelestialVelocity());
            }
            boolean color = !options().endSkyLike();
            if (skyType.contains("textures")) {
                renderCustomSky(matrices, Identifier.of(skyType), tickDelta, color);
            }
            else if (skyType.equals("LSD_rainbow")) {
                renderLSDSky(matrices, tickDelta, color);
            }
            else if (skyType.equals("LSD")) {
                renderCustomSky(matrices, LSD_SKY[0], 1.0f, 255, 255, tickDelta, color);
            }
            if (options().getCelestialVelocity() != 0.0f) {
                rotate_with_velocity(matrices, tickDelta, -1 * options().getCelestialVelocity());
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-1 * options().getCelestialTilt()));
            }
        }
    }
    @Unique
    private void renderSingleColorSky(MatrixStack matrices, Matrix4f projectionMatrix, float f, float g, float h, float a) {
        RenderSystem.setShaderColor(f, g, h, a);
        lightSkyBuffer.bind();
        lightSkyBuffer.draw(matrices.peek().getPositionMatrix(), projectionMatrix, RenderSystem.getShader());
        VertexBuffer.unbind();
    }

    @Unique
    private void renderSun(Tessellator tessellator, Matrix4f matrix4f2, Identifier texture, float k, float y, Vector3f tint) {
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.setShaderColor(tint.x, tint.y, tint.z, 1.0f);
        BufferBuilder bufferBuilder = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        bufferBuilder.vertex(matrix4f2, -k, y, -k).texture(0.0f, 0.0f);
        bufferBuilder.vertex(matrix4f2, k, y, -k).texture(1.0f, 0.0f);
        bufferBuilder.vertex(matrix4f2, k, y, k).texture(1.0f, 1.0f);
        bufferBuilder.vertex(matrix4f2, -k, y, k).texture(0.0f, 1.0f);
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
    }

    @Unique
    private void renderSingleMoon(MatrixStack matrices, Tessellator tessellator, float tickDelta, float size, float tilt_y, float tilt_z, float velocity, float offset, Vector3f tint, Identifier texture) {
        float lunarv = (velocity != 1.0f) ? velocity - 1 : 0;
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(tilt_y));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(tilt_z));
        rotate_with_velocity(matrices, tickDelta, lunarv, offset);
        renderMoon(tessellator, matrices.peek().getPositionMatrix(), texture, size, -100.0f, tint);
        rotate_with_velocity(matrices, tickDelta, -1 * lunarv, offset);
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-tilt_z));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-tilt_y));
    }
    @Unique
    private void renderMoon(Tessellator tessellator, Matrix4f matrix4f2, Identifier texture, float k, float y, Vector3f tint) {
        float t, q, p, o;
        if (!options().isMoonCustom()) {
            int r = this.world.getMoonPhase();
            int s = r % 4;
            int m = r / 4 % 2;
            t = (float)(s) / 4.0f;
            o = (float)(m) / 2.0f;
            p = (float)(s + 1) / 4.0f;
            q = (float)(m + 1) / 2.0f;
        }
        else {
            t = q = 1.0f;
            p = o = 0.0f;
        }
        renderMoon(tessellator, matrix4f2, texture, k, y, tint, t, q, p, o);
    }
    @Unique
    private void renderMoon(Tessellator tessellator, Matrix4f matrix4f2, Identifier texture, float k, float y, Vector3f tint, float t, float q, float p, float o) {
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.setShaderColor(tint.x, tint.y, tint.z, 1.0f);
        BufferBuilder bufferBuilder = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        bufferBuilder.vertex(matrix4f2, -k, y, k).texture(p, q);
        bufferBuilder.vertex(matrix4f2, k, y, k).texture(t, q);
        bufferBuilder.vertex(matrix4f2, k, y, -k).texture(t, o);
        bufferBuilder.vertex(matrix4f2, -k, y, -k).texture(p, o);
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
    }

    @Unique
    private void renderStars(Matrix4f matrix4f2, float tickDelta, Matrix4f projectionMatrix, Runnable fogCallback, float i) {
        float u = world.getStarBrightness(tickDelta) * i;
        Vector3f color = options().getStellarColor();
        if (u > 0.0f) {
            RenderSystem.setShaderColor(u*color.x, u*color.y, u*color.z, u);
            BackgroundRenderer.clearFog();
            starsBuffer.bind();
            starsBuffer.draw(matrix4f2, projectionMatrix, GameRenderer.getPositionProgram());
            VertexBuffer.unbind();
            fogCallback.run();
        }
    }

    @Unique
    private InfinityOptions options() {
        InfinityOptions options = ((InfinityOptionsAccess)client).projectInfinity$getInfinityOptions();
        if (options == null) options = InfinityOptions.empty();
        return options;
    }
    @Unique
    private void renderRainbowSky(MatrixStack matrices, float tickDelta, Matrix4f projectionMatrix) {
        float main = world.getSkyAngle(tickDelta) * 2;
        int color = Color.getHSBColor(main - (int)main, 1.0f, 1.0f).getRGB();
        float f = MathHelper.clamp(MathHelper.cos(world.getSkyAngle(tickDelta) * ((float)Math.PI * 2)) * 2.0f + 0.5f, 0.2f, 1.0f);
        renderSingleColorSky(matrices, projectionMatrix, f * (float)(color >> 16 & 0xFF) / 255.0f, f * (float)(color >> 8 & 0xFF) / 255.0f, f * (float)(color & 0xFF) / 255.0f, 1.0f);
    }
    @Unique
    private static final Identifier[] LSD_SKY = new Identifier[]{InfinityMod.getId("textures/lsd.png"),
            InfinityMod.getId("textures/lsd60.png"),
            InfinityMod.getId("textures/lsd120.png"),
            InfinityMod.getId("textures/lsd180.png"),
            InfinityMod.getId("textures/lsd240.png"),
            InfinityMod.getId("textures/lsd300.png")};

    @Unique
    private void renderLSDSky(MatrixStack matrices, float tickDelta, boolean color) {
        RenderSystem.enableBlend();
        float main = world.getSkyAngle(tickDelta)*12;
        int i = ((int)main)%6;
        int j = ((int)main + 1)%6;
        float alpha = main - (int)main;
        renderCustomSky(matrices, LSD_SKY[i], 1.0f, 255, (int)(255*(1-alpha)), tickDelta, color);
        renderCustomSky(matrices, LSD_SKY[j], 1.0f, 255, (int)(255*alpha), tickDelta, color);
        RenderSystem.disableBlend();
    }
    @Unique
    private void renderCustomSky(MatrixStack matrices, Identifier texture, float tickDelta, boolean color) {
        renderCustomSky(matrices, texture, options().getCelestialTilesAmount(), options().getCelestialBrightness(), options().getCelestialAlpha(), tickDelta, color);
    }
    @Unique
    private void renderCustomSky(MatrixStack matrices, Identifier texture, float copies, int brightness, int alpha, float tickDelta, boolean color) {
        renderCustomSky(matrices, texture, copies, brightness, brightness, brightness, alpha, tickDelta, color);
    }
    @Unique
    private void renderCustomSky(MatrixStack matrices, Identifier texture, float copies, int r, int g, int b, int alpha, float tickDelta, boolean color) {
        RenderSystem.enableBlend();
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getPositionTexColorProgram);
        if (color) {
            float f = MathHelper.clamp(MathHelper.cos(world.getSkyAngle(tickDelta) * ((float)Math.PI * 2)) * 2.0f + 0.5f, options().getCelestialNightBrightness(), 1.0f);
            RenderSystem.setShaderColor(f, f, f, 1.0f);
        }
        RenderSystem.setShaderTexture(0, texture);
        Tessellator tessellator = Tessellator.getInstance();

        for (int i = 0; i < 6; ++i) {
            matrices.push();
            if (i == 1) {
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90.0f));
            }
            if (i == 2) {
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-90.0f));
            }
            if (i == 3) {
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180.0f));
            }
            if (i == 4) {
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(90.0f));
            }
            if (i == 5) {
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-90.0f));
            }

            Matrix4f matrix4f = matrices.peek().getPositionMatrix();
            BufferBuilder bufferBuilder = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
            bufferBuilder.vertex(matrix4f, -100.0f, -100.0f, -100.0f).texture(0.0f, 0.0f).color(r, g, b, alpha);
            bufferBuilder.vertex(matrix4f, -100.0f, -100.0f, 100.0f).texture(0.0f, copies).color(r, g, b, alpha);
            bufferBuilder.vertex(matrix4f, 100.0f, -100.0f, 100.0f).texture(copies, copies).color(r, g, b, alpha);
            bufferBuilder.vertex(matrix4f, 100.0f, -100.0f, -100.0f).texture(copies, 0.0f).color(r, g, b, alpha);
            BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
            matrices.pop();
        }
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
    }

    @Unique
    private void handleFog(MatrixStack matrices, Tessellator tessellator, float tickDelta) {
        RenderSystem.enableBlend();
        float[] fs = this.world.getDimensionEffects().getFogColorOverride(this.world.getSkyAngle(tickDelta), tickDelta);
        if (fs != null) {
            RenderSystem.setShader(GameRenderer::getPositionColorProgram);
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            matrices.push();
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90.0f));
            float i = MathHelper.sin(this.world.getSkyAngleRadians(tickDelta)) < 0.0f ? 180.0f : 0.0f;
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(i));
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(90.0f));
            float j = fs[0];
            float k = fs[1];
            float l = fs[2];
            Matrix4f matrix4f = matrices.peek().getPositionMatrix();
            BufferBuilder bufferBuilder = tessellator.begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
            bufferBuilder.vertex(matrix4f, 0.0f, 100.0f, 0.0f).color(j, k, l, fs[3]);
            for (int n = 0; n <= 16; ++n) {
                float o = (float)n * ((float)Math.PI * 2) / 16.0f;
                float p = MathHelper.sin(o);
                float q = MathHelper.cos(o);
                bufferBuilder.vertex(matrix4f, p * 120.0f, q * 120.0f, -q * 40.0f * fs[3]).color(fs[0], fs[1], fs[2], 0.0f);
            }
            BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
            matrices.pop();
        }
        RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE, GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ZERO);
    }
}
