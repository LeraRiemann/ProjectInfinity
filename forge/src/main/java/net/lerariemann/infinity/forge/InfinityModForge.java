package net.lerariemann.infinity.forge;

import dev.architectury.platform.Platform;
import dev.architectury.platform.forge.EventBuses;
import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.compat.forge.CanaryCompat;
import net.lerariemann.infinity.entity.ModEntities;
import net.lerariemann.infinity.fluids.forge.FluidTypes;
import net.lerariemann.infinity.fluids.forge.ModEffectsForge;
import net.lerariemann.infinity.fluids.forge.ModFluidsForge;
import net.lerariemann.infinity.forge.client.InfinityModForgeClient;
import net.lerariemann.infinity.iridescence.ModStatusEffects;
import net.lerariemann.infinity.item.ModItems;
import net.lerariemann.infinity.item.function.ModItemFunctions;
import net.lerariemann.infinity.var.ModStats;
import net.minecraft.registry.tag.ItemTags;
import net.minecraftforge.api.distmarker.Dist;
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
        
        FluidTypes.registerFluidTypes(eventBus);
        ModFluidsForge.registerModFluids(eventBus);
        ModEffectsForge.register(eventBus);
        ModItems.IRIDESCENT_TAG = ItemTags.create(InfinityMod.getId("iridescent"));
    }

    @SubscribeEvent
    public static void registerSpawns(FMLDedicatedServerSetupEvent event) {
        ModEntities.registerSpawnRestrictions();
    }

    @SubscribeEvent
    public static void commonSetup(FMLCommonSetupEvent event) {
        ModStats.load();
        ModStatusEffects.IRIDESCENT_EFFECT = ModEffectsForge.IRIDESCENT_EFFECT.getHolder().get();
        ModStatusEffects.IRIDESCENT_SETUP = ModEffectsForge.IRIDESCENT_SETUP.getHolder().get();
        ModStatusEffects.IRIDESCENT_COOLDOWN = ModEffectsForge.IRIDESCENT_COOLDOWN.getHolder().get();
        ModItemFunctions.registerDispenserBehaviour();
    }
}
