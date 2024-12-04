package net.lerariemann.infinity.util;

import com.google.common.collect.ImmutableSet;
import dev.architectury.injectables.annotations.ExpectPlatform;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.block.BlockState;
import net.minecraft.block.FluidBlock;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.poi.PointOfInterestType;

import java.nio.file.Path;

// Methods that are dependent on ExpectPlatform, and require a different implementation to work on both Fabric and NeoForge.
public class PlatformMethods {

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

    @ExpectPlatform
    public static RegistrySupplier<? extends FlowableFluid> getIridescenceStill() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static RegistrySupplier<? extends FlowableFluid> getIridescenceFlowing() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static RegistrySupplier<FluidBlock> getIridBlockForReg() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static Path getRootConfigPath() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static TagKey<Item> createItemTag(String id) {
        throw new AssertionError();
    }
}
