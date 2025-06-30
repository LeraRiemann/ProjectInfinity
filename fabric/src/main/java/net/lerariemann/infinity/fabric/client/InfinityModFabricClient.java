package net.lerariemann.infinity.fabric.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.lerariemann.infinity.InfinityModClient;
import net.lerariemann.infinity.fluids.fabric.ModFluidsFabric;
import net.lerariemann.infinity.registry.var.ModPayloads;
import net.lerariemann.infinity.util.PlatformMethods;
import net.lerariemann.infinity.registry.core.ModBlocks;
import net.lerariemann.infinity.registry.core.ModItemFunctions;
import net.lerariemann.infinity.registry.core.ModItems;
import net.lerariemann.infinity.util.InfinityMethods;
import net.minecraft.client.render.RenderLayer;

public class InfinityModFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Colour handlers
        ColorProviderRegistry.BLOCK.register(InfinityMethods::getBookBoxColor,
                ModBlocks.BOOK_BOX.get());
        ColorProviderRegistry.BLOCK.register(InfinityMethods::getBlockEntityColor,
                ModBlocks.PORTAL.get(),
                ModBlocks.BIOME_BOTTLE.get(),
                ModBlocks.CHROMATIC_WOOL.get(),
                ModBlocks.CHROMATIC_CARPET.get());

        // Render layer maps
        BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getCutoutMipped(),
                ModBlocks.BOOK_BOX.get(),
                ModBlocks.IRIDESCENT_KELP.get(),
                ModBlocks.IRIDESCENT_KELP_PLANT.get());
        BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getTranslucent(),
                ModBlocks.TIME_BOMB.get(),
                ModBlocks.BIOME_BOTTLE.get(),
                ModBlocks.CHROMATIC_WOOL.get(),
                ModBlocks.CHROMATIC_CARPET.get());
        BlockRenderLayerMap.INSTANCE.putFluids(RenderLayer.getTranslucent(),
                PlatformMethods.getIridescenceStill().get(),
                PlatformMethods.getIridescenceFlowing().get());

        // Common client setup tasks
        InfinityModClient.initializeClient();
        ModPayloads.registerPayloadsClient();
        ModItemFunctions.registerModelPredicates();

        //Fluid stuff
        FluidVariantAttributes.register(PlatformMethods.getIridescenceStill().get(), new ModFluidsFabric.IridescenceVariantAttributeHandler());
        FluidVariantAttributes.register(PlatformMethods.getIridescenceFlowing().get(), new ModFluidsFabric.IridescenceVariantAttributeHandler());
        FluidVariantRendering.register(PlatformMethods.getIridescenceStill().get(), new ModFluidsFabric.IridescenceVariantRenderHandler());
        FluidVariantRendering.register(PlatformMethods.getIridescenceFlowing().get(), new ModFluidsFabric.IridescenceVariantRenderHandler());
        FluidRenderHandlerRegistry.INSTANCE.register(PlatformMethods.getIridescenceStill().get(), new ModFluidsFabric.IridescenceRenderHandler());
        FluidRenderHandlerRegistry.INSTANCE.register(PlatformMethods.getIridescenceFlowing().get(), new ModFluidsFabric.IridescenceRenderHandler());
    }
}