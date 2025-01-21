package net.lerariemann.infinity.util.fabric;

import dev.architectury.registry.registries.RegistrySupplier;
import me.basiqueevangelist.dynreg.util.RegistryUtils;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBlockTags;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.item.StarOfLangItem;
import net.lerariemann.infinity.registry.core.ModComponentTypes;
import net.lerariemann.infinity.util.PlatformMethods;
import net.lerariemann.infinity.registry.core.ModBlocks;
import net.lerariemann.infinity.fluids.fabric.ModFluidsFabric;
import net.lerariemann.infinity.iridescence.IridescenceLiquidBlock;
import net.lerariemann.infinity.util.InfinityMethods;
import net.lerariemann.infinity.util.var.ColorLogic;
import net.minecraft.block.*;
import net.minecraft.component.ComponentType;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;

import java.nio.file.Path;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * See {@link PlatformMethods} for usages.
 */
@SuppressWarnings("unused")
public class PlatformMethodsImpl {

    public static PacketByteBuf createPacketByteBufs() {
        return PacketByteBufs.create();
    }

    public static void onWorldLoad(Object mixin, ServerWorld world) {
        ServerWorldEvents.LOAD.invoker().onWorldLoad((MinecraftServer) mixin, world);
    }

    public static void unfreeze(Registry<?> registry) {
        RegistryUtils.unfreeze(registry);
    }

    public static <T extends Item> void addAfter(RegistrySupplier<T> supplier, RegistryKey<ItemGroup> group, Item item) {
        if (InfinityMethods.isFabricApiLoaded("fabric-item-group-api-v1")) {
            ItemGroupEvents.modifyEntriesEvent(group).register(content -> content.addAfter(item, supplier.get()));
        }
    }

    public static boolean isInBlack(BlockState state) {
        return state.isIn(ConventionalBlockTags.BLACK_DYED);
    }

    public static boolean isInWhite(BlockState state) {
        return state.isIn(ConventionalBlockTags.WHITE_DYED);
    }


    public static RegistrySupplier<? extends FlowableFluid> getIridescenceStill() {
        return ModFluidsFabric.IRIDESCENCE_STILL;
    }

    public static RegistrySupplier<? extends FlowableFluid> getIridescenceFlowing() {
        return ModFluidsFabric.IRIDESCENCE_FLOWING;
    }

    public static RegistrySupplier<FluidBlock> getIridBlockForReg() {
        return ModBlocks.BLOCKS.register("iridescence", () ->
                new IridescenceLiquidBlock(PlatformMethods.getIridescenceStill(),
                        AbstractBlock.Settings.copy(Blocks.WATER).mapColor(MapColor.MAGENTA)));
    }

    public static Path getRootConfigPath() {
        ModContainer mc = FabricLoader.getInstance().getModContainer(InfinityMod.MOD_ID).orElse(null);
        assert mc != null;
        return mc.getRootPaths().getFirst().resolve("config");
    }

    public static TagKey<Item> createItemTag(String id) {
        return TagKey.of(RegistryKeys.ITEM, InfinityMethods.getId(id));
    }
    public static TagKey<Block> createBlockTag(String id) {
        return TagKey.of(RegistryKeys.BLOCK, InfinityMethods.getId(id));
    }

    public static void registerFlammableBlock(RegistrySupplier<Block> block, int burn, int spread) {
        FlammableBlockRegistry.getDefaultInstance().add(block.get(), burn, spread);
    }

    public static boolean acidTest(Entity entity, boolean eyes) {
        return false;
    }
    public static double acidHeightTest(Entity entity) {
        return -1;
    }

    public static Function<Item.Settings, ? extends StarOfLangItem> getStarOfLangConstructor() {
        return StarOfLangItem::new;
    }

    public static Item.Settings deferredIntComponent(Supplier<ComponentType<Integer>> componentTypeSupplier, int i) {
        return new Item.Settings().component(componentTypeSupplier.get(), i);
    }
}
