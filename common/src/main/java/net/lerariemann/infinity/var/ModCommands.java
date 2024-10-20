package net.lerariemann.infinity.var;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.mojang.brigadier.context.CommandContext;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.lerariemann.infinity.access.ServerPlayerEntityAccess;
import net.lerariemann.infinity.access.MinecraftServerAccess;
import net.lerariemann.infinity.block.custom.NeitherPortalBlock;
import net.lerariemann.infinity.dimensions.RandomProvider;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.nio.charset.StandardCharsets;


public class ModCommands {
    public static void warpId(CommandContext<ServerCommandSource> context, long value) {
        MinecraftServer s = context.getSource().getServer();
        boolean isThisANewDimension = NeitherPortalBlock.addInfinityDimension(s, value);
        final ServerPlayerEntity self = context.getSource().getPlayer();
        if (self != null) {
            if (isThisANewDimension) self.increaseStat(ModStats.DIMS_OPENED_STAT, 1);
            self.increaseStat(ModStats.PORTALS_OPENED_STAT, 1);
            ((ServerPlayerEntityAccess)(self)).projectInfinity$setWarpTimer(20, value);
        }
    }

    public static RegistryKey<World> getKey(long d, MinecraftServer s) {
        return RegistryKey.of(RegistryKeys.WORLD, getIdentifier(d, s));
    }

    public static Identifier getIdentifier(long d, MinecraftServer s) {
        if (d == ModCommands.getDimensionSeed("abatised redivides", s)) return World.END.getValue();
        return ((MinecraftServerAccess)s).projectInfinity$getDimensionProvider().easterizer.keyOf(d);
    }

    public static long getDimensionSeed(String text, MinecraftServer s) {
        return getDimensionSeed(text, ((MinecraftServerAccess)(s)).projectInfinity$getDimensionProvider());
    }

    /* Hashes text into dimension ID. */
    public static long getDimensionSeed(String text, RandomProvider prov) {
        HashCode f = Hashing.sha256().hashString(text + prov.salt, StandardCharsets.UTF_8);
        return prov.rule("longArithmeticEnabled") ? f.asLong() & Long.MAX_VALUE : f.asInt() & Integer.MAX_VALUE;
    }

    @ExpectPlatform
    public static void registerCommands() {
        throw new AssertionError();
    }
}
