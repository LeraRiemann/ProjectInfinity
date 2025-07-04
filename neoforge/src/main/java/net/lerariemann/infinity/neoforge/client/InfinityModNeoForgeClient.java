package net.lerariemann.infinity.neoforge.client;

import dev.architectury.platform.Platform;
import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.InfinityModClient;
import net.lerariemann.infinity.registry.var.ModScreenHandlers;
import net.lerariemann.infinity.util.PlatformMethods;
import net.lerariemann.infinity.registry.core.ModBlocks;
import net.lerariemann.infinity.compat.neoforge.ModConfigFactory;
import net.lerariemann.infinity.iridescence.Iridescence;
import net.lerariemann.infinity.registry.core.ModItemFunctions;
import net.lerariemann.infinity.registry.core.ModItems;
import net.lerariemann.infinity.fluids.neoforge.FluidTypes;
import net.lerariemann.infinity.util.InfinityMethods;
import net.lerariemann.infinity.util.screen.F4Screen;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.world.BlockRenderView;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
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
        eventBus.addListener(InfinityModNeoForgeClient::registerFluidRenderLayers);
        eventBus.addListener(InfinityModNeoForgeClient::registerModelPredicates);
        eventBus.addListener(InfinityModNeoForgeClient::registerMenuScreens);
    }

    //Integrate Cloth Config screen (if mod present) with NeoForge mod menu.
    public static void registerModsPage() {
        if (clothConfigInstalled()) ModLoadingContext.get().registerExtensionPoint(IConfigScreenFactory.class, ModConfigFactory::new);
    }

    // Apply colour handlers to tint Neither Portals and Book Boxes.
    @SubscribeEvent
    public static void registerBlockColorHandlers(RegisterColorHandlersEvent.Block event) {
        event.register(InfinityMethods::getBlockEntityColor, ModBlocks.PORTAL.get(),
                ModBlocks.BIOME_BOTTLE.get(),
                ModBlocks.CHROMATIC_WOOL.get(),
                ModBlocks.CHROMATIC_CARPET.get());
        event.register(InfinityMethods::getBookBoxColor, ModBlocks.BOOK_BOX.get());
    }
    @SubscribeEvent
    public static void registerItemColorHandlers(RegisterColorHandlersEvent.Item event) {
        event.register(InfinityMethods::getOverlayColorFromComponents,
                ModItems.TRANSFINITE_KEY.get(),
                ModItems.BIOME_BOTTLE_ITEM.get(),
                ModItems.F4.get());
        event.register(InfinityMethods::getItemColorFromComponents,
                ModItems.CHROMATIC_WOOL.get(),
                ModItems.CHROMATIC_CARPET.get(),
                ModItems.CHROMATIC_MATTER.get(),
                ModItems.PORTAL_ITEM.get());
        event.register(InfinityMethods::getDiscColorFromComponents,
                ModItems.DISC.get());
    }
    @SubscribeEvent
    public static void registerMenuScreens(RegisterMenuScreensEvent event) {
        event.register(ModScreenHandlers.F4.get(), F4Screen::new);
    }
    @SubscribeEvent
    public static void registerModelPredicates(FMLClientSetupEvent event) {
        ModItemFunctions.registerModelPredicates();
    }

    @SubscribeEvent
    public static void registerFluidRenderLayers(FMLClientSetupEvent event) {
        RenderLayers.setRenderLayer(PlatformMethods.getIridescenceStill().get(), RenderLayer.getTranslucent());
        RenderLayers.setRenderLayer(PlatformMethods.getIridescenceFlowing().get(), RenderLayer.getTranslucent());
    }

    @EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, modid = InfinityMod.MOD_ID)
    public static class FluidClientHandler {
        @SubscribeEvent
        static void onRegisterClientExtensions(RegisterClientExtensionsEvent event) {
            event.registerFluidType(new IClientFluidTypeExtensions() {
                @Override
                public @NotNull Identifier getStillTexture() {
                    return Iridescence.TEXTURE;
                }
                @Override
                public @NotNull Identifier getFlowingTexture() {
                    return Iridescence.FLOWING_TEXTURE;
                }
                @Override
                public @NotNull Identifier getOverlayTexture() {
                    return Iridescence.OVERLAY_TEXTURE;
                }

                @Override
                public int getTintColor() {
                    return ColorHelper.Argb.fullAlpha(Iridescence.getTimeBasedColor());
                }
                @Override
                public int getTintColor(@NotNull FluidState state, @NotNull BlockRenderView getter, @NotNull BlockPos pos) {
                    return ColorHelper.Argb.fullAlpha(Iridescence.getPosBasedColor(pos));
                }

            }, FluidTypes.IRIDESCENCE_TYPE.value());
        }
    }

    private static boolean clothConfigInstalled() {
        return Platform.isModLoaded("cloth_config");
    }
}