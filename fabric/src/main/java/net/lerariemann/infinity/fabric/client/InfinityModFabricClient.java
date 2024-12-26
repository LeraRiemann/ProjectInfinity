package net.lerariemann.infinity.fabric.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.lerariemann.infinity.InfinityModClient;
import net.lerariemann.infinity.util.PlatformMethods;
import net.lerariemann.infinity.registry.core.ModBlocks;
import net.lerariemann.infinity.registry.core.ModItemFunctions;
import net.lerariemann.infinity.registry.core.ModItems;
import net.lerariemann.infinity.util.InfinityMethods;
import net.minecraft.client.render.RenderLayer;

public class InfinityModFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Apply colour handlers to tint blocks.
        ColorProviderRegistry.BLOCK.register(InfinityMethods::getBookBoxColor,
                ModBlocks.BOOK_BOX.get());
        ColorProviderRegistry.BLOCK.register(InfinityMethods::getBlockEntityColor,
                ModBlocks.PORTAL.get(),
                ModBlocks.BIOME_BOTTLE.get(),
                ModBlocks.CHROMATIC_WOOL.get(),
                ModBlocks.CHROMATIC_CARPET.get());
        ColorProviderRegistry.ITEM.register(InfinityMethods::getOverlayColorFromComponents,
                ModItems.TRANSFINITE_KEY.get(),
                ModItems.BIOME_BOTTLE_ITEM.get(),
                ModItems.F4.get());
        ColorProviderRegistry.ITEM.register(InfinityMethods::getItemColorFromComponents,
                ModItems.CHROMATIC_WOOL.get(),
                ModItems.CHROMATIC_CARPET.get());
        ColorProviderRegistry.ITEM.register(InfinityMethods::getBlockEntityColor, ModItems.PORTAL_ITEM.get());
        // On Fabric, render layer maps are also applied to blocks.
        BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getCutoutMipped(),
                ModBlocks.BOOK_BOX.get(),
                ModBlocks.IRIDESCENT_KELP.get(),
                ModBlocks.IRIDESCENT_KELP_PLANT.get());
        BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getTranslucent(),
                ModBlocks.TIME_BOMB.get(),
                ModBlocks.BIOME_BOTTLE.get());
        // Render layer maps are also applied to fluids.
        BlockRenderLayerMap.INSTANCE.putFluids(RenderLayer.getTranslucent(),
                PlatformMethods.getIridescenceStill().get(),
                PlatformMethods.getIridescenceFlowing().get());
        // Common client setup tasks.
        InfinityModClient.initializeClient();
        // Register model predicates for Transfinite Keys
        ModItemFunctions.registerModelPredicates();
    }
}