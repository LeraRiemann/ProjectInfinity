package net.lerariemann.infinity.registry.var.neoforge;

import net.lerariemann.infinity.registry.var.ModPayloads;
import net.lerariemann.infinity.util.config.SoundScanner;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import static net.lerariemann.infinity.registry.var.ModPayloads.*;

public class ModPayloadsNeoforge {
    public static MinecraftClient client() {
        return MinecraftClient.getInstance();
    }
    public static MinecraftServer getServer(IPayloadContext context) {
        return ((ServerPlayerEntity)context.player()).server;
    }

    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(
                ModPayloads.WorldAddS2CPayload.ID,
                ModPayloads.WorldAddS2CPayload.CODEC,
                (payload, context) -> receiveWorldAddPayload(client(), payload.world_id(), payload.world_data())
        );
        registrar.playToServer(
                ModPayloads.BiomeAddS2CPayload.ID,
                ModPayloads.BiomeAddS2CPayload.CODEC,
                (payload, context) -> receiveBiomeAddPayload(client(), payload.biome_id(), payload.biome_data())
        );
        registrar.playToServer(
                ModPayloads.ShaderS2CPayload.ID,
                ModPayloads.ShaderS2CPayload.CODEC,
                (payload, context) -> receiveShaderPayload(client(), payload.shader_data(), payload.iridescence())
        );
        registrar.playToServer(
                ModPayloads.StarsS2CPayload.ID,
                ModPayloads.StarsS2CPayload.CODEC,
                (payload, context) -> receiveStarsPayload(client())
        );
        registrar.playToServer(
                ModPayloads.SoundPackS2CPayload.ID,
                ModPayloads.SoundPackS2CPayload.CODEC,
                (payload, context) -> SoundScanner.unpackDownloadedPack(payload.songIds(), client())
        );
        registrar.playToClient(
                ModPayloads.F4UpdateC2SPayload.ID,
                ModPayloads.F4UpdateC2SPayload.CODEC,
                (payload, context) -> receiveF4UpdatePayload((ServerPlayerEntity)context.player(), payload.slot(), payload.width(), payload.height())
        );
        registrar.playToClient(
                ModPayloads.F4DeployC2SPayload.ID,
                ModPayloads.F4DeployC2SPayload.CODEC,
                (payload, context) -> receiveF4DeployPayload((ServerPlayerEntity)context.player())
        );
        registrar.playToClient(
                ModPayloads.JukeboxesC2SPayload.ID,
                ModPayloads.JukeboxesC2SPayload.CODEC,
                (payload, context) -> SoundScanner.unpackUploadedJukeboxes(getServer(context), payload.data())
        );
    }
}
