package net.lerariemann.infinity.entity.client;

import net.lerariemann.infinity.entity.custom.TintableEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.mob.MobEntity;
import org.joml.Vector3f;

public class TintedLayerRenderer<T extends MobEntity, S extends EntityModel<T>> extends FeatureRenderer<T, S> {
    private final S model;

    public TintedLayerRenderer(FeatureRendererContext<T, S> context, S model) {
        super(context);
        this.model = model;
    }

    @Override
    public void render(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, T livingEntity, float f, float g, float h, float j, float k, float l) {
        boolean bl = MinecraftClient.getInstance().hasOutline(livingEntity) && livingEntity.isInvisible();
        if (livingEntity.isInvisible() && !bl) {
            return;
        }
        VertexConsumer vertexConsumer = bl ? vertexConsumerProvider.getBuffer(RenderLayer.getOutline(this.getTexture(livingEntity))) : vertexConsumerProvider.getBuffer(RenderLayer.getEntityTranslucent(this.getTexture(livingEntity)));
        (this.getContextModel()).copyStateTo(this.model);
        this.model.animateModel(livingEntity, f, g, h);
        this.model.setAngles(livingEntity, f, g, j, k, l);
        Vector3f color = new Vector3f(1.0f, 1.0f, 1.0f);
        float alpha = 1.0f;
        if (livingEntity instanceof TintableEntity) {
            color = ((TintableEntity)livingEntity).getColor();
            alpha = ((TintableEntity)livingEntity).getAlpha();
        }
        this.model.render(matrixStack, vertexConsumer, i, LivingEntityRenderer.getOverlay(livingEntity, 0.0f), color.x, color.y, color.z, alpha);
    }
}