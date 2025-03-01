package net.lerariemann.infinity.util.forge;

import dev.architectury.registry.registries.RegistrySupplier;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.lerariemann.infinity.item.forge.StarOfLangItemForge;
import net.lerariemann.infinity.fluids.forge.FluidTypes;
import net.lerariemann.infinity.item.StarOfLangItem;
import net.lerariemann.infinity.util.PlatformMethods;
import net.lerariemann.infinity.registry.core.ModBlocks;
import net.lerariemann.infinity.fluids.forge.IridescenceLiquidBlockForge;
import net.lerariemann.infinity.fluids.forge.ModFluidsForge;
import net.lerariemann.infinity.util.InfinityMethods;
import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Path;
import java.util.function.Function;

/**
 * See {@link PlatformMethods} for usages.
 */
@SuppressWarnings("unused")
public class PlatformMethodsImpl {

    public static PacketByteBuf createPacketByteBufs() {
        return new PacketByteBuf(Unpooled.buffer());
    }

    public static void onWorldLoad(Object mixin, ServerWorld world) {
        MinecraftServer server = world.getServer();
        server.forgeGetWorldMap().put(world.getRegistryKey(),world);
        server.markWorldsDirty();
        MinecraftForge.EVENT_BUS.post(new LevelEvent.Load(world));
    }

    public static void unfreeze(Registry<?> registry) {
        SimpleRegistry<?> writableRegistry = (SimpleRegistry<?>) registry;
        writableRegistry.unfreeze();
    }

    public static void freeze(Registry<?> registry) {
        registry.freeze();
    }

    //Optional, requires Item Group API.
    public static <T extends Item> void addAfter(RegistrySupplier<T> supplier, RegistryKey<ItemGroup> group, Item item) {
        if (InfinityMethods.isFabricApiLoaded("fabric-item-group-api-v1")) {
            ItemGroupEvents.modifyEntriesEvent(group).register(content -> content.addAfter(item, supplier.get()));
        }
    }

    public static RegistrySupplier<ForgeFlowingFluid.Source> getIridescenceStill() {
        return ModFluidsForge.IRIDESCENCE_STILL;
    }

    public static RegistrySupplier<ForgeFlowingFluid.Flowing> getIridescenceFlowing() {
        return ModFluidsForge.IRIDESCENCE_FLOWING;
    }

    public static RegistrySupplier<FluidBlock> getIridBlockForReg() {
        return ModBlocks.BLOCKS.register("iridescence", () ->
                new IridescenceLiquidBlockForge(PlatformMethods.getIridescenceStill(), AbstractBlock.Settings.copy(Blocks.WATER)
                                .mapColor(MapColor.MAGENTA)));
    }

    public static Path getRootConfigPath() {
        return ModLoadingContext.get().getActiveContainer().getModInfo().getOwningFile().getFile().findResource("config");
    }

    public static Path getConfigPath() {
        return Path.of(FMLPaths.CONFIGDIR.get() + "/infinity");
    }

    public static TagKey<Item> createItemTag(String id) {
        return ItemTags.create(InfinityMethods.getId(id));
    }
    public static TagKey<Block> createBlockTag(String id) {
        return BlockTags.create(InfinityMethods.getId(id));
    }

    public static void registerFlammableBlock(RegistrySupplier<Block> block, int burn, int spread) {
        if (InfinityMethods.isFabricApiLoaded("fabric-content-registries-v0")) {
            FlammableBlockRegistry.getDefaultInstance().add(block.get(), burn, spread);
        }
    }

    public static boolean acidTest(Entity entity, boolean eyes) {
        if (entity instanceof PlayerEntity) return false;
        if (eyes) return entity.isEyeInFluidType(FluidTypes.IRIDESCENCE_TYPE.get());
        return entity.isInFluidType(FluidTypes.IRIDESCENCE_TYPE.get());
    }

    public static double acidHeightTest(Entity entity) {
        if (entity instanceof PlayerEntity) return -1;
        return entity.getFluidTypeHeight(FluidTypes.IRIDESCENCE_TYPE.get());
    }

    public static Function<Item.Settings, ? extends StarOfLangItem> getStarOfLangConstructor() {
        return StarOfLangItemForge::new;
    }
}
