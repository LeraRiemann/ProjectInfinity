package net.lerariemann.infinity.var;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import dev.architectury.event.events.common.CommandRegistrationEvent;
import net.lerariemann.infinity.access.ServerPlayerEntityAccess;
import net.lerariemann.infinity.access.MinecraftServerAccess;
import net.lerariemann.infinity.block.custom.NeitherPortalBlock;
import net.lerariemann.infinity.dimensions.RandomProvider;
import net.lerariemann.infinity.util.ConfigGenerator;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.nio.charset.StandardCharsets;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;


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

    public static void registerCommands() {
        CommandRegistrationEvent.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(literal("warp-id")
                .requires(source -> source.hasPermissionLevel(2))
                .then(argument("id", IntegerArgumentType.integer()).executes(context -> {
                    final int value = IntegerArgumentType.getInteger(context, "id");
                    warpId(context, value);
                    return 1;
                }))));
        CommandRegistrationEvent.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(literal("warp")
                .requires(source -> source.hasPermissionLevel(2))
                .then(argument("text", StringArgumentType.string()).executes(context -> {
                    final String text = StringArgumentType.getString(context, "text");
                    warpId(context, getDimensionSeed(text, context.getSource().getServer()));
                    return 1;
                }))));
        CommandRegistrationEvent.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(literal("generate_configs")
                .requires(source -> source.hasPermissionLevel(2))
                .then(argument("pos_air", BlockPosArgumentType.blockPos()).executes(context -> 1)
                        .then(argument("pos_stone", BlockPosArgumentType.blockPos()).executes(context -> {
                            BlockPos bp1 = BlockPosArgumentType.getBlockPos(context, "pos_air");
                            BlockPos bp2 = BlockPosArgumentType.getBlockPos(context, "pos_stone");
                            ServerWorld w = context.getSource().getWorld();
                            ConfigGenerator.generateAll(w, bp1, bp2);
                            return 1;
                        })))));
    }
}
