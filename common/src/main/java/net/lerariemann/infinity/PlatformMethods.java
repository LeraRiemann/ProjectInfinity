package net.lerariemann.infinity;

import com.google.common.collect.ImmutableSet;
import dev.architectury.injectables.annotations.ExpectPlatform;
import dev.architectury.registry.registries.RegistrySupplier;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.lerariemann.infinity.block.entity.NeitherPortalBlockEntity;
import net.lerariemann.infinity.var.ModComponentTypes;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.poi.PointOfInterestType;

import static net.lerariemann.infinity.InfinityModClient.sampler;

//Abstraction layer for classes from Fabric API and Forgified Fabric API, as well as Fabric Loader vs. NeoForge Loader.
public class PlatformMethods {
    @ExpectPlatform
    public static boolean isModLoaded(String modID) {
        throw new AssertionError();
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

    static int posToColor(BlockPos pos) {
        double r = sample(pos.getX(), pos.getY() - 10000, pos.getZ());
        double g = sample(pos.getX(), pos.getY(), pos.getZ());
        double b = sample(pos.getX(), pos.getY() + 10000, pos.getZ());
        return (int)(256 * ((r + 1)/2)) + 256*((int)(256 * ((g + 1)/2)) + 256*(int)(256 * ((b + 1)/2)));
    }

    public static int getBookBoxColour(BlockState state, BlockRenderView world, BlockPos pos, int tintIndex) {
            if (pos != null) {
                return posToColor(pos);
            }
            return 16777215;
    }

    public static int getKeyColor(ItemStack stack, int layer) {
        Identifier dim = stack.getComponents().get(ModComponentTypes.KEY_DESTINATION.get());
        Integer color = stack.getComponents().get(ModComponentTypes.KEY_COLOR.get());
        if (layer == 3) {
            if (dim != null && dim.toString().equals("minecraft:random")) return 0;
            if (color == null || color == 0) return ColorHelper.Argb.fullAlpha(0xFFFFFF);
            return 0;
        }
        if (layer == 2) {
            if (dim != null && dim.toString().equals("minecraft:random")) return ColorHelper.Argb.fullAlpha(0xFFFFFF);
            return 0;
        }
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

    @ExpectPlatform
    public static void addAfter(RegistrySupplier<Item> blockItem, RegistryKey<ItemGroup> group, Item item) {
        throw new AssertionError();
    }
}
