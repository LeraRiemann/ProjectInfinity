package net.lerariemann.infinity.util;

import com.google.common.collect.ImmutableSet;
import dev.architectury.injectables.annotations.ExpectPlatform;
import dev.architectury.platform.Platform;
import dev.architectury.registry.registries.RegistrySupplier;
import net.lerariemann.infinity.block.entity.BiomeBottleBlockEntity;
import net.lerariemann.infinity.block.entity.InfinityPortalBlockEntity;
import net.lerariemann.infinity.item.StarOfLangItem;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FluidBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.poi.PointOfInterestType;

import java.nio.file.Path;
import java.util.function.Function;

import static net.lerariemann.infinity.InfinityModClient.sampler;

/**
 * Methods that require different implementations to work on Fabric vs. NeoForge and thus depend on {@link dev.architectury.injectables.annotations.ExpectPlatform}.
 * @see InfinityMethods
 */
public class PlatformMethods {

    public static boolean isFabricApiLoaded(String modID) {
        if (Platform.isFabric()) return Platform.isModLoaded(modID.replace("_", "-"));
        else return Platform.isModLoaded(modID.replace("-", "_"));
    }

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

    public static int getBookBoxColour(BlockState state, BlockRenderView world, BlockPos pos, int tintIndex) {
        if (pos != null) {
            return posToColor(pos);
        }
        return 16777215;
    }

    public static int getOverlayColorFromComponents(ItemStack stack, int layer) {
        if (stack.getNbt() != null) {
            int color = stack.getNbt().getInt("key_color");
            if (layer == 1) {
                return color;
            }
        }
        return 0xFFFFFF;
    }

    public static int getNeitherPortalColour(BlockState state, BlockRenderView world, BlockPos pos, int tintIndex) {
        if (world != null && pos != null) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof InfinityPortalBlockEntity be) {
                Object j = be.getRenderData();
                if (j == null) return 0;
                return (int)j & 0xFFFFFF;
            }
        }
        return 0xFFFFFF;
    }

    public static int getBiomeBottleColor(BlockState state, BlockRenderView world, BlockPos pos, int tintIndex) {
        if (world != null && pos != null) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof BiomeBottleBlockEntity be) {
                Object j = be.getRenderData();
                if (j == null) return 0;
                return (int)j & 0xFFFFFF;
            }
        }
        return 0xFFFFFF;
    }

    @ExpectPlatform
    public static <T extends Item> void addAfter(RegistrySupplier<T> blockItem, RegistryKey<ItemGroup> group, Item item) {
        throw new AssertionError();
    }

    /**
     * Check if a block is in the Black Dyed Conventional Tag.
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

    @ExpectPlatform
    public static Path getConfigPath() {
        throw new AssertionError();
    }

    /**
     * Create an Item Tag.
     */
    @ExpectPlatform
    public static TagKey<Item> createItemTag(String id) {
        throw new AssertionError();
    }
    @ExpectPlatform
    public static TagKey<Block> createBlockTag(String id) {
        throw new AssertionError();
    }

    /**
     * Register a Flammable nameToElement (how does Architectury API not have a helper for this)
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

    @ExpectPlatform
    public static Function<Item.Settings, ? extends StarOfLangItem> getStarOfLangConstructor() {
        throw new AssertionError();
    }
}
