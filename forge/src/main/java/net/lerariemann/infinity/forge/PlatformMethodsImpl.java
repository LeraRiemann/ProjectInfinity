package net.lerariemann.infinity.forge;

import dev.architectury.registry.registries.RegistrySupplier;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.lerariemann.infinity.util.PlatformMethods;
import net.lerariemann.infinity.block.ModBlocks;
import net.lerariemann.infinity.fluids.IridescenceLiquidBlockForge;
import net.lerariemann.infinity.fluids.ModFluidsForge;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidBlock;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.fluids.ForgeFlowingFluid;

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
                new IridescenceLiquidBlockForge(PlatformMethods.getIridescenceStill(), AbstractBlock.Settings.copy(Blocks.WATER)));
    }
}
