package net.lerariemann.infinity.neoforge;

import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.registry.core.ModBlocks;
import net.lerariemann.infinity.registry.core.ModEntities;
import net.lerariemann.infinity.fluids.neoforge.FluidTypes;
import net.lerariemann.infinity.fluids.neoforge.ModEffectsNeoforge;
import net.lerariemann.infinity.fluids.neoforge.ModFluidsNeoforge;
import net.lerariemann.infinity.registry.core.ModItemFunctions;
import net.lerariemann.infinity.neoforge.client.InfinityModNeoForgeClient;
import net.lerariemann.infinity.registry.var.ModPayloads;
import net.lerariemann.infinity.registry.var.ModStats;
import net.lerariemann.infinity.util.config.SoundScanner;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.network.ServerPlayerEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import static net.lerariemann.infinity.registry.var.ModPayloads.*;
import static net.lerariemann.infinity.registry.var.ModPayloads.receiveF4DeployPayload;

@Mod(InfinityMod.MOD_ID)
public final class InfinityModNeoForge {
    public InfinityModNeoForge(IEventBus eventBus, ModContainer container) {
        // Run our common setup.
        InfinityMod.init();
        // Run our client setup.
        if (FMLEnvironment.dist == Dist.CLIENT) InfinityModNeoForgeClient.initializeClient(eventBus);
        // Run any remaining tasks that require waiting for the registry to freeze on NeoForge.
        eventBus.addListener(InfinityModNeoForge::registerSpawns);
        eventBus.addListener(InfinityModNeoForge::commonSetup);
        eventBus.addListener(FluidTypes::registerFluidInteractions);
        FluidTypes.registerFluidTypes(eventBus);
        ModFluidsNeoforge.registerModFluids();
        ModEffectsNeoforge.register(eventBus);
    }

    @SubscribeEvent
    public static void registerSpawns(RegisterSpawnPlacementsEvent event) {
        ModEntities.registerSpawnRestrictions();
    }

    @SubscribeEvent
    public static void commonSetup(FMLCommonSetupEvent event) {
        ModStats.load();
        ModBlocks.registerFlammableBlocks();
        ModItemFunctions.registerDispenserBehaviour();
    }

    public static MinecraftClient client() {
        return MinecraftClient.getInstance();
    }
    public static ServerPlayerEntity getPlayer(IPayloadContext context) {
        return ((ServerPlayerEntity)context.player());
    }

    @SubscribeEvent
    public static void registerPayloads(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(
                ModPayloads.WorldAddS2CPayload.ID,
                ModPayloads.WorldAddS2CPayload.CODEC,
                (payload, context) -> receiveWorldAddPayload(client(), payload.world_id(), payload.world_data())
        );
        registrar.playToClient(
                ModPayloads.BiomeAddS2CPayload.ID,
                ModPayloads.BiomeAddS2CPayload.CODEC,
                (payload, context) -> receiveBiomeAddPayload(client(), payload.biome_id(), payload.biome_data())
        );
        registrar.playToClient(
                ModPayloads.ShaderS2CPayload.ID,
                ModPayloads.ShaderS2CPayload.CODEC,
                (payload, context) -> receiveShaderPayload(client(), payload.shader_data(), payload.iridescence())
        );
        registrar.playToClient(
                ModPayloads.StarsS2CPayload.ID,
                ModPayloads.StarsS2CPayload.CODEC,
                (payload, context) -> receiveStarsPayload(client())
        );
        registrar.playToClient(
                ModPayloads.SoundPackS2CPayload.ID,
                ModPayloads.SoundPackS2CPayload.CODEC,
                (payload, context) -> SoundScanner.unpackDownloadedPack(payload.songIds(), client())
        );
        registrar.playToServer(
                ModPayloads.F4UpdateC2SPayload.ID,
                ModPayloads.F4UpdateC2SPayload.CODEC,
                (payload, context) -> receiveF4UpdatePayload(getPlayer(context), payload.slot(), payload.width(), payload.height())
        );
        registrar.playToServer(
                ModPayloads.F4DeployC2SPayload.ID,
                ModPayloads.F4DeployC2SPayload.CODEC,
                (payload, context) -> receiveF4DeployPayload(getPlayer(context))
        );
        registrar.playToServer(
                ModPayloads.JukeboxesC2SPayload.ID,
                ModPayloads.JukeboxesC2SPayload.CODEC,
                (payload, context) -> SoundScanner.unpackUploadedJukeboxes(getPlayer(context).server, payload.data())
        );
    }
}
