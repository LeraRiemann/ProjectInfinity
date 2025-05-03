package net.lerariemann.infinity.registry.var.fabric;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.lerariemann.infinity.registry.var.ModPayloads;
import net.lerariemann.infinity.util.config.SoundScanner;
import net.minecraft.client.MinecraftClient;

import static net.lerariemann.infinity.registry.var.ModPayloads.*;

/**
 * See {@link ModPayloads} for usages.
 */
@SuppressWarnings("unused")
public class ModPayloadsImpl {
    public static MinecraftClient client(Object context) {
        ClientPlayNetworking.Context clientContext = (ClientPlayNetworking.Context) context;
        return clientContext.client();
    }

    public static void registerPayloadsServer() {
        PayloadTypeRegistry.playS2C().register(WorldAddS2CPayload.ID, WorldAddS2CPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(BiomeAddS2CPayload.ID, BiomeAddS2CPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(ShaderS2CPayload.ID, ShaderS2CPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(StarsS2CPayload.ID, StarsS2CPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(SoundPackS2CPayload.ID, SoundPackS2CPayload.CODEC);

        PayloadTypeRegistry.playC2S().register(F4UpdateC2SPayload.ID, F4UpdateC2SPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(F4DeployC2SPayload.ID, F4DeployC2SPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(JukeboxesC2SPayload.ID, JukeboxesC2SPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(F4UpdateC2SPayload.ID, (payload, context) ->
                receiveF4UpdatePayload(context.player(), payload.slot(), payload.width(), payload.height()));
        ServerPlayNetworking.registerGlobalReceiver(F4DeployC2SPayload.ID, (payload, context) ->
                receiveF4DeployPayload(context.player()));
        ServerPlayNetworking.registerGlobalReceiver(JukeboxesC2SPayload.ID, (payload, context) ->
                SoundScanner.unpackUploadedJukeboxes(context.server(), payload.data()));
    }

    public static void registerPayloadsClient() {
        ClientPlayNetworking.registerGlobalReceiver(WorldAddS2CPayload.ID, (payload, context) ->
                receiveWorldAddPayload(client(context), payload.world_id(), payload.world_data()));
        ClientPlayNetworking.registerGlobalReceiver(BiomeAddS2CPayload.ID, (payload, context) ->
                receiveBiomeAddPayload(client(context), payload.biome_id(), payload.biome_data()));
        ClientPlayNetworking.registerGlobalReceiver(ShaderS2CPayload.ID, (payload, context) ->
                receiveShaderPayload(client(context), payload.shader_data(), payload.iridescence()));
        ClientPlayNetworking.registerGlobalReceiver(StarsS2CPayload.ID, (payload, context) ->
                receiveStarsPayload(client(context)));
        ClientPlayNetworking.registerGlobalReceiver(SoundPackS2CPayload.ID, (payload, context) ->
                SoundScanner.unpackDownloadedPack(payload.songIds(), client(context)));
    }
}
