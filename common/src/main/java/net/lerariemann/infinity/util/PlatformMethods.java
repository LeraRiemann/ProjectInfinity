package net.lerariemann.infinity.util;

import com.google.common.collect.ImmutableSet;
import dev.architectury.injectables.annotations.ExpectPlatform;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FluidBlock;
import net.minecraft.entity.Entity;
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

/**
 * Methods that are dependent on ExpectPlatform, and require a different implementation to work on both Fabric and NeoForge.
 */
public class PlatformMethods {

    /**
     * Creates an empty PacketByteBuf - intended to replace the method
     * in Fabric API.
     */
    @ExpectPlatform
    public static PacketByteBuf createPacketByteBufs() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static <E> PointOfInterestType registerPoi(Identifier id, int i, int i1, ImmutableSet<E> es) {
        throw new AssertionError();
    }

    /**
     * On NeoForge, this allows the world to be ticked.
     * On Fabric, this invokes a server world event in Fabric API.
     */
    @ExpectPlatform
    public static void onWorldLoad(Object mixin, ServerWorld world) {
        throw new AssertionError();
    }

    /**
     * Unfreeze the registry while the game is running.
     * This is through DynReg on Fabric and a NeoForge exclusive method.
     */
    @ExpectPlatform
    public static void unfreeze(Registry<?> registry) {
        throw new AssertionError();
    }

    /**
     * Try and add an item to an Item Group using Fabric API.
     */
    @ExpectPlatform
    public static void addAfter(RegistrySupplier<Item> blockItem, RegistryKey<ItemGroup> group, Item item) {
        throw new AssertionError();
    }

    /**
     * Check if a block is in the Black Dyed Conventional Tag.
     */
    @ExpectPlatform
    public static boolean isInBlack(BlockState state) {
        throw new AssertionError();
    }

    /**
     * Check if a block is in the White Dyed Conventional Tag.
     */
    @ExpectPlatform
    public static boolean isInWhite(BlockState state) {
        throw new AssertionError();
    }

    /**
     * Get the still variant of Iridescence fluid.
     */
    @ExpectPlatform
    public static RegistrySupplier<? extends FlowableFluid> getIridescenceStill() {
        throw new AssertionError();
    }

    /**
     * Get the flowing variant of Iridescence fluid.
     */
    @ExpectPlatform
    public static RegistrySupplier<? extends FlowableFluid> getIridescenceFlowing() {
        throw new AssertionError();
    }

    /**
     * Get the registry supplier for Iridescence fluid.
     */
    @ExpectPlatform
    public static RegistrySupplier<FluidBlock> getIridBlockForReg() {
        throw new AssertionError();
    }

    /**
     * Read from the config path inside the mod JAR.
     */
    @ExpectPlatform
    public static Path getRootConfigPath() {
        throw new AssertionError();
    }

    /**
     * Create an Item Tag.
     */
    @ExpectPlatform
    public static TagKey<Item> createItemTag(String id) {
        throw new AssertionError();
    }

    /**
     * Register a Flammable Block (how does Architectury API not have a helper for this)
     */
    @ExpectPlatform
    public static void registerFlammableBlock(RegistrySupplier<Block> block, int burn, int spread) {
        throw new AssertionError();
    }

    /**
     * Neoforge-exclusive method of testing if a mob is located in iridescence as far as fluid types are concerned.
     * Used only in mixins, to fix the neoforge loader stripping mobs of ability to swim in non-water fluids.
     * On Fabric, returns false.
     */
    @ExpectPlatform
    public static boolean acidTest(Entity entity, boolean eyes) {
        throw new AssertionError();
    }
    @ExpectPlatform
    public static double acidHeightTest(Entity entity) {
        throw new AssertionError();
    }
}
