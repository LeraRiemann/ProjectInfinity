package net.lerariemann.infinity.mixin.options;

import com.mojang.blaze3d.systems.RenderSystem;
import net.lerariemann.infinity.access.InfinityOptionsAccess;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {
    @Shadow private ClientWorld world;
    @Shadow private VertexBuffer lightSkyBuffer;
    @ModifyConstant(method = "renderSky(Lnet/minecraft/client/util/math/MatrixStack;Lorg/joml/Matrix4f;FLnet/minecraft/client/render/Camera;ZLjava/lang/Runnable;)V", constant = @Constant(floatValue = 30.0f))
    private float injected(float constant) {
        return ((InfinityOptionsAccess)MinecraftClient.getInstance()).getInfinityOptions().getSolarSize();
    }

    @ModifyConstant(method = "renderSky(Lnet/minecraft/client/util/math/MatrixStack;Lorg/joml/Matrix4f;FLnet/minecraft/client/render/Camera;ZLjava/lang/Runnable;)V", constant = @Constant(floatValue = 20.0f))
    private float injected2(float constant) {
        return ((InfinityOptionsAccess)MinecraftClient.getInstance()).getInfinityOptions().getLunarSize();
    }

    @Inject(method = "renderSky(Lnet/minecraft/client/util/math/MatrixStack;Lorg/joml/Matrix4f;FLnet/minecraft/client/render/Camera;ZLjava/lang/Runnable;)V",
            at=@At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;enableBlend()V"))
    private void injected3(MatrixStack matrices, Matrix4f projectionMatrix, float tickDelta, Camera camera, boolean thickFog, Runnable fogCallback, CallbackInfo ci) {
        if (skyType().contains("LSD_rainbow")) {
            renderLSDSky(matrices, tickDelta);
        }
        else if (skyType().contains("rainbow")) {
            renderRainbowSky(matrices, tickDelta, projectionMatrix);
        }
        else if (skyType().contains("LSD")) {
            renderCustomSky(matrices, LSD_SKY[0], 1.0f, 255, 255, tickDelta, 0.1f);
        }
    }
    @Unique
    private static String skyType() {
        return ((InfinityOptionsAccess)MinecraftClient.getInstance()).getInfinityOptions().getSkyType();
    }
    @Unique
    private void renderRainbowSky(MatrixStack matrices, float tickDelta, Matrix4f projectionMatrix) {
        RenderSystem.depthMask(false);
        float main = world.getSkyAngle(tickDelta) * 2;
        int color = Color.getHSBColor(main - (int)main, 1.0f, 1.0f).getRGB();
        float f = MathHelper.clamp(MathHelper.cos(world.getSkyAngle(tickDelta) * ((float)Math.PI * 2)) * 2.0f + 0.5f, 0.2f, 1.0f);
        RenderSystem.setShaderColor(f * (float)(color >> 16 & 0xFF) / 255.0f, f * (float)(color >> 8 & 0xFF) / 255.0f, f * (float)(color & 0xFF) / 255.0f, 1.0f);
        ShaderProgram shaderProgram = RenderSystem.getShader();
        this.lightSkyBuffer.bind();
        this.lightSkyBuffer.draw(matrices.peek().getPositionMatrix(), projectionMatrix, shaderProgram);
        VertexBuffer.unbind();
    }
    @Unique
    private static final Identifier[] LSD_SKY = new Identifier[]{new Identifier("infinity:textures/lsd.png"),
            new Identifier("infinity:textures/lsd60.png"),
            new Identifier("infinity:textures/lsd120.png"),
            new Identifier("infinity:textures/lsd180.png"),
            new Identifier("infinity:textures/lsd240.png"),
            new Identifier("infinity:textures/lsd300.png")};
    @Unique
    private void renderLSDSky(MatrixStack matrices, float tickDelta) {
        RenderSystem.enableBlend();
        float main = world.getSkyAngle(tickDelta)*12;
        int i = ((int)main)%6;
        int j = ((int)main + 1)%6;
        float alpha = main - (int)main;
        renderCustomSky(matrices, LSD_SKY[i], 1.0f, 255, (int)(255*(1-alpha)), tickDelta, 0.1f);
        renderCustomSky(matrices, LSD_SKY[j], 1.0f, 255, (int)(255*alpha), tickDelta, 0.1f);
        RenderSystem.disableBlend();
    }
    @Unique
    private void renderCustomSky(MatrixStack matrices, Identifier texture, float copies, int brightness, int alpha, float tickDelta, float night) {
        renderCustomSky(matrices, texture, copies, brightness, brightness, brightness, alpha, tickDelta, night);
    }
    @Unique
    private void renderCustomSky(MatrixStack matrices, Identifier texture, float copies, int r, int g, int b, int alpha, float tickDelta, float night) {
        RenderSystem.enableBlend();
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getPositionTexColorProgram);
        float f = MathHelper.clamp(MathHelper.cos(world.getSkyAngle(tickDelta) * ((float)Math.PI * 2)) * 2.0f + 0.5f, night, 1.0f);
        RenderSystem.setShaderColor(f, f, f, 1.0f);
        RenderSystem.setShaderTexture(0, texture);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
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
            bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
            bufferBuilder.vertex(matrix4f, -100.0f, -100.0f, -100.0f).texture(0.0f, 0.0f).color(r, g, b, alpha).next();
            bufferBuilder.vertex(matrix4f, -100.0f, -100.0f, 100.0f).texture(0.0f, copies).color(r, g, b, alpha).next();
            bufferBuilder.vertex(matrix4f, 100.0f, -100.0f, 100.0f).texture(copies, copies).color(r, g, b, alpha).next();
            bufferBuilder.vertex(matrix4f, 100.0f, -100.0f, -100.0f).texture(copies, 0.0f).color(r, g, b, alpha).next();
            tessellator.draw();
            matrices.pop();
        }
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
    }
}
