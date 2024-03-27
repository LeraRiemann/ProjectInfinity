package net.lerariemann.infinity.var;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.lerariemann.access.ServerPlayerEntityAccess;
import net.lerariemann.access.MinecraftServerAccess;
import net.lerariemann.infinity.block.custom.NeitherPortalBlock;
import net.lerariemann.infinity.dimensions.RandomProvider;
import net.lerariemann.infinity.util.ConfigGenerator;
import net.lerariemann.infinity.util.RandomLootDrops;
import net.minecraft.command.CommandException;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;

import java.nio.charset.StandardCharsets;

import static net.minecraft.server.command.CommandManager.*;

public class ModCommands {
    static void warpId(CommandContext<ServerCommandSource> context, long value) {
        MinecraftServer s = context.getSource().getServer();
        boolean bl = ((MinecraftServerAccess)(s)).getDimensionProvider().rule("runtimeGenerationEnabled");
        boolean bl2 = NeitherPortalBlock.addDimension(s, value, bl);
        if (!bl) throw new CommandException(Text.translatable("commands.warp.runtime_disabled"));
        final ServerPlayerEntity self = context.getSource().getPlayer();
        if (self == null) throw new CommandException(Text.translatable("commands.warp.not_a_player"));
        if (bl2) self.increaseStat(ModStats.DIMS_OPENED_STAT, 1);
        self.increaseStat(ModStats.PORTALS_OPENED_STAT, 1);
        ((ServerPlayerEntityAccess)(self)).setWarpTimer(20, value);
    }

    public static long getDimensionSeed(String text, MinecraftServer s) {
        return getDimensionSeed(text, ((MinecraftServerAccess)(s)).getDimensionProvider());
    }
    public static long getDimensionSeed(NbtCompound compound, MinecraftServer s) {
        NbtList pages = compound.getList("pages", NbtElement.STRING_TYPE);
        return getDimensionSeed(pages.get(0).asString(), ((MinecraftServerAccess)(s)).getDimensionProvider());
    }

    public static long getDimensionSeed(String text, RandomProvider prov) {
        HashCode f = Hashing.sha256().hashString(text + prov.salt, StandardCharsets.UTF_8);
        return prov.rule("longArithmeticEnabled") ? f.asLong() & Long.MAX_VALUE : f.asInt() & Integer.MAX_VALUE;
    }

    public static void registerCommands() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(literal("warp-id")
                .requires(source -> source.hasPermissionLevel(2))
                .then(argument("id", IntegerArgumentType.integer()).executes(context -> {
                    final int value = IntegerArgumentType.getInteger(context, "id");
                    warpId(context, value);
                    return 1;
                }))));
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(literal("warp")
                .requires(source -> source.hasPermissionLevel(2))
                .then(argument("text", StringArgumentType.string()).executes(context -> {
                    final String text = StringArgumentType.getString(context, "text");
                    warpId(context, getDimensionSeed(text, context.getSource().getServer()));
                    return 1;
                }))));
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(literal("generate_configs")
                .requires(source -> source.hasPermissionLevel(2))
                .then(argument("pos_air", BlockPosArgumentType.blockPos()).executes(context -> 1)
                .then(argument("pos_stone", BlockPosArgumentType.blockPos()).executes(context -> {
                    BlockPos bp1 = BlockPosArgumentType.getBlockPos(context, "pos_air");
                    BlockPos bp2 = BlockPosArgumentType.getBlockPos(context, "pos_stone");
                    WorldView w = context.getSource().getWorld();
                    ConfigGenerator.generateAll(w, bp1, bp2);
                    return 1;
                })))));
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(literal("generate_loot")
                .requires(source -> source.hasPermissionLevel(2))
                .then(argument("seed", IntegerArgumentType.integer()).executes(context -> {
                    final int seed = IntegerArgumentType.getInteger(context, "seed");
                    RandomLootDrops.genAll(seed, context.getSource().getServer());
                    return 1;
                })))); //experimental command, will likely get separated into its own mod
    }
}
