package net.lerariemann.infinity.util.forge;

import dev.architectury.registry.registries.RegistrySupplier;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.lerariemann.infinity.block.ModBlocks;
import net.lerariemann.infinity.fluids.forge.IridescenceLiquidBlockForge;
import net.lerariemann.infinity.fluids.forge.ModFluidsForge;
import net.lerariemann.infinity.util.InfinityMethods;
import net.lerariemann.infinity.util.PlatformMethods;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidBlock;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.fml.ModLoadingContext;

import java.nio.file.Path;

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
    public static void addAfter(RegistrySupplier<Item> blockItem, RegistryKey<ItemGroup> group, Item item) {
        ItemGroupEvents.modifyEntriesEvent(group).register(content -> content.addAfter(item, blockItem.get()));
    }

    public static RegistrySupplier<ForgeFlowingFluid.Source> getIridescenceStill() {
        return ModFluidsForge.IRIDESCENCE_STILL;
    }

    public static RegistrySupplier<ForgeFlowingFluid.Flowing> getIridescenceFlowing() {
        return ModFluidsForge.IRIDESCENCE_FLOWING;
    }

    public static RegistrySupplier<FluidBlock> getIridBlockForReg() {
        return ModBlocks.BLOCKS.register("iridescence", () ->
                new IridescenceLiquidBlockForge(getIridescenceStill(), AbstractBlock.Settings.copy(Blocks.WATER)));
    }

    public static Path getRootConfigPath() {
        return ModLoadingContext.get().getActiveContainer().getModInfo().getOwningFile().getFile().findResource("config");
    }

    public static TagKey<Item> createItemTag(String id) {
        return ItemTags.create(InfinityMethods.getId(id));
    }

    public static void registerFlammableBlock(RegistrySupplier<Block> block, int burn, int spread) {
        if (InfinityMethods.isFabricApiLoaded("fabric-content-registries-v0")) {
            FlammableBlockRegistry.getDefaultInstance().add(block.get(), burn, spread);
        }
    }

}
