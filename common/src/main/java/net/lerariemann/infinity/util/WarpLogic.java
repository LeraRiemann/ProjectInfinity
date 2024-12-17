package net.lerariemann.infinity.util;

import com.mojang.brigadier.context.CommandContext;
import net.lerariemann.infinity.access.MinecraftServerAccess;
import net.lerariemann.infinity.access.ServerPlayerEntityAccess;
import net.lerariemann.infinity.registry.var.ModStats;
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
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Objects;

public interface WarpLogic {
    /**
     * Handles the /warp-id command, converting their numeric ID to an Identifier
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
        requestWarp(context.getSource().getPlayer(), value, true);
    }

    /**
     * Handles moving a specified player to a specified dimension which may or may not yet exist.
     */
    static void requestWarp(@Nullable ServerPlayerEntity player, Identifier value, boolean increaseStats) {
        if (player == null) return;
        MinecraftServer s = player.getServer();
        if (s == null) return;
        if (((MinecraftServerAccess)s).infinity$needsInvocation()) {
            PortalCreator.onInvocationNeedDetected(player);
            return;
        }
        boolean isThisANewDimension = PortalCreator.tryAddInfinityDimension(s, value);
        if (isThisANewDimension) { //the server needs a bit of time to process the dim-adding request, hence the 2-tick wait
            if (increaseStats) player.increaseStat(ModStats.DIMS_OPENED_STAT, 1);
            ((ServerPlayerEntityAccess)(player)).infinity$setWarpTimer(2, value);
        }
        else performWarp(player, value);
    }

    /**
     * Moves the player to a specified existing dimension.
     */
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

    /**
     * Teleport a player to their respawn point.
     */
    static void respawnAlive(@Nullable ServerPlayerEntity player) {
        if (player == null) return;
        TeleportTarget targ = player.getRespawnTarget(true, TeleportTarget.NO_OP);
        player.teleport(targ.world(), targ.pos().x, targ.pos().y, targ.pos().z, targ.yaw(), targ.pitch());
    }
    /* will implement a proper end-of-time dimension later */
    static void sendToMissingno(ServerPlayerEntity player) {
        requestWarp(player, InfinityMethods.getId("missingno"), false);
    }

    /**
     * Root method for finding a safe position to warp to.
     */
    static BlockPos getPosForWarp(@NotNull BlockPos orig, @NotNull ServerWorld world) {
        BlockPos iter = orig;
        int counter = 0;
        BlockPos pos = scanColumn(orig, world, true);
        while (pos == null) { // do eight attempts on strong rules and one on weak rules
            iter = iter.add(world.random.nextInt(16) - 8, 0, world.random.nextInt(16) - 8);
            pos = scanColumn(iter, world, true);
            if (++counter > 7) {
                pos = Objects.requireNonNullElse(scanColumn(orig, world, false), orig);
                break;
            }
        }
        return pos;
    }

    /**
     * Tries to find a safe position with fixed x and z coordinates.
     */
    @Nullable
    static BlockPos scanColumn(BlockPos orig, ServerWorld world, boolean strong) {
        int x = orig.getX();
        int y1 = orig.getY();
        int z = orig.getZ();
        if (isPosViable(x, y1, z, world, strong)) return orig;
        int y2 = y1;
        while (y1 > world.getBottomY() || y2 < world.getTopY()) {
            y1-=1;
            if (isPosViable(x, y1, z, world, strong)) return new BlockPos(x, y1, z);
            y2+=1;
            if (isPosViable(x, y2, z, world, strong)) return new BlockPos(x, y2, z);
        }
        return null;
    }

    static boolean isPosViable(int x, int y, int z, BlockView w, boolean strong) {
        return strong ? isPosViableStrong(new BlockPos(x, y, z), w) : isPosViableWeak(new BlockPos(x, y, z), w);
    }
    /** "Place me on ground surface" where ground is equivalent to "not air". */
    static boolean isPosViableWeak(BlockPos pos, BlockView w) {
        if (w.isOutOfHeightLimit(pos.getY())) return false;
        boolean bl = w.getBlockState(pos.up()).isAir();
        boolean bl2 = w.getBlockState(pos).isAir();
        boolean bl3 = w.getBlockState(pos.down()).isAir();
        return (!bl3 && bl2 && bl);
    }
    /** Same as above except now fluids (except water) are unacceptable ground. */
    static boolean isPosViableStrong(BlockPos pos, BlockView w) {
        if (w.isOutOfHeightLimit(pos.getY())) return false;
        boolean bl = isSafe(w, pos.up(), true);
        boolean bl2 = isSafe(w, pos, true);
        boolean bl3 = isSafe(w, pos.down(), false);
        return (bl3 && bl2 && bl);
    }

    static boolean isSafe(BlockView world, BlockPos pos, boolean asAir) {
        BlockState state = world.getBlockState(pos);
        if (state.isAir()) return asAir;
        if (asAir) return false;
        FluidState fluidState = state.getFluidState();
        return (fluidState.isOf(Fluids.EMPTY) || fluidState.isIn(FluidTags.WATER));
    }

    /** Replaces blocks at the target position in case all above measures failed. */
    static void ensureSafety(ServerWorld world, BlockPos pos, Block fallback) {
        BlockState fb = fallback.getDefaultState();
        if (!isSafe(world, pos, fb.isAir()))
            world.setBlockState(pos, fb);
    }
}
