package net.lerariemann.infinity.util.neoforge;

import dev.architectury.registry.registries.RegistrySupplier;
import io.netty.buffer.Unpooled;
import net.lerariemann.infinity.fluids.neoforge.FluidTypes;
import net.lerariemann.infinity.item.StarOfLangItem;
import net.lerariemann.infinity.util.PlatformMethods;
import net.lerariemann.infinity.registry.core.ModBlocks;
import net.lerariemann.infinity.fluids.neoforge.IridescenceLiquidBlockNeoforge;
import net.lerariemann.infinity.fluids.neoforge.ModFluidsNeoforge;
import net.lerariemann.infinity.util.InfinityMethods;
import net.minecraft.block.*;
import net.minecraft.component.ComponentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.nio.file.Path;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * See {@link PlatformMethods} for usages.
 */
@SuppressWarnings("unused")
public class PlatformMethodsImpl {

    public static PacketByteBuf createPacketByteBufs() {
        return new PacketByteBuf(Unpooled.buffer());
    }

    public static void sendS2CPayload(ServerPlayerEntity entity, CustomPayload payload) {
        PacketDistributor.sendToPlayer(entity, payload);
    }
    public static void sendC2SPayload(CustomPayload payload) {
        PacketDistributor.sendToServer(payload);
    }

    public static void onWorldLoad(Object mixin, ServerWorld world) {
        MinecraftServer server = world.getServer();
        server.forgeGetWorldMap().put(world.getRegistryKey(),world);
        server.markWorldsDirty();
        NeoForge.EVENT_BUS.post(new LevelEvent.Load(world));
    }

    /**
     * Replaced by NeoForge APIs. See {@link NeoItems}.
     */
    public static <T extends Item> void addAfter(RegistrySupplier<T> supplier, RegistryKey<ItemGroup> group, Item item) {
    }

    public static boolean isInBlack(BlockState state) {
        return state.isIn(Tags.Blocks.DYED_BLACK);
    }

    public static boolean isInWhite(BlockState state) {
        return state.isIn(Tags.Blocks.DYED_WHITE);
    }

    public static RegistrySupplier<? extends FlowableFluid> getIridescenceStill() {
        return ModFluidsNeoforge.IRIDESCENCE_STILL;
    }

    public static RegistrySupplier<? extends FlowableFluid> getIridescenceFlowing() {
        return ModFluidsNeoforge.IRIDESCENCE_FLOWING;
    }

    public static RegistrySupplier<FluidBlock> getIridBlockForReg() {
        return ModBlocks.BLOCKS.register("iridescence", () ->
                new IridescenceLiquidBlockNeoforge(PlatformMethods.getIridescenceStill(), AbstractBlock.Settings.copy(Blocks.WATER)
                                .mapColor(MapColor.MAGENTA)));
    }

    public static Path getRootConfigPath() {
        return ModLoadingContext.get().getActiveContainer().getModInfo().getOwningFile().getFile().findResource("config");
    }

    public static TagKey<Item> createItemTag(String id) {
        return ItemTags.create(InfinityMethods.getId(id));
    }
    public static TagKey<Block> createBlockTag(String id) {
        return BlockTags.create(InfinityMethods.getId(id));
    }

    public static void registerFlammableBlock(RegistrySupplier<Block> block, int burn, int spread) {
        FireBlock fireblock = (FireBlock)Blocks.FIRE;
        fireblock.registerFlammableBlock(block.get(), burn, spread);
    }

    public static boolean acidTest(Entity entity, boolean eyes) {
        if (entity instanceof PlayerEntity) return false;
        if (eyes) return entity.isEyeInFluidType(FluidTypes.IRIDESCENCE_TYPE.value());
        return entity.isInFluidType(FluidTypes.IRIDESCENCE_TYPE.value());
    }

    public static double acidHeightTest(Entity entity) {
        if (entity instanceof PlayerEntity) return -1;
        return entity.getFluidTypeHeight(FluidTypes.IRIDESCENCE_TYPE.value());
    }

    public static Item.Settings deferredIntComponent(Supplier<ComponentType<Integer>> componentTypeSupplier, int i) {
        return new Item.Settings().component(componentTypeSupplier, i);
    }
}
