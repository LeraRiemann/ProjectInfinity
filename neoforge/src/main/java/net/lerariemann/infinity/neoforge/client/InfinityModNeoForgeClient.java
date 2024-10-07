package net.lerariemann.infinity.neoforge.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.lerariemann.infinity.InfinityModClient;
import net.lerariemann.infinity.block.ModBlocks;
import net.lerariemann.infinity.block.entity.NeitherPortalBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.math.BlockPos;

import static net.lerariemann.infinity.InfinityModClient.sampler;

public class InfinityModNeoForgeClient implements ClientModInitializer {

    double sample(int x, int y, int z) {
        return sampler.sample(x, y, z);
    }

    int posToColor(BlockPos pos) {
        double r = sample(pos.getX(), pos.getY() - 10000, pos.getZ());
        double g = sample(pos.getX(), pos.getY(), pos.getZ());
        double b = sample(pos.getX(), pos.getY() + 10000, pos.getZ());
        return (int)(256 * ((r + 1)/2)) + 256*((int)(256 * ((g + 1)/2)) + 256*(int)(256 * ((b + 1)/2)));
    }
    @Override
    public void onInitializeClient() {
//        ColorProviderRegistry.BLOCK.register((state, world, pos, tintIndex) -> {
//            if (world != null && pos != null) {
//                BlockEntity blockEntity = world.getBlockEntity(pos);
//                if (blockEntity instanceof NeitherPortalBlockEntity) {
//                    Object j = ((NeitherPortalBlockEntity) blockEntity).getRenderData();
//                    if (j == null) return 0;
//                    return (int)j & 0xFFFFFF;
//                }
//            }
//            return 16777215;
//        }, ModBlocks.NEITHER_PORTAL);
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.BOOK_BOX.get(), RenderLayer.getCutoutMipped());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.TIME_BOMB.get(), RenderLayer.getTranslucent());
        ColorProviderRegistry.BLOCK.register((state, world, pos, tintIndex) -> {
            if (pos != null) {
                return posToColor(pos);
            }
            return 16777215;
        }, ModBlocks.BOOK_BOX.get());
        InfinityModClient.init();
    }
}