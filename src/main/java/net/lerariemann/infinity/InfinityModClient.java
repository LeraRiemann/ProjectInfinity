package net.lerariemann.infinity;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.lerariemann.infinity.block.ModBlocks;
import net.lerariemann.infinity.block.entity.NeitherPortalBlockEntity;
import net.lerariemann.infinity.options.PacketTransiever;
import net.lerariemann.infinity.entity.ModEntities;
import net.lerariemann.infinity.loading.DimensionGrabber;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.noise.DoublePerlinNoiseSampler;
import net.minecraft.util.math.random.CheckedRandom;

import java.util.ArrayList;
import java.util.List;

public class InfinityModClient implements ClientModInitializer {
    final static DoublePerlinNoiseSampler sampler = DoublePerlinNoiseSampler.create(new CheckedRandom(0L), -3, 1.0, 1.0, 1.0, 0.0);

    double sample(int x, int y, int z) {
        return sampler.sample(x, y, z);
    }

    int posToColor(BlockPos pos) {
        double r = sample(pos.getX(), pos.getY() - 10000, pos.getZ());
        double g = sample(pos.getX(), pos.getY(), pos.getZ());
        double b = sample(pos.getX(), pos.getY() + 10000, pos.getZ());
        return (int)(256 * ((r + 1)/2)) + 256*((int)(256 * ((g + 1)/2)) + 256*(int)(256 * ((b + 1)/2)));
    }
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
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.BOOK_BOX, RenderLayer.getCutoutMipped());
        ColorProviderRegistry.BLOCK.register((state, world, pos, tintIndex) -> {
            if (pos != null) {
                return posToColor(pos);
            }
            return 16777215;
        }, ModBlocks.BOOK_BOX);
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
        ModEntities.registerEntityRenderers();
    }
}
