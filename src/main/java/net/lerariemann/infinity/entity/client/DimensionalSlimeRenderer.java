package net.lerariemann.infinity.entity.client;

import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.entity.custom.DimensionalSlime;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.SlimeEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public class DimensionalSlimeRenderer extends MobEntityRenderer<DimensionalSlime, SlimeEntityModel<DimensionalSlime>> {
    private static final Identifier TEXTURE = new Identifier(InfinityMod.MOD_ID, "textures/entity/slime.png");

    public DimensionalSlimeRenderer(EntityRendererFactory.Context context) {
        super(context, new SlimeEntityModel<>(context.getPart(EntityModelLayers.SLIME)), 0.25f);
        this.addFeature(new DimensionalSlimeCoreRenderer(this, context.getBlockRenderManager()));
        this.addFeature(new DimensionalSlimeOutlineRenderer(this, context.getModelLoader()));
    }
    @Override
    public Identifier getTexture(DimensionalSlime slimeEntity) {
        return TEXTURE;
    }
    @Override
    public void render(DimensionalSlime slimeEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
        this.shadowRadius = 0.25f * (float)slimeEntity.getSize();
        super.render(slimeEntity, f, g, matrixStack, vertexConsumerProvider, i);
    }
    @Override
    protected void scale(DimensionalSlime slimeEntity, MatrixStack matrixStack, float f) {
        float g = 0.999f;
        matrixStack.scale(g, g, g);
        matrixStack.translate(0.0f, 0.001f, 0.0f);
        float h = slimeEntity.getSize();
        float i = MathHelper.lerp(f, slimeEntity.lastStretch, slimeEntity.stretch) / (h * 0.5f + 1.0f);
        float j = 1.0f / (i + 1.0f);
        matrixStack.scale(j * h, 1.0f / j * h, j * h);
    }
}
