package net.lerariemann.infinity.neoforge;

import dev.architectury.registry.registries.RegistrySupplier;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.mixin.registry.sync.BaseMappedRegistryAccessor;
import net.lerariemann.infinity.PlatformMethods;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.LevelEvent;
/**
 * See {@link net.lerariemann.infinity.PlatformMethods} for usages.
 */
@SuppressWarnings("unused")
public class PlatformMethodsImpl {
    public static boolean isModLoaded(String modID) {
       return ModList.get().isLoaded(modID);
    }

    public static boolean isFabricApiLoaded(String modID) {
        return isModLoaded(modID.replace("-", "_"));
    }

    public static PacketByteBuf createPacketByteBufs() {
        return new PacketByteBuf(Unpooled.buffer());
    }

    public static void onWorldLoad(Object mixin, ServerWorld world) {
        MinecraftServer server = world.getServer();
        server.forgeGetWorldMap().put(world.getRegistryKey(),world);
        server.markWorldsDirty();
        NeoForge.EVENT_BUS.post(new LevelEvent.Load(world));
    }

    public static void unfreeze(Registry<?> registry) {
        ((BaseMappedRegistryAccessor) registry).invokeUnfreeze();
    }

    public static void freeze(Registry<?> registry) {
        registry.freeze();
    }

    //Optional, requires Item Group API.
    public static void addAfter(RegistrySupplier<Item> supplier, RegistryKey<ItemGroup> group, Item item) {
        if (PlatformMethods.isFabricApiLoaded("fabric-item-group-api-v1")) {
            ItemGroupEvents.modifyEntriesEvent(group).register(content -> content.addAfter(item, supplier.get()));
        }
    }

}
