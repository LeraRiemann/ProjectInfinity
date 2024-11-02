package net.lerariemann.infinity;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.lerariemann.infinity.entity.ModEntities;
import net.lerariemann.infinity.loading.DimensionGrabber;
import net.lerariemann.infinity.options.PacketTransiever;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.noise.DoublePerlinNoiseSampler;
import net.minecraft.util.math.random.CheckedRandom;

import java.util.ArrayList;
import java.util.List;


public class InfinityModClient {
    public final static DoublePerlinNoiseSampler sampler = DoublePerlinNoiseSampler.create(new CheckedRandom(0L), -3, 1.0, 1.0, 1.0, 0.0);

    public static void initializeClient() {
        ModEntities.registerEntityRenderers();
        ClientPlayNetworking.registerGlobalReceiver(InfinityMod.WORLD_ADD, (client, handler, buf, responseSender) -> {
            Identifier id = buf.readIdentifier();
            NbtCompound optiondata = buf.readNbt();
            int i = buf.readInt();
            List<Identifier> biomeids = new ArrayList<>();
            List<NbtCompound> biomes = new ArrayList<>();
            for (int j = 0; j < i; j++) {
                biomeids.add(buf.readIdentifier());
                biomes.add(buf.readNbt());
            }
            client.execute(() -> (new DimensionGrabber(client.getNetworkHandler().getRegistryManager())).grab_for_client(id, optiondata, biomeids, biomes));
        });
        ClientPlayNetworking.registerGlobalReceiver(InfinityMod.SHADER_RELOAD, PacketTransiever::receive);
        ClientPlayNetworking.registerGlobalReceiver(InfinityMod.STARS_RELOAD, PacketTransiever::receiveStars);
    }
}