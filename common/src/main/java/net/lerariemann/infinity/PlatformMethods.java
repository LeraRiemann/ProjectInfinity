package net.lerariemann.infinity;

import com.google.common.collect.ImmutableSet;
import dev.architectury.injectables.annotations.ExpectPlatform;
import dev.architectury.platform.Platform;
import dev.architectury.registry.registries.RegistrySupplier;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.lerariemann.infinity.block.entity.NeitherPortalBlockEntity;
import net.lerariemann.infinity.item.ModComponentTypes;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.poi.PointOfInterestType;

import java.awt.*;
import java.util.List;

import static net.lerariemann.infinity.InfinityModClient.sampler;

//Abstraction layer for classes from Fabric API and Forgified Fabric API, as well as Fabric Loader vs. NeoForge Loader.
public class PlatformMethods {

    public static boolean isFabricApiLoaded(String modID) {
        if (Platform.isFabric()) return Platform.isModLoaded(modID.replace("_", "-"));
        else return Platform.isModLoaded(modID.replace("-", "_"));
    }

    public static void sendServerPlayerEntity(ServerPlayerEntity entity, CustomPayload payload) {
        ServerPlayNetworking.send(entity, payload);
    }

    @ExpectPlatform
    public static PacketByteBuf createPacketByteBufs() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static <E> PointOfInterestType registerPoi(Identifier id, int i, int i1, ImmutableSet<E> es) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void onWorldLoad(Object mixin, ServerWorld world) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void unfreeze(Registry<?> registry) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void freeze(Registry<?> registry) {
        throw new AssertionError();
    }

    static double sample(int x, int y, int z) {
        return sampler.sample(x, y, z);
    }

    public static int posToColor(BlockPos pos) {
        double r = sample(pos.getX(), pos.getY() - 10000, pos.getZ());
        double g = sample(pos.getX(), pos.getY(), pos.getZ());
        double b = sample(pos.getX(), pos.getY() + 10000, pos.getZ());
        return (int)(256 * ((r + 1)/2)) + 256*((int)(256 * ((g + 1)/2)) + 256*(int)(256 * ((b + 1)/2)));
    }

    public static int iridescentColor(BlockPos pos) {
        int i = pos.getX() + pos.getY() + pos.getZ();
        return Color.HSBtoRGB(i / 600.0f + (float)((Math.sin(pos.getX()/12.0f) + Math.sin(pos.getZ()/12.0f)) / 4), 1.0F, 1.0F);
    }

    public static int getBookBoxColour(BlockState state, BlockRenderView world, BlockPos pos, int tintIndex) {
            if (pos != null) {
                return posToColor(pos);
            }
            return 16777215;
    }

    public static int getKeyColor(ItemStack stack, int layer) {
        Integer color = stack.getComponents().get(ModComponentTypes.KEY_COLOR.get());
        if (layer == 1) {
            return (color == null) ? 0 : color;
        }
        return ColorHelper.Argb.fullAlpha(0xFFFFFF);
    }

    public static int getNeitherPortalColour(BlockState state, BlockRenderView world, BlockPos pos, int tintIndex) {
        if (world != null && pos != null) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof NeitherPortalBlockEntity be) {
                Object j = be.getRenderData();
                if (j == null) return 0;
                return (int)j & 0xFFFFFF;
            }
        }
        return 16777215;
    }

    static java.util.List<String> colors = List.of("minecraft:white_",
            "minecraft:red_",
            "minecraft:orange_",
            "minecraft:yellow_",
            "minecraft:lime_",
            "minecraft:green_",
            "minecraft:cyan_",
            "minecraft:light_blue_",
            "minecraft:blue_",
            "minecraft:purple_",
            "minecraft:magenta_",
            "minecraft:pink_",
            "minecraft:gray_",
            "minecraft:light_gray_",
            "minecraft:black_",
            "minecraft:brown_");

    public static Block getRandomColorBlock(WorldAccess world, String str) {
        return Registries.BLOCK.get(Identifier.of(colors.get(world.getRandom().nextInt(16)) + str));
    }
    public static Block getRandomColorBlock(double d, String str) {
        return Registries.BLOCK.get(Identifier.of(colors.get((int)(d*16)) + str));
    }

    @ExpectPlatform
    public static void addAfter(RegistrySupplier<Item> blockItem, RegistryKey<ItemGroup> group, Item item) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean isInBlack(BlockState state) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean isInWhite(BlockState state) {
        throw new AssertionError();
    }
}
