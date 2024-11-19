package net.lerariemann.infinity.forge.client;

import dev.architectury.platform.Platform;
import net.lerariemann.infinity.InfinityModClient;
import net.lerariemann.infinity.PlatformMethods;
import net.lerariemann.infinity.block.ModBlocks;
import net.lerariemann.infinity.config.forge.ModConfigFactory;
import net.lerariemann.infinity.item.ModItems;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class InfinityModForgeClient {

    public static void initializeClient(IEventBus eventBus) {
        InfinityModClient.initializeClient();
        net.lerariemann.infinity.forge.client.InfinityModForgeClient.registerModsPage();
        eventBus.addListener(net.lerariemann.infinity.forge.client.InfinityModForgeClient::registerBlockColorHandlers);
        eventBus.addListener(net.lerariemann.infinity.forge.client.InfinityModForgeClient::registerItemColorHandlers);
        eventBus.addListener(net.lerariemann.infinity.forge.client.InfinityModForgeClient::registerModelPredicates);
    }

    //Integrate Cloth Config screen (if mod present) with NeoForge mod menu.
    public static void registerModsPage() {
        if (clothConfigInstalled()) ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class, () -> new ConfigScreenHandler.ConfigScreenFactory(ModConfigFactory::createScreen));
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