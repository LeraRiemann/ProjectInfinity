package net.lerariemann.infinity.entity.client;

import net.lerariemann.infinity.entity.custom.BishopEntity;
import net.lerariemann.infinity.util.InfinityMethods;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.BipedEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class BishopRenderer extends BipedEntityRenderer<BishopEntity, BishopModel> {
    private static final Identifier TEXTURE = InfinityMethods.getId("textures/entity/bishop.png");

    public BishopRenderer(EntityRendererFactory.Context context) {
        super(context, new BishopModel(context.getPart(BishopModel.MODEL_LAYER)), 0.6f);
        this.addFeature(new ArmorFeatureRenderer<>(this,
                new BishopModel(context.getPart(EntityModelLayers.SKELETON_INNER_ARMOR)),
                new BishopModel(context.getPart(EntityModelLayers.SKELETON_OUTER_ARMOR)),
                context.getModelManager()));
    }

    @Override
    public Identifier getTexture(BishopEntity entity) {
        return TEXTURE;
    }

    @Override
    public void render(BishopEntity mobEntity, float f, float g, MatrixStack matrixStack,
                       VertexConsumerProvider vertexConsumerProvider, int i) {
        super.render(mobEntity, f, g, matrixStack, vertexConsumerProvider, i);
    }
}
