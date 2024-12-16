package net.lerariemann.infinity.util;

import com.mojang.brigadier.context.CommandContext;
import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.access.MinecraftServerAccess;
import net.lerariemann.infinity.access.ServerPlayerEntityAccess;
import net.lerariemann.infinity.var.ModStats;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;

import static net.lerariemann.infinity.util.InfinityMethods.getNumericFromId;

public interface WarpLogic {
    /**
     * Handles the /warpid command, converting their numeric ID to an Identifier
     * and passing the data along to /warp.
     */
    static void requestWarpById(CommandContext<ServerCommandSource> context, long value) {
        requestWarp(context, InfinityMethods.getDimId(value));
    }

    /**
     * Handles the /warp command, warping the player to their specified dimension.
     * The player-provided text should have already been converted to an Identifier.
     */
    static void requestWarp(CommandContext<ServerCommandSource> context, Identifier value) {
        requestWarpWithTimer(context.getSource().getPlayer(), value, 2, true);
    }

    /**
     * Handles moving a specified player to a specified dimension.
     * This is used by both the warp commands and Iridescence.
     */
    static void requestWarpWithTimer(@Nullable ServerPlayerEntity player, Identifier value, int ticks, boolean increaseStats) {
        if (player == null) return;
        MinecraftServer s = player.getServer();
        if (s == null) return;
        if (((MinecraftServerAccess)s).infinity$needsInvocation()) {
            PortalCreationLogic.onInvocationNeedDetected(player);
            return;
        }
        boolean isThisANewDimension = PortalCreationLogic.addInfinityDimension(s, value);
        if (isThisANewDimension && increaseStats) player.increaseStat(ModStats.DIMS_OPENED_STAT, 1);
        ((ServerPlayerEntityAccess)(player)).infinity$setWarpTimer(ticks, value);
    }

    /**
     * Teleport a player to their respawn point.
     */
    static void respawnAlive(@Nullable ServerPlayerEntity player) {
        if (player == null) return;
        BlockPos targ = player.getSpawnPointPosition();
        ServerWorld serverWorld = player.server.getWorld(player.getSpawnPointDimension());
        if (targ == null || serverWorld == null) {
            serverWorld = player.server.getOverworld();
            targ = serverWorld.getSpawnPos();
        }
        player.teleport(serverWorld, targ.getX() + 0.5, targ.getY(), targ.getZ() + 0.5,
                new HashSet<>(), player.getYaw(), player.getPitch());

    }
    /* will implement a proper end-of-time dimension later */
    static void sendToMissingno(ServerPlayerEntity player) {
        requestWarpWithTimer(player, InfinityMethods.getId("missingno"), 2, false);
    }

    static void performWarp(ServerPlayerEntity player, Identifier idForWarp) {
        MinecraftServer s = player.getServerWorld().getServer();
        RegistryKey<World> key = RegistryKey.of(RegistryKeys.WORLD, idForWarp);
        ServerWorld w = s.getWorld(key);
        if (w==null) {
            InfinityMethods.sendUnexpectedError(player, "warp");
            return;
        }

        double d = DimensionType.getCoordinateScaleFactor(player.getServerWorld().getDimension(), w.getDimension());
        double y = MathHelper.clamp(player.getY(), w.getBottomY(), w.getTopY());

        BlockPos blockPos2 = getPosForWarp(w.getWorldBorder().clamp(player.getX() * d, y, player.getZ() * d), w);

        ensureSafety(w, blockPos2.down(), Blocks.OBSIDIAN);
        ensureSafety(w, blockPos2, Blocks.AIR);
        ensureSafety(w, blockPos2.up(), Blocks.AIR);
        player.teleport(w, blockPos2.getX() + 0.5, blockPos2.getY(), blockPos2.getZ() + 0.5,
                new HashSet<>(), player.getYaw(), player.getPitch());
    }

      static int getKeyColorFromId(Identifier id) {
        if(id.getNamespace().equals(InfinityMod.MOD_ID) && id.getPath().contains("generated_"))
            return Math.toIntExact(getNumericFromId(id) & 0xFFFFFF);
        return 0;
    }


    static void ensureSafety(ServerWorld world, BlockPos pos, Block fallback) {
        BlockState fb = fallback.getDefaultState();
        if (!isSafe(world.getBlockState(pos), fb))
            world.setBlockState(pos, fb);
    }

    static boolean isSafe(BlockState state, BlockState fallback) {
        if (state.isAir()) return fallback.isAir();
        FluidState fluidState = state.getFluidState();
        return (fluidState.isOf(Fluids.EMPTY) || fluidState.isIn(FluidTags.WATER));
    }

    static BlockPos getPosForWarp(@NotNull BlockPos orig, @NotNull ServerWorld world) {
        BlockPos pos = scanColumn(orig, world);
        BlockPos iter = orig;
        int counter = 0;
        while (pos == null) {
            if (++counter > 8) pos = orig;
            else {
                iter = iter.add(world.random.nextInt(8), 0, world.random.nextInt(8));
                pos = scanColumn(iter, world);
            }
        }
        return pos;
    }

    @Nullable
    static BlockPos scanColumn(BlockPos orig, ServerWorld world) {
        int x = orig.getX();
        int y1 = orig.getY();
        int z = orig.getZ();
        if (isPosViable(x, y1, z, world)) return orig;
        int y2 = y1;
        while (y1 > world.getBottomY() || y2 < world.getTopY()) {
            y1-=1;
            if (isPosViable(x, y1, z, world)) return new BlockPos(x, y1, z);
            y2+=1;
            if (isPosViable(x, y2, z, world)) return new BlockPos(x, y2, z);
        }
        return null;
    }

    static boolean isPosViable(int x, int y, int z, BlockView w) {
        if (w.isOutOfHeightLimit(y)) return false;
        boolean bl = w.getBlockState(new BlockPos(x, y+1, z)).isAir();
        boolean bl2 = w.getBlockState(new BlockPos(x, y, z)).isAir();
        boolean bl3 = w.getBlockState(new BlockPos(x, y-1, z)).isAir();
        return (!bl3 && bl2 && bl);
    }
}
