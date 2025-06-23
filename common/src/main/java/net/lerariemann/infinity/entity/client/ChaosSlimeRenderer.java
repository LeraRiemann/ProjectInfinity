package net.lerariemann.infinity.entity.client;

import net.lerariemann.infinity.entity.client.state.ChaosSlimeRenderState;
import net.lerariemann.infinity.entity.custom.ChaosSlime;
import net.lerariemann.infinity.util.InfinityMethods;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.SlimeEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public class ChaosSlimeRenderer extends MobEntityRenderer<ChaosSlime, ChaosSlimeRenderState, SlimeEntityModel> {
    private static final Identifier TEXTURE = InfinityMethods.getId("textures/entity/slime.png");

    public ChaosSlimeRenderer(EntityRendererFactory.Context context) {
        super(context, new SlimeEntityModel(context.getPart(EntityModelLayers.SLIME)), 0.25f);
        this.addFeature(new DimensionalSlimeCoreRenderer(this, context.getBlockRenderManager()));
        this.addFeature(new TintedLayerRenderer<>(context, new SlimeEntityModel(context.getModelLoader().getModelPart(EntityModelLayers.SLIME_OUTER))));
    }

    @Override
    public ChaosSlimeRenderState createRenderState() {
        return new ChaosSlimeRenderState();
    }

    @Override
    public Identifier getTexture(ChaosSlimeRenderState state) {
        return TEXTURE;
    }

    public void render(ChaosSlimeRenderState slimeEntity, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
        this.shadowRadius = 0.25F * slimeEntity.size;
        super.render(slimeEntity, matrixStack, vertexConsumerProvider, i);
    }

    protected void scale(ChaosSlimeRenderState slimeEntity, MatrixStack matrixStack, float f) {
        float g = 0.999f;
        matrixStack.scale(g, g, g);
        matrixStack.translate(0.0f, 0.001f, 0.0f);
        float h = slimeEntity.size;
        float i = slimeEntity.stretch / (g * 0.5F + 1.0F);
        float j = 1.0f / (i + 1.0f);
        matrixStack.scale(j * h, 1.0f / j * h, j * h);
    }
}
