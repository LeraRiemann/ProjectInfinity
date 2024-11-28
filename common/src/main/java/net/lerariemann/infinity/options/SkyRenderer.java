package net.lerariemann.infinity.options;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.awt.*;

@Environment(EnvType.CLIENT)
public record SkyRenderer(InfinityOptions options, MinecraftClient client, ClientWorld world,
                          MatrixStack matrices, Tessellator tessellator, float tickDelta, Matrix4f projectionMatrix,
                          VertexBuffer lightSkyBuffer, VertexBuffer starsBuffer) {
    
    public SkyRenderer(InfinityOptions options, MinecraftClient client, ClientWorld world,
                MatrixStack matrices, float tickDelta, Matrix4f projectionMatrix,
                VertexBuffer lightSkyBuffer, VertexBuffer starsBuffer) {
        this(options, client, world, matrices, Tessellator.getInstance(), tickDelta, projectionMatrix, lightSkyBuffer, starsBuffer);
    }

    public static boolean testCameraCancels(Camera camera) {
        CameraSubmersionType cameraSubmersionType = camera.getSubmersionType();
        return  (cameraSubmersionType == CameraSubmersionType.POWDER_SNOW
                || cameraSubmersionType == CameraSubmersionType.LAVA);
    }
    
    public boolean testAndRenderNonOverworldySkies() {
        if (client.world!=null && client.world.getDimensionEffects().getSkyType() == DimensionEffects.SkyType.END) {
            renderSkybox(new Identifier("textures/environment/end_sky.png"), 16.0f, 40, 255);
            return true;
        }
        if (options.endSkyLike()) {
            handleSkyBackground();
            return true;
        }
        return client.world != null && client.world.getDimensionEffects().getSkyType() != DimensionEffects.SkyType.NORMAL;
    }

    public void setupOverworldySky() {
        BackgroundRenderer.setFogBlack();
        RenderSystem.depthMask(false);
        handleSkyBackground();
        handleFog();
        matrices.push();
    }
    public void handleSkyBackground() {
        String skyType = options.getSkyType();
        if (skyType.equals("rainbow")) {
            renderRainbowBackground();
        }
        else {
            Vec3d vec3d = this.world.getSkyColor(client.gameRenderer.getCamera().getPos(), tickDelta);
            renderSingleColorBackground((float)vec3d.x, (float)vec3d.y, (float)vec3d.z, 1.0f);
        }
    }
    public void renderRainbowBackground() {
        float main = world.getSkyAngle(tickDelta) * 2;
        int color = Color.getHSBColor(main - (int)main, 1.0f, 1.0f).getRGB();
        float f = MathHelper.clamp(MathHelper.cos(world.getSkyAngle(tickDelta) * ((float)Math.PI * 2)) * 2.0f + 0.5f, 0.0f, 1.0f);
        renderSingleColorBackground(f * (float)(color >> 16 & 0xFF) / 255.0f, f * (float)(color >> 8 & 0xFF) / 255.0f, f * (float)(color & 0xFF) / 255.0f, 1.0f);
    }
    public void renderSingleColorBackground(float f, float g, float h, float a) {
        RenderSystem.setShaderColor(f, g, h, a);
        lightSkyBuffer.bind();
        lightSkyBuffer.draw(matrices.peek().getPositionMatrix(), projectionMatrix, RenderSystem.getShader());
        VertexBuffer.unbind();
    }
    
    public void handleFog() {
        RenderSystem.enableBlend();
        float[] fs = this.world.getDimensionEffects().getFogColorOverride(this.world.getSkyAngle(tickDelta), tickDelta);
        if (fs != null && options().hasDawn()) handleSunriseFog(fs);
        RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE, GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ZERO);
    }
    public void handleSunriseFog(float[] fs) {
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

        matrices.push();
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90.0f));
        float i = MathHelper.sin(this.world.getSkyAngleRadians(tickDelta)) < 0.0f ? 180.0f : 0.0f;
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(i - options.getSolarTilt()));

        Matrix4f matrix4f = matrices.peek().getPositionMatrix();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        bufferBuilder.begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
        bufferBuilder.vertex(matrix4f, 0.0f, 100.0f, 0.0f).color(fs[0], fs[1], fs[2], fs[3]);
        for (int n = 0; n <= 16; ++n) {
            float o = (float)n * ((float)Math.PI * 2) / 16.0f;
            float p = MathHelper.sin(o);
            float q = MathHelper.cos(o);
            bufferBuilder.vertex(matrix4f, p * 120.0f, q * 120.0f, -q * 40.0f * fs[3]).color(fs[0], fs[1], fs[2], 0.0f);
        }
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
        matrices.pop();
    }

    public void renderAllCelestialBodies(Runnable fogCallback) {
        float rain_alpha = 1.0f - this.world.getRainGradient(tickDelta);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, rain_alpha);

        renderSun();
        for (int i = 0; i < options.getNumMoons(); i++) {
            renderMoon(i);
        }
        renderStars(fogCallback, rain_alpha);
    }

    public void finish() {
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
        matrices.pop();
        RenderSystem.depthMask(true);
    }

    public void rotate_with_velocity(float v, float offset) {
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees((world.getSkyAngle(tickDelta) + offset) * 360.0f * v));
    }
    
    public void renderSun() {
        renderSingleBody(
                options.getSolarSize(),
                options.getSolarTilt(),
                0,
                1,
                0,
                options.getSolarTint(),
                options.getSolarTexture(),
                true);
    }
    public void renderMoon(int i) {
        renderSingleBody(
                options.getLunarSize(i),
                options.getLunarTiltY(i),
                options.getLunarTiltZ(i),
                options.getLunarVelocity(i),
                options.getLunarOffset(i),
                options.getLunarTint(i),
                options.getLunarTexture(i),
                false);
    }
    
    public void renderSingleBody(float size, float tilt_y, float tilt_z, float velocity, float offset, Vector3f tint, Identifier texture,
                                           boolean sun) {
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(tilt_y));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(tilt_z));
        rotate_with_velocity(velocity, offset);
        if (sun) renderSun(matrices.peek().getPositionMatrix(), texture, size, 100.0f, tint);
        else renderMoon(matrices.peek().getPositionMatrix(), texture, size, -100.0f, tint);
        rotate_with_velocity(-1 * velocity, offset);
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-tilt_z));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-tilt_y));
    }
    public void renderSun(Matrix4f matrix4f2, Identifier texture, float k, float y, Vector3f tint) {
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.setShaderColor(tint.x, tint.y, tint.z, 1.0f);
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        bufferBuilder.vertex(matrix4f2, -k, y, -k).texture(0.0f, 0.0f);
        bufferBuilder.vertex(matrix4f2, k, y, -k).texture(1.0f, 0.0f);
        bufferBuilder.vertex(matrix4f2, k, y, k).texture(1.0f, 1.0f);
        bufferBuilder.vertex(matrix4f2, -k, y, k).texture(0.0f, 1.0f);
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
    }
    public void renderMoon(Matrix4f matrix4f2, Identifier texture, float k, float y, Vector3f tint) {
        float t, q, p, o;
        if (!options.isMoonCustom()) {
            int moon_phase = world.getMoonPhase();
            int s = moon_phase % 4;
            int m = moon_phase / 4 % 2;
            t = (float)(s) / 4.0f;
            o = (float)(m) / 2.0f;
            p = (float)(s + 1) / 4.0f;
            q = (float)(m + 1) / 2.0f;
        }
        else {
            t = q = 1.0f;
            p = o = 0.0f;
        }
        renderMoon(matrix4f2, texture, k, y, tint, t, q, p, o);
    }
    public void renderMoon(Matrix4f matrix4f2, Identifier texture, float k, float y, Vector3f tint, float t, float q, float p, float o) {
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.setShaderColor(tint.x, tint.y, tint.z, 1.0f);
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        bufferBuilder.vertex(matrix4f2, -k, y, k).texture(p, q);
        bufferBuilder.vertex(matrix4f2, k, y, k).texture(t, q);
        bufferBuilder.vertex(matrix4f2, k, y, -k).texture(t, o);
        bufferBuilder.vertex(matrix4f2, -k, y, -k).texture(p, o);
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
    }

    public void renderStars(Runnable fogCallback, float rain_alpha) {
        renderStars(options.getStellarTiltY(),
                options.getStellarTiltZ(),
                options.getStellarVelocity(),
                0,
                fogCallback, rain_alpha);
    }
    public void renderStars(float tilt_y, float tilt_z, float velocity, float offset,
                                      Runnable fogCallback, float rain_alpha) {
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(tilt_y));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(tilt_z));
        rotate_with_velocity(velocity, offset);
        renderStars(matrices.peek().getPositionMatrix(), projectionMatrix, fogCallback, rain_alpha);
        rotate_with_velocity(-1 * velocity, offset);
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-tilt_z));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-tilt_y));
    }
    public void renderStars(Matrix4f matrix4f2, Matrix4f projectionMatrix, Runnable fogCallback, float rain_alpha) {
        float u = world.method_23787(tickDelta) * rain_alpha;
        Vector3f color = options.getStellarColor();
        if (u > 0.0f) {
            RenderSystem.setShaderColor(u*color.x, u*color.y, u*color.z, u);
            BackgroundRenderer.clearFog();
            starsBuffer.bind();
            starsBuffer.draw(matrix4f2, projectionMatrix, GameRenderer.getPositionProgram());
            VertexBuffer.unbind();
            fogCallback.run();
        }
    }
    public float getStarBrightness(float tickDelta) {
        float f = world.getSkyAngle(tickDelta);
        float g = 1.0F - (MathHelper.cos(f * (float) (Math.PI * 2)) * 2.0F + 0.25F);
        g = MathHelper.clamp(g, 0.0f, 1.0f);
        float day = options.getDayStarBrightness();
        float night = options.getNightStarBrightness();
        return day + g*g*(night-day);
    }

    public static BuiltBuffer buildStarsBuffer(Tessellator tessellator, InfinityOptions options) {
        Random random = Random.create(10842L);
        int num_stars = options.getNumStars();
        float distance_to_stars = 100.0F;
        BufferBuilder bufferBuilder = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);
        for (int star = 0; star < num_stars; star++) {
            float star_x = random.nextFloat() * 2.0F - 1.0F;
            float star_y = random.nextFloat() * 2.0F - 1.0F;
            float star_z = random.nextFloat() * 2.0F - 1.0F;
            float star_size = options.getStarSizeBase() + random.nextFloat() * options.getStarSizeModifier();
            float m = MathHelper.magnitude(star_x, star_y, star_z);
            if (!(m <= 0.010000001F) && !(m >= 1.0F)) {
                Vector3f star_coords = new Vector3f(star_x, star_y, star_z).normalize(distance_to_stars);
                float rotation_angle = (float)(random.nextDouble() * (float) Math.PI * 2.0);
                Quaternionf quaternionf = new Quaternionf().rotateTo(new Vector3f(0.0F, 0.0F, -1.0F), star_coords).rotateZ(rotation_angle);
                bufferBuilder.vertex(star_coords.add(new Vector3f(star_size, -star_size, 0.0F).rotate(quaternionf)));
                bufferBuilder.vertex(star_coords.add(new Vector3f(star_size, star_size, 0.0F).rotate(quaternionf)));
                bufferBuilder.vertex(star_coords.add(new Vector3f(-star_size, star_size, 0.0F).rotate(quaternionf)));
                bufferBuilder.vertex(star_coords.add(new Vector3f(-star_size, -star_size, 0.0F).rotate(quaternionf)));
            }
        }
        return bufferBuilder.end();
    }
    
    public void renderSkybox(Identifier texture, float copies, int brightness, int alpha) {
        renderSkybox(texture, copies, brightness, brightness, brightness, alpha);
    }
    public void renderSkybox(Identifier texture, float copies, int r, int g, int b, int alpha) {
        RenderSystem.enableBlend();
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getPositionTexColorProgram);
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
            BufferBuilder bufferBuilder = tessellator.getBuffer();
            bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
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
}
