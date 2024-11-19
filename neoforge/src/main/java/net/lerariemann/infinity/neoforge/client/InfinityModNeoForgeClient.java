package net.lerariemann.infinity.neoforge.client;

import dev.architectury.platform.Platform;
import net.lerariemann.infinity.InfinityModClient;
import net.lerariemann.infinity.PlatformMethods;
import net.lerariemann.infinity.block.ModBlocks;
import net.lerariemann.infinity.config.neoforge.ModConfigFactory;
import net.lerariemann.infinity.fluid.ModFluids;
import net.lerariemann.infinity.item.ModItems;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;

public class InfinityModNeoForgeClient {

    public static void initializeClient(IEventBus eventBus) {
        InfinityModClient.initializeClient();
        InfinityModNeoForgeClient.registerModsPage();
        eventBus.addListener(InfinityModNeoForgeClient::registerBlockColorHandlers);
        eventBus.addListener(InfinityModNeoForgeClient::registerItemColorHandlers);
        eventBus.addListener(InfinityModNeoForgeClient::registerModelPredicates);
    }

    //Integrate Cloth Config screen (if mod present) with NeoForge mod menu.
    public static void registerModsPage() {
        if (clothConfigInstalled()) ModLoadingContext.get().registerExtensionPoint(IConfigScreenFactory.class, ModConfigFactory::new);
    }

    // Apply colour handlers to tint Neither Portals and Book Boxes.
    @SubscribeEvent
    public static void registerBlockColorHandlers(RegisterColorHandlersEvent.Block event) {
        event.register(PlatformMethods::getNeitherPortalColour, ModBlocks.NEITHER_PORTAL.get());
        event.register(PlatformMethods::getBookBoxColour, ModBlocks.BOOK_BOX.get());
        event.register(PlatformMethods::getBookBoxColour, ModBlocks.IRIDESCENCE.get());
    }
    @SubscribeEvent
    public static void registerItemColorHandlers(RegisterColorHandlersEvent.Item event) {
        event.register(PlatformMethods::getKeyColor, ModItems.TRANSFINITE_KEY.get());
    }
    @SubscribeEvent
    public static void registerModelPredicates(FMLClientSetupEvent event) {
        ModItems.registerModelPredicates();
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        RenderLayers.setRenderLayer(ModFluids.IRIDESCENCE_STILL.get(), RenderLayer.getTranslucent());
        RenderLayers.setRenderLayer(ModFluids.IRIDESCENCE_FLOWING.get(), RenderLayer.getTranslucent());
    }

    private static boolean clothConfigInstalled() {
        return Platform.isModLoaded("cloth_config");
    }
}