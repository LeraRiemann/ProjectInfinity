package net.lerariemann.infinity;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.lerariemann.infinity.block.ModBlocks;
import net.lerariemann.infinity.block.entity.NeitherPortalBlockEntity;
import net.lerariemann.infinity.loading.DimensionGrabber;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;

import java.util.ArrayList;
import java.util.List;

public class InfinityModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ColorProviderRegistry.BLOCK.register((state, world, pos, tintIndex) -> {
            if (world != null && pos != null) {
                BlockEntity blockEntity = world.getBlockEntity(pos);
                if (blockEntity instanceof NeitherPortalBlockEntity) {
                    Object j = ((NeitherPortalBlockEntity)blockEntity).getRenderAttachmentData();
                    if (j == null) return 0;
                    return (int)j & 0xFFFFFF;
                }
            }
            return 16777215;
        }, ModBlocks.NEITHER_PORTAL);
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

            client.execute(() -> {
                LogManager.getLogger().info("Packet received");
                (new DimensionGrabber(client.getNetworkHandler().getRegistryManager())).grab_for_client(id, optiondata, biomeids, biomes);
            });
        });
    }
}
