package net.lerariemann.infinity.compat;

import com.simibubi.create.foundation.utility.BlockFace;
import com.simibubi.create.foundation.utility.Pair;
import net.lerariemann.infinity.block.entity.InfinityPortalBlockEntity;
import net.lerariemann.infinity.util.WarpLogic;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class CreateCompat {

    public static RegistryKey<World> getInfinityWorld(Pair<ServerWorld, BlockFace> inbound) {
        ServerWorld world = inbound.getFirst();
        BlockFace blockFace = inbound.getSecond();
        BlockEntity blockEntity = world.getBlockEntity(blockFace.getConnectedPos());
        if (blockEntity instanceof InfinityPortalBlockEntity portalEntity && portalEntity.isOpen()) {
            Identifier id = portalEntity.getDimension();
            RegistryKey<World> key = RegistryKey.of(RegistryKeys.WORLD, id);
            if (WarpLogic.dimExists(world.getServer().getWorld(key))) return key;
        }
        return null;
    }
}
