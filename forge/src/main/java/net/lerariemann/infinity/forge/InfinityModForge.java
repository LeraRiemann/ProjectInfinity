package net.lerariemann.infinity.forge;

import dev.architectury.platform.Platform;
import dev.architectury.platform.forge.EventBuses;
import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.access.MobEntityAccess;
import net.lerariemann.infinity.compat.CreateCompat;
import net.lerariemann.infinity.compat.forge.CanaryCompat;
import net.lerariemann.infinity.compat.forge.RadiumCompat;
import net.lerariemann.infinity.fluids.forge.FluidTypes;
import net.lerariemann.infinity.fluids.forge.ModEffectsForge;
import net.lerariemann.infinity.fluids.forge.ModFluidsForge;
import net.lerariemann.infinity.forge.client.InfinityModForgeClient;
import net.lerariemann.infinity.registry.core.*;
import net.lerariemann.infinity.registry.var.ModStats;
import net.lerariemann.infinity.registry.var.ModTags;
import net.lerariemann.infinity.util.InfinityMethods;
import net.minecraft.registry.tag.ItemTags;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLDedicatedServerSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;

@Mod(InfinityMod.MOD_ID)
public final class InfinityModForge {
    public InfinityModForge() {
        // Register compat file ASAP to prevent a Canary crash.
        if (Platform.isModLoaded("canary"))
            CanaryCompat.writeCompatFile();
        // Register compat file ASAP to prevent a Radium crash.
        if (Platform.isModLoaded("radium"))
            RadiumCompat.writeCompatFile();
        // Submit our event bus to let Architectury API register our content on the right time.
        EventBuses.registerModEventBus(InfinityMod.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        // Run our common setup.
        InfinityMod.init();
        // Run our client setup.
        if (FMLEnvironment.dist == Dist.CLIENT)
            InfinityModForgeClient.initializeClient(eventBus);
        // Run any remaining Forge specific tasks.
        eventBus.addListener(InfinityModForge::registerSpawns);
        eventBus.addListener(InfinityModForge::commonSetup);
        eventBus.addListener(FluidTypes::registerFluidInteractions);
        MinecraftForge.EVENT_BUS.addListener(InfinityModForge::sliderSpamFix);
        
        FluidTypes.registerFluidTypes(eventBus);
        ModFluidsForge.registerModFluids();
        ModEffectsForge.register(eventBus);
        ModTags.IRIDESCENT_ITEMS = ItemTags.create(InfinityMethods.getId("iridescent"));
    }

    @SubscribeEvent
    public static void registerSpawns(FMLDedicatedServerSetupEvent event) {
        ModEntities.registerSpawnRestrictions();
    }

    @SubscribeEvent
    public static void sliderSpamFix(MobSpawnEvent event) {
        if (InfinityMethods.isBiomeInfinity(event.getLevel(), event.getEntity().getBlockPos())) {
            ((MobEntityAccess)event.getEntity()).infinity$setPersistent(false);
        }
    }

    @SubscribeEvent
    public static void commonSetup(FMLCommonSetupEvent event) {
        ModStats.load();
        ModStatusEffects.IRIDESCENT_EFFECT = ModEffectsForge.IRIDESCENT_EFFECT.getHolder().get();
        ModStatusEffects.IRIDESCENT_SETUP = ModEffectsForge.IRIDESCENT_SETUP.getHolder().get();
        ModStatusEffects.IRIDESCENT_COOLDOWN = ModEffectsForge.IRIDESCENT_COOLDOWN.getHolder().get();
        ModStatusEffects.AFTERGLOW = ModEffectsForge.AFTERGLOW.getHolder().get();
        ModBlocks.registerFlammableBlocks();
        ModItemFunctions.registerDispenserBehaviour();
        if (Platform.isModLoaded("create"))
            CreateCompat.register();
    }
}
