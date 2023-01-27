package net.lerariemann.infinity;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.lerariemann.infinity.block.ModBlocks;
import net.lerariemann.infinity.block.entity.NeitherPortalBlockEntity;
import net.minecraft.block.entity.BlockEntity;

public class InfinityModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ColorProviderRegistry.BLOCK.register((state, world, pos, tintIndex) -> {
            if (world != null && pos != null) {
                BlockEntity blockEntity = world.getBlockEntity(pos);
                if (blockEntity instanceof NeitherPortalBlockEntity) {
                    int j = ((NeitherPortalBlockEntity)blockEntity).getDimension();
                    return j & 0xFFFFFF;
                }
            }
            return 16777215;
        }, ModBlocks.NEITHER_PORTAL);
    }
}
