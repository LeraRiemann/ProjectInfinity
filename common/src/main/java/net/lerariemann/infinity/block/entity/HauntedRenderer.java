package net.lerariemann.infinity.block.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.random.Random;

@Environment(EnvType.CLIENT)
public class HauntedRenderer implements BlockEntityRenderer<HauntedBlockEntity> {
    BlockRenderManager manager;

    public HauntedRenderer(BlockEntityRendererFactory.Context ctx) {
        manager = ctx.getRenderManager();
    }

    @Override
    public void render(HauntedBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        BlockState bs = entity.original;
        if (bs.getRenderType() != BlockRenderType.INVISIBLE)
            manager.renderBlock(bs, entity.getPos(), entity.getWorld(), matrices,
                vertexConsumers.getBuffer(RenderLayers.getBlockLayer(bs)), true, Random.create());
    }
}
