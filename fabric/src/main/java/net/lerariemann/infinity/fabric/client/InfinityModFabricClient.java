package net.lerariemann.infinity.fabric.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.lerariemann.infinity.InfinityModClient;
import net.lerariemann.infinity.PlatformMethods;
import net.lerariemann.infinity.block.ModBlocks;
import net.lerariemann.infinity.var.ModItems;
import net.minecraft.client.render.RenderLayer;

public class InfinityModFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Apply colour handlers to tint Neither Portals and Book Boxes.
        ColorProviderRegistry.BLOCK.register(PlatformMethods::getNeitherPortalColour, ModBlocks.NEITHER_PORTAL.get());
        ColorProviderRegistry.BLOCK.register(PlatformMethods::getBookBoxColour, ModBlocks.BOOK_BOX.get());
        ColorProviderRegistry.ITEM.register(PlatformMethods::getKeyColor, ModItems.TRANSFINITE_KEY.get());
        // On Fabric, render layer maps are also applied to Book Boxes and Time Bombs.
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.BOOK_BOX.get(), RenderLayer.getCutoutMipped());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.TIME_BOMB.get(), RenderLayer.getTranslucent());
        InfinityModClient.initializeClient();
    }
}