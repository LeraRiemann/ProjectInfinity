package net.lerariemann.infinity.util;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.mojang.brigadier.context.CommandContext;
import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.access.MinecraftServerAccess;
import net.lerariemann.infinity.access.ServerPlayerEntityAccess;
import net.lerariemann.infinity.block.custom.NeitherPortalBlock;
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
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;


public interface WarpLogic {
    static void warpId(CommandContext<ServerCommandSource> context, long value) {
        warp(context, InfinityMod.getDimId(value));
    }

    static void warp(CommandContext<ServerCommandSource> context, Identifier value) {
        warpWithTimer(context.getSource().getPlayer(), value, 20, true);
    }

    static void warpWithTimer(@Nullable ServerPlayerEntity player, Identifier value, int ticks, boolean increaseStats) {
        if (player == null) return;
        MinecraftServer s = player.getServer();
        if (s == null) return;
        if (((MinecraftServerAccess)s).infinity$needsInvocation()) {
            onInvocationNeedDetected(player);
            return;
        }
        boolean isThisANewDimension = NeitherPortalBlock.addInfinityDimension(s, value);
        if (isThisANewDimension && increaseStats) player.increaseStat(ModStats.DIMS_OPENED_STAT, 1);
        ((ServerPlayerEntityAccess)(player)).projectInfinity$setWarpTimer(ticks, value);
    }

    static void respawnAlive(@Nullable ServerPlayerEntity player) {
        if (player == null) return;
        TeleportTarget targ = player.getRespawnTarget(true, TeleportTarget.NO_OP);
        player.teleport(targ.world(), targ.pos().x, targ.pos().y, targ.pos().z, targ.yaw(), targ.pitch());
    }

    static void onInvocationNeedDetected(PlayerEntity player) {
        if (player != null) player.sendMessage(Text.translatable("error.infinity.invocation_needed"));
    }

    static int properMod(int a, int b) {
        int res = a%b;
        return (res >= 0) ? res : b + res;
    }

    static PortalColorApplier getPortalColorApplier(Identifier id, MinecraftServer server) {
        if(id.toString().equals("minecraft:the_end")) return new PortalColorApplier.Simple(0);
        NbtCompound c = RandomProvider.getProvider(server).easterizer.optionmap.get(id.getPath());
        if (c == null) c = InfinityOptions.readData(server, id);
        return InfinityOptions.extractApplier(c);
    }

    static long getNumericFromId(Identifier id, MinecraftServer server) {
        String dimensionName = id.getPath();
        String numericId = dimensionName.substring(dimensionName.lastIndexOf("_") + 1);
        long i;
        try {
            i = Long.parseLong(numericId);
        } catch (Exception e) {
            /* Simply hash the name if it isn't of "generated_..." format. */
            i = WarpLogic.getDimensionSeed(numericId, server);
        }
        return i;
    }

    static int getKeyColorFromId(Identifier id, MinecraftServer server) {
        if(id.getNamespace().equals(InfinityMod.MOD_ID) && id.getPath().contains("generated_"))
            return ColorHelper.Argb.fullAlpha((int) getNumericFromId(id, server) & 0xFFFFFF);
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

    static Identifier getIdentifier(String text, MinecraftServer s) {
        if (text.equals("abatised redivides")) return World.END.getValue();
        if (text.isEmpty()) return InfinityMod.getId("missingno");
        if (RandomProvider.getProvider(s).easterizer.isEaster(text, RandomProvider.getProvider(s)) && !text.equals("missingno")) return InfinityMod.getId(text);
        return InfinityMod.getDimId(getDimensionSeed(text, s));
    }

    static long getDimensionSeed(String text, MinecraftServer s) {
        return getDimensionSeed(text, RandomProvider.getProvider(s));
    }

    /* Hashes text into dimension ID. */
    static long getDimensionSeed(String text, RandomProvider prov) {
        HashCode f = Hashing.sha256().hashString(text + prov.salt, StandardCharsets.UTF_8);
        return InfinityMod.longArithmeticEnabled ? f.asLong() & Long.MAX_VALUE : f.asInt() & Integer.MAX_VALUE;
    }

    static long getRandomSeed(java.util.Random random) {
        return InfinityMod.longArithmeticEnabled ? random.nextLong() : random.nextInt();
    }
    static long getRandomSeed(Random random) {
        return InfinityMod.longArithmeticEnabled ? random.nextLong() : random.nextInt();
    }

    static Identifier getRandomId(java.util.Random random) {
        return InfinityMod.getDimId(getRandomSeed(random));
    }
    static Identifier getRandomId(Random random) {
        return InfinityMod.getDimId(getRandomSeed(random));
    }
}
