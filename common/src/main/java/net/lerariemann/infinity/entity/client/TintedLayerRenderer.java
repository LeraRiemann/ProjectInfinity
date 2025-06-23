package net.lerariemann.infinity.entity.client;

import net.lerariemann.infinity.entity.client.state.ChaosPawnRenderState;
import net.lerariemann.infinity.entity.client.state.ChaosSlimeRenderState;
import net.lerariemann.infinity.entity.custom.TintableEntity;
import net.lerariemann.infinity.util.InfinityMethods;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.Identifier;

public class TintedLayerRenderer<T extends LivingEntityRenderState, S extends EntityModel<T>> extends FeatureRenderer<T, S> {
    private final S model;

    public TintedLayerRenderer(FeatureRendererContext<T, S> context, S model) {
        super(context);
        this.model = model;
    }

//    public void render(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, T livingEntity, float f, float g, float h, float j, float k, float l) {
    @Override
    public void render(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int light, T livingEntity, float limbAngle, float limbDistance) {
        boolean bl = livingEntity.hasOutline && livingEntity.invisible;
        if (livingEntity.invisible && !bl) {
            return;
        }
        // TODO 1.21.3 find a better way of extracting a texture from a render state
        Identifier texture;
        if (livingEntity instanceof ChaosPawnRenderState) {
            texture = InfinityMethods.getId("textures/entity/empty.png");
        } else if (livingEntity instanceof ChaosSlimeRenderState) {
            texture = InfinityMethods.getId("textures/entity/slime.png");
        } else {
            texture = InfinityMethods.getId("textures/entity/empty.png");
        }
        VertexConsumer vertexConsumer = bl ? vertexConsumerProvider.getBuffer(RenderLayer.getOutline(texture)) : vertexConsumerProvider.getBuffer(RenderLayer.getEntityTranslucent(texture));
        // TODO 1.21.3 these were probably necessary
//        this.getContextModel().copyStateTo(this.model);
//        this.model.animateModel(livingEntity, f, g, h);
        this.model.setAngles(livingEntity);
        int color = 16777215;
        if (livingEntity instanceof TintableEntity ent) {
            color = ent.getColorForRender();
        }
        this.model.render(matrixStack, vertexConsumer, light, LivingEntityRenderer.getOverlay(livingEntity, 0.0f), color);
    }
}
