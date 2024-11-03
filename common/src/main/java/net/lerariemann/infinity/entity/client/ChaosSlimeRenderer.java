package net.lerariemann.infinity.entity.client;

import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.entity.custom.ChaosSlime;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.SlimeEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public class ChaosSlimeRenderer extends MobEntityRenderer<ChaosSlime, SlimeEntityModel<ChaosSlime>> {
    private static final Identifier TEXTURE = InfinityMod.getId("textures/entity/slime.png");

    public ChaosSlimeRenderer(EntityRendererFactory.Context context) {
        super(context, new SlimeEntityModel<>(context.getPart(EntityModelLayers.SLIME)), 0.25f);
        this.addFeature(new ChaosSlimeCoreRenderer(this, context.getBlockRenderManager()));
        this.addFeature(new TintedLayerRenderer<>(this, new SlimeEntityModel<>(context.getModelLoader().getModelPart(EntityModelLayers.SLIME_OUTER))));
    }
    @Override
    public Identifier getTexture(ChaosSlime slimeEntity) {
        return TEXTURE;
    }
    @Override
    public void render(ChaosSlime slimeEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
        this.shadowRadius = 0.25f * (float)slimeEntity.getSize();
        super.render(slimeEntity, f, g, matrixStack, vertexConsumerProvider, i);
    }
    @Override
    protected void scale(ChaosSlime slimeEntity, MatrixStack matrixStack, float f) {
        float g = 0.999f;
        matrixStack.scale(g, g, g);
        matrixStack.translate(0.0f, 0.001f, 0.0f);
        float h = slimeEntity.getSize();
        float i = MathHelper.lerp(f, slimeEntity.lastStretch, slimeEntity.stretch) / (h * 0.5f + 1.0f);
        float j = 1.0f / (i + 1.0f);
        matrixStack.scale(j * h, 1.0f / j * h, j * h);
    }
}
