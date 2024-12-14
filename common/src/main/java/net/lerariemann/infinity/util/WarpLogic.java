package net.lerariemann.infinity.util;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.mojang.brigadier.context.CommandContext;
import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.access.MinecraftServerAccess;
import net.lerariemann.infinity.access.ServerPlayerEntityAccess;
import net.lerariemann.infinity.access.Timebombable;
import net.lerariemann.infinity.options.InfinityOptions;
import net.lerariemann.infinity.options.PortalColorApplier;
import net.lerariemann.infinity.var.ModStats;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.world.BlockView;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;


public interface WarpLogic {
    /**
     * Handles the /warpid command, converting their numeric ID to an Identifier
     * and passing the data along to /warp.
     */
    static void warpId(CommandContext<ServerCommandSource> context, long value) {
        warp(context, InfinityMethods.getDimId(value));
    }

    /**
     * Handles the /warp command, warping the player to their specified dimension.
     * The player-provided text should have already been converted to an Identifier.
     */
    static void warp(CommandContext<ServerCommandSource> context, Identifier value) {
        warpWithTimer(context.getSource().getPlayer(), value, 20, true);
    }

    /**
     * Handles moving a specified player to a specified dimension.
     * This is used by both the warp commands and Iridescence.
     */
    static void warpWithTimer(@Nullable ServerPlayerEntity player, Identifier value, int ticks, boolean increaseStats) {
        if (player == null) return;
        MinecraftServer s = player.getServer();
        if (s == null) return;
        if (((MinecraftServerAccess)s).infinity$needsInvocation()) {
            onInvocationNeedDetected(player);
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
        TeleportTarget targ = player.getRespawnTarget(true, TeleportTarget.NO_OP);
        player.teleport(targ.world(), targ.pos().x, targ.pos().y, targ.pos().z, targ.yaw(), targ.pitch());
    }
    /* will implement a proper end-of-time dimension later */
    static void sendToMissingno(ServerPlayerEntity player) {
        warpWithTimer(player, InfinityMethods.getId("missingno"), 2, false);
    }

    static void onInvocationNeedDetected(PlayerEntity player) {
        if (player != null) player.sendMessage(Text.translatable("error.infinity.invocation_needed"));
    }

    static PortalColorApplier getPortalColorApplier(Identifier id, MinecraftServer server) {
        return getPortalColorApplier(id, InfinityOptions.readData(server, id));
    }
    static PortalColorApplier getPortalColorApplier(Identifier id, NbtCompound def) {
        NbtCompound c = InfinityMod.provider.easterizer.optionmap.get(id.getPath());
        if (c == null) c = def;
        return PortalColorApplier.extract(c, (int)WarpLogic.getNumericFromId(id));
    }

    static long getNumericFromId(Identifier id) {
        String dimensionName = id.getPath();
        String numericId = dimensionName.substring(dimensionName.lastIndexOf("_") + 1);
        long i;
        try {
            i = Long.parseLong(numericId);
        } catch (Exception e) {
            /* Simply hash the name if it isn't of "generated_..." format. */
            i = WarpLogic.getDimensionSeed(numericId);
        }
        return i;
    }

    static int getKeyColorFromId(Identifier id) {
        if(id.getNamespace().equals(InfinityMod.MOD_ID) && id.getPath().contains("generated_"))
            return ColorHelper.Argb.fullAlpha((int) getNumericFromId(id) & 0xFFFFFF);
        return 0;
    }

    static BlockPos getPosForWarp(BlockPos orig, ServerWorld world) {
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
        return orig;
    }

    static boolean isPosViable(int x, int y, int z, BlockView w) {
        boolean bl = w.getBlockState(new BlockPos(x, y+1, z)).isAir();
        boolean bl2 = w.getBlockState(new BlockPos(x, y, z)).isAir();
        boolean bl3 = w.getBlockState(new BlockPos(x, y-1, z)).isAir();
        return (!bl3 && bl2 && bl);
    }

    /**
     * Convert a provided string into a dimension ID.
     * This also checks if it matches an Easter Egg dimension.
     */
    static Identifier getIdentifier(String text) {
        if (text.equals("abatised redivides")) return World.END.getValue();
        if (text.isEmpty()) return InfinityMethods.getId("missingno");
        if (InfinityMod.provider.easterizer.isEaster(text, InfinityMod.provider) && !text.equals("missingno")) return InfinityMethods.getId(text);
        return InfinityMethods.getDimId(getDimensionSeed(text));
    }

    /**
     * Check if a dimension exists and has not been timebombed.
     */
    static boolean dimExists(ServerWorld world) {
        return (world != null && !((Timebombable)world).infinity$isTimebombed());
    }

    /**
     * Hashes text into dimension ID.
     */
    static long getDimensionSeed(String text) {
        HashCode f = Hashing.sha256().hashString(text + InfinityMod.provider.salt, StandardCharsets.UTF_8);
        return InfinityMethods.longArithmeticEnabled() ? f.asLong() & Long.MAX_VALUE : f.asInt() & Integer.MAX_VALUE;
    }
}
