package net.lerariemann.infinity.entity.client;

import net.lerariemann.infinity.entity.client.state.ChaosPawnRenderState;
import net.lerariemann.infinity.entity.client.state.ChaosSlimeRenderState;
import net.lerariemann.infinity.entity.custom.ChaosPawn;
import net.lerariemann.infinity.entity.custom.TintableEntity;
import net.lerariemann.infinity.util.InfinityMethods;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;

import java.awt.Color;

public class ChaosPawnTint extends FeatureRenderer<ChaosPawnRenderState, BipedEntityModel<ChaosPawnRenderState>> {
    private final BipedEntityModel<ChaosPawnRenderState> model;
    public ChaosPawnTint(FeatureRendererContext<ChaosPawnRenderState, BipedEntityModel<ChaosPawnRenderState>> context, BipedEntityModel<ChaosPawnRenderState> model) {
        super(context);
        this.model = model;
    }

    public void renderOneLayer(MatrixStack matrixStack, VertexConsumer vertexConsumer, int light, int overlay, ChaosPawnRenderState livingEntity, ModelPart part, String name) {
        int color;
        color = livingEntity.colors.getInt(name);
        if (livingEntity.customName != null) {
            String s = livingEntity.customName.toString();
             if ("jeb_".equals(s)) {
                 color = TintableEntity.getColorJeb((int) (livingEntity.age + (name.equals("hat") ? 200 : 0)), livingEntity.id);
            }
            if ("hue".equals(s)) {
                int n = (int) (livingEntity.age + (name.equals("hat") ? 200 : 0) + livingEntity.id);
                float hue = n/400.f;
                hue = hue - (int)hue;
                color = Color.getHSBColor(hue, 1.0f, 1.0f).getRGB();
            }
        }
        else {
            color = ColorHelper.fullAlpha(color);
        }
        part.render(matrixStack, vertexConsumer, light, overlay, color);
    }


    @Override
    public void render(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int light, ChaosPawnRenderState state, float limbAngle, float limbDistance) {
//        public void render(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, ChaosPawn e, float f, float g) {
//    public void render(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, ChaosPawn e, float f, float g, float h, float j, float k, float l) {
        boolean bl = state.hasOutline && state.invisible;
        if (state.invisible && !bl) {
            return;
        }
        Identifier texture = InfinityMethods.getId("textures/entity/empty.png");
        VertexConsumer vertexConsumer = bl ? vertexConsumerProvider.getBuffer(RenderLayer.getOutline(texture)) : vertexConsumerProvider.getBuffer(RenderLayer.getEntityTranslucent(texture));
        // TODO 1.21.3 these were probably necessary
        //        (this.getContextModel()).copyStateTo(this.model);
        //        this.model.animateModel(e, f, g, h);
        this.model.setAngles(state);
        int o = LivingEntityRenderer.getOverlay(state, 0.0f);
        renderOneLayer(matrixStack, vertexConsumer, light, o, state, model.body, "body");
        renderOneLayer(matrixStack, vertexConsumer, light, o, state, model.head, "head");
        renderOneLayer(matrixStack, vertexConsumer, light, o, state, model.hat, "hat");
        renderOneLayer(matrixStack, vertexConsumer, light, o, state, model.leftArm, "left_arm");
        renderOneLayer(matrixStack, vertexConsumer, light, o, state, model.rightArm, "right_arm");
        renderOneLayer(matrixStack, vertexConsumer, light, o, state, model.leftLeg, "left_leg");
        renderOneLayer(matrixStack, vertexConsumer, light, o, state, model.rightLeg, "right_leg");
    }
}
