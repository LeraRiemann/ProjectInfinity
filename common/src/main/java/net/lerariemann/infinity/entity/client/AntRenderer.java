package net.lerariemann.infinity.entity.client;

import net.lerariemann.infinity.entity.custom.AntEntity;
import net.lerariemann.infinity.util.InfinityMethods;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class AntRenderer extends MobEntityRenderer<AntEntity, AntModel> {
    private static final Identifier TEXTURE = InfinityMethods.getId("textures/entity/ant.png");

    public AntRenderer(EntityRendererFactory.Context context) {
        super(context, new AntModel(context.getPart(AntModel.MODEL_LAYER)), 0.6f);
    }

    @Override
    public Identifier getTexture(AntEntity entity) {
        return TEXTURE;
    }

    @Override
    public void render(AntEntity mobEntity, float f, float g, MatrixStack matrixStack,
                       VertexConsumerProvider vertexConsumerProvider, int i) {
        if(mobEntity.isBaby()) {
            matrixStack.scale(0.5f, 0.5f, 0.5f);
        } else {
            matrixStack.scale(1f, 1f, 1f);
        }

        super.render(mobEntity, f, g, matrixStack, vertexConsumerProvider, i);
    }
}
