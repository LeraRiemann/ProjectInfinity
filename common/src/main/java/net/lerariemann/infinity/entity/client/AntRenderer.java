package net.lerariemann.infinity.entity.client;

import net.lerariemann.infinity.entity.client.state.AntEntityRenderState;
import net.lerariemann.infinity.entity.custom.AntEntity;
import net.lerariemann.infinity.util.InfinityMethods;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class AntRenderer extends MobEntityRenderer<AntEntity, AntEntityRenderState, AntModel> {
    private static final Identifier TEXTURE = InfinityMethods.getId("textures/entity/ant.png");

    public AntRenderer(EntityRendererFactory.Context context) {
        super(context, new AntModel(context.getPart(AntModel.MODEL_LAYER)), 0.6f);
    }

    @Override
    public AntEntityRenderState createRenderState() {
        return new AntEntityRenderState();
    }

    @Override
    public Identifier getTexture(AntEntityRenderState state) {
        return TEXTURE;
    }

    public void render(AntEntityRenderState mobEntity, MatrixStack matrixStack,
                       VertexConsumerProvider vertexConsumerProvider, int i) {
        if(mobEntity.baby) {
            matrixStack.scale(0.5f, 0.5f, 0.5f);
        } else {
            matrixStack.scale(1f, 1f, 1f);
        }

        super.render(mobEntity, matrixStack, vertexConsumerProvider, i);
    }


}
