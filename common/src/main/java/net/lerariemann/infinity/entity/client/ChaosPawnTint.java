package net.lerariemann.infinity.entity.client;

import net.lerariemann.infinity.entity.custom.ChaosPawn;
import net.lerariemann.infinity.entity.custom.TintableEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.ColorHelper;

import java.awt.Color;

public class ChaosPawnTint extends FeatureRenderer<ChaosPawn, BipedEntityModel<ChaosPawn>> {
    private final BipedEntityModel<ChaosPawn> model;
    public ChaosPawnTint(ChaosPawnRenderer context, BipedEntityModel<ChaosPawn> model) {
        super(context);
        this.model = model;
    }

    public void renderOneLayer(MatrixStack matrixStack, VertexConsumer vertexConsumer, int light, int overlay, ChaosPawn livingEntity, ModelPart part, String name) {
        int color;
        color = livingEntity.getColors().getInt(name);
        if (livingEntity.hasCustomName()) {
            String s = livingEntity.getName().getString();
             if ("jeb_".equals(s)) {
                 color = TintableEntity.getColorJeb(livingEntity.age + (name.equals("hat") ? 200 : 0), livingEntity.getId());
            }
            if ("hue".equals(s)) {
                int n = livingEntity.age + (name.equals("hat") ? 200 : 0) + livingEntity.getId();
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
    public void render(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, ChaosPawn e, float f, float g) {
//    public void render(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, ChaosPawn e, float f, float g, float h, float j, float k, float l) {
        boolean bl = MinecraftClient.getInstance().hasOutline(e) && e.isInvisible();
        if (e.isInvisible() && !bl) {
            return;
        }
        VertexConsumer vertexConsumer = bl ? vertexConsumerProvider.getBuffer(RenderLayer.getOutline(this.getTexture(e))) : vertexConsumerProvider.getBuffer(RenderLayer.getEntityTranslucent(this.getTexture(e)));
        (this.getContextModel()).copyStateTo(this.model);
        this.model.animateModel(e, f, g, h);
        this.model.setAngles(e, f, g, j, k, l);
        int o = LivingEntityRenderer.getOverlay(e, 0.0f);
        renderOneLayer(matrixStack, vertexConsumer, i, o, e, model.body, "body");
        renderOneLayer(matrixStack, vertexConsumer, i, o, e, model.head, "head");
        renderOneLayer(matrixStack, vertexConsumer, i, o, e, model.hat, "hat");
        renderOneLayer(matrixStack, vertexConsumer, i, o, e, model.leftArm, "left_arm");
        renderOneLayer(matrixStack, vertexConsumer, i, o, e, model.rightArm, "right_arm");
        renderOneLayer(matrixStack, vertexConsumer, i, o, e, model.leftLeg, "left_leg");
        renderOneLayer(matrixStack, vertexConsumer, i, o, e, model.rightLeg, "right_leg");
    }


}
