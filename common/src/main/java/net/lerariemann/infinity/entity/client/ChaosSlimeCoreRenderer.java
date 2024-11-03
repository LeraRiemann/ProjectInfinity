package net.lerariemann.infinity.entity.client;

import net.lerariemann.infinity.entity.custom.ChaosSlime;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.SlimeEntityModel;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.math.MatrixStack;

public class ChaosSlimeCoreRenderer extends FeatureRenderer<ChaosSlime, SlimeEntityModel<ChaosSlime>> {
    private final BlockRenderManager blockRenderManager;

    public ChaosSlimeCoreRenderer(FeatureRendererContext<ChaosSlime, SlimeEntityModel<ChaosSlime>> context, BlockRenderManager blockRenderManager) {
        super(context);
        this.blockRenderManager = blockRenderManager;
    }
    @Override
    public void render(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, ChaosSlime slime, float f, float g, float h, float j, float k, float l) {
        boolean bl = MinecraftClient.getInstance().hasOutline(slime) && slime.isInvisible();
        if (slime.isInvisible() && !bl) {
            return;
        }
        BlockState blockState = slime.getCore();
        int m = LivingEntityRenderer.getOverlay(slime, 0.0f);
        matrixStack.push();
        matrixStack.scale(0.25f, 0.25f, 0.25f);
        matrixStack.translate(-0.5f, 4.5f, -0.5f);
        this.renderCore(matrixStack, vertexConsumerProvider, i, blockState, m);
        matrixStack.pop();
    }

    private void renderCore(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, BlockState coreState, int overlay) {
        BakedModel coreModel = this.blockRenderManager.getModel(coreState);
        this.blockRenderManager.getModelRenderer().render(matrices.peek(), vertexConsumers.getBuffer(RenderLayers.getBlockLayer(coreState)), coreState, coreModel, 1.0f, 1.0f, 1.0f, light, overlay);
    }
}
