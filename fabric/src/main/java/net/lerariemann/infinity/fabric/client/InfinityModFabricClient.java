package net.lerariemann.infinity.fabric.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.lerariemann.infinity.InfinityModClient;
import net.lerariemann.infinity.PlatformMethods;
import net.lerariemann.infinity.block.ModBlocks;
import net.lerariemann.infinity.item.ModItemFunctions;
import net.lerariemann.infinity.item.ModItems;
import net.minecraft.client.render.RenderLayer;

public class InfinityModFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Apply colour handlers to tint Neither Portals and Book Boxes.
        ColorProviderRegistry.BLOCK.register(PlatformMethods::getNeitherPortalColour, ModBlocks.NEITHER_PORTAL.get());
        ColorProviderRegistry.BLOCK.register(PlatformMethods::getBookBoxColour, ModBlocks.BOOK_BOX.get());
        ColorProviderRegistry.BLOCK.register(PlatformMethods::getBookBoxColour, ModBlocks.IRIDESCENCE.get());
        ColorProviderRegistry.BLOCK.register(PlatformMethods::getBiomeBottleColor, ModBlocks.BIOME_BOTTLE.get());
        ColorProviderRegistry.ITEM.register(PlatformMethods::getOverlayColorFromComponents, ModItems.TRANSFINITE_KEY.get());
        ColorProviderRegistry.ITEM.register(PlatformMethods::getOverlayColorFromComponents, ModItems.BIOME_BOTTLE_ITEM.get());
        // On Fabric, render layer maps are also applied to blocks with cutouts.
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.BOOK_BOX.get(), RenderLayer.getCutoutMipped());
        // On Fabric, render layer maps are also applied to blocks with translucency.
        BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getTranslucent(), ModBlocks.TIME_BOMB.get(), ModBlocks.BIOME_BOTTLE.get());
        // Render layer maps are also applied to fluids.
        BlockRenderLayerMap.INSTANCE.putFluids(RenderLayer.getTranslucent(),
                PlatformMethods.getIridescenceStill().get(), PlatformMethods.getIridescenceFlowing().get());
        // Common client setup tasks.
        InfinityModClient.initializeClient();
        // Register model predicates for Transfinite Keys
        ModItemFunctions.registerModelPredicates();
    }
}