package net.lerariemann.infinity.fabric;

import dev.architectury.registry.registries.RegistrySupplier;
import me.basiqueevangelist.dynreg.util.RegistryUtils;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBlockTags;
import net.lerariemann.infinity.PlatformMethods;
import net.lerariemann.infinity.fluid.ModFluids;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
/**
 * See {@link net.lerariemann.infinity.PlatformMethods} for usages.
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

    public static void freeze(Registry<?> registry) {
        registry.freeze();

    }

    public static void addAfter(RegistrySupplier<Item> supplier, RegistryKey<ItemGroup> group, Item item) {
        if (PlatformMethods.isFabricApiLoaded("fabric-item-group-api-v1")) {
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
        return ModFluids.IRIDESCENCE_STILL;
    }

    public static RegistrySupplier<? extends FlowableFluid> getIridescenceFlowing() {
        return ModFluids.IRIDESCENCE_FLOWING;
    }

}
