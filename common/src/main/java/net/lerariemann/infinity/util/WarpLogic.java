package net.lerariemann.infinity.util;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.mojang.brigadier.context.CommandContext;
import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.access.MinecraftServerAccess;
import net.lerariemann.infinity.access.ServerPlayerEntityAccess;
import net.lerariemann.infinity.block.custom.NeitherPortalBlock;
import net.lerariemann.infinity.var.ModStats;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

import java.awt.*;
import java.nio.charset.StandardCharsets;
import java.util.Objects;


public interface WarpLogic {
    static void warpId(CommandContext<ServerCommandSource> context, long value) {
        warp(context, InfinityMod.getId("generated_" + value));
    }

    static void warp(CommandContext<ServerCommandSource> context, Identifier value) {
        MinecraftServer s = context.getSource().getServer();
        if (((MinecraftServerAccess)s).projectInfinity$needsInvocation()) {
            onInvocationNeedDetected(context.getSource().getPlayer());
            return;
        }
        boolean isThisANewDimension = NeitherPortalBlock.addInfinityDimension(s, value);
        final ServerPlayerEntity self = context.getSource().getPlayer();
        if (self != null) {
            if (isThisANewDimension) self.increaseStat(ModStats.DIMS_OPENED_STAT, 1);
            ((ServerPlayerEntityAccess)(self)).projectInfinity$setWarpTimer(20, value);
        }
    }

    static void onInvocationNeedDetected(PlayerEntity player) {
        if (player != null) player.sendMessage(Text.translatable("error.infinity.invocation_needed"), false);
    }

    static int properMod(int a, int b) {
        int res = a%b;
        return (res >= 0) ? res : b + res;
    }

    static long getPortalColorFromId(Identifier id, MinecraftServer server, BlockPos pos) {
        return switch(id.toString()) {
            case "minecraft:the_end" -> 0;
            case "infinity:chaos" -> Color.HSBtoRGB(Objects.requireNonNull(server.getWorld(World.OVERWORLD)).getRandom().nextFloat(),
                        1.0f, 1.0f);
            case "infinity:chess" -> (properMod(pos.getX() + pos.getY() + pos.getZ(), 2) == 0 ? 0 : 0xffffff);
            case "infinity:pride" -> switch(properMod(pos.getX() + pos.getY() + pos.getZ(), 3)) {
                    case 0 -> 0x77c1de;
                    case 1 -> 0xdaadb5;
                    default -> 0xffffff;
                };
            default -> RandomProvider.getProvider(server).easterizer.colormap.getOrDefault(
                    id.getPath(), (int)getNumericFromId(id, server));
        };
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
            return ColorHelper.fullAlpha((int) getNumericFromId(id, server) & 0xFFFFFF);
        return 0;
    }

    static BlockPos getPosForWarp(BlockPos orig, ServerWorld world) {
        int x = orig.getX();
        int y1 = orig.getY();
        int z = orig.getZ();
        if (isPosViable(x, y1, z, world)) return orig;
        int y2 = y1;
        while (y1 > world.getBottomY() || y2 < world.getTopYInclusive()) {
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
        if (RandomProvider.getProvider(s).easterizer.isEaster(text) && !text.equals("missingno")) return InfinityMod.getId(text);
        return InfinityMod.getId("generated_" + getDimensionSeed(text, s));
    }

    static long getDimensionSeed(String text, MinecraftServer s) {
        return getDimensionSeed(text, RandomProvider.getProvider(s));
    }

    /* Hashes text into dimension ID. */
    static long getDimensionSeed(String text, RandomProvider prov) {
        HashCode f = Hashing.sha256().hashString(text + prov.salt, StandardCharsets.UTF_8);
        return prov.rule("longArithmeticEnabled") ? f.asLong() & Long.MAX_VALUE : f.asInt() & Integer.MAX_VALUE;
    }
}
