package net.lerariemann.infinity.neoforge.client;

import dev.architectury.platform.Platform;
import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.InfinityModClient;
import net.lerariemann.infinity.PlatformMethods;
import net.lerariemann.infinity.block.ModBlocks;
import net.lerariemann.infinity.compat.neoforge.ModConfigFactory;
import net.lerariemann.infinity.iridescence.Iridescence;
import net.lerariemann.infinity.item.ModItemFunctions;
import net.lerariemann.infinity.item.ModItems;
import net.lerariemann.infinity.fluids.FluidTypes;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import org.jetbrains.annotations.NotNull;

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
        event.register(PlatformMethods::getBiomeBottleColor, ModBlocks.BIOME_BOTTLE.get());
    }
    @SubscribeEvent
    public static void registerItemColorHandlers(RegisterColorHandlersEvent.Item event) {
        event.register(PlatformMethods::getOverlayColorFromComponents, ModItems.TRANSFINITE_KEY.get());
        event.register(PlatformMethods::getOverlayColorFromComponents, ModItems.BIOME_BOTTLE_ITEM.get());
    }
    @SubscribeEvent
    public static void registerModelPredicates(FMLClientSetupEvent event) {
        ModItemFunctions.registerModelPredicates();
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        RenderLayers.setRenderLayer(PlatformMethods.getIridescenceStill().get(), RenderLayer.getTranslucent());
        RenderLayers.setRenderLayer(PlatformMethods.getIridescenceFlowing().get(), RenderLayer.getTranslucent());
    }

    @EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, modid = InfinityMod.MOD_ID)
    public static class FluidClientHandler {
        @SubscribeEvent
        static void onRegisterClientExtensions(RegisterClientExtensionsEvent event) {
            event.registerFluidType(new IClientFluidTypeExtensions() {
                private static final Identifier IRIDESCENCE = InfinityMod.getId("block/iridescence");

                @Override
                public @NotNull Identifier getStillTexture() {
                    return IRIDESCENCE;
                }

                @Override
                public @NotNull Identifier getFlowingTexture() {
                    return IRIDESCENCE;
                }

                @Override
                public int getTintColor(@NotNull FluidState state, @NotNull BlockRenderView getter, @NotNull BlockPos pos) {
                    return Iridescence.color(pos);
                }

            }, FluidTypes.IRIDESCENCE_TYPE.value());
        }
    }

    private static boolean clothConfigInstalled() {
        return Platform.isModLoaded("cloth_config");
    }
}