package net.lerariemann.infinity.var.neoforge;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.lerariemann.infinity.var.ModPayloads;
import net.minecraft.client.MinecraftClient;
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
        PayloadTypeRegistry.playS2C().register(ModPayloads.WorldAddPayload.ID, ModPayloads.WorldAddPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(ModPayloads.BiomeAddPayload.ID, ModPayloads.BiomeAddPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(ModPayloads.ShaderRePayload.ID, ModPayloads.ShaderRePayload.CODEC);
        PayloadTypeRegistry.playS2C().register(ModPayloads.StarsRePayLoad.ID, ModPayloads.StarsRePayLoad.CODEC);
    }
    public static void registerPayloadsClient() {
        ClientPlayNetworking.registerGlobalReceiver(ModPayloads.WorldAddPayload.ID, ModPayloads::addWorld);
        ClientPlayNetworking.registerGlobalReceiver(ModPayloads.BiomeAddPayload.ID, ModPayloads::addBiome);
        ClientPlayNetworking.registerGlobalReceiver(ModPayloads.ShaderRePayload.ID, ModPayloads::receiveShader);
        ClientPlayNetworking.registerGlobalReceiver(ModPayloads.StarsRePayLoad.ID, ModPayloads::receiveStars);
    }
}
