package net.lerariemann.infinity.var;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.lerariemann.infinity.access.ServerPlayerEntityAccess;
import net.lerariemann.infinity.access.MinecraftServerAccess;
import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.block.custom.NeitherPortalBlock;
import net.lerariemann.infinity.dimensions.RandomProvider;
import net.lerariemann.infinity.util.ConfigGenerator;
import net.lerariemann.infinity.util.RandomLootDrops;
import net.minecraft.command.CommandException;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

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

    public static boolean checkEnd(long d, MinecraftServer s) {
        return (d == ModCommands.getDimensionSeed("abatised redivides", s));
    }

    public static Identifier getIdentifier(long d, MinecraftServer s) {
        String s1 = ((MinecraftServerAccess)s).getDimensionProvider().easterizer.keyOf(d);
        return InfinityMod.getId(s1);
    }

    public static RegistryKey<World> getKey(long d, MinecraftServer s) {
        return checkEnd(d, s) ? World.END : RegistryKey.of(RegistryKeys.WORLD, getIdentifier(d, s));
    }

    public static long getDimensionSeed(String text, MinecraftServer s) {
        return getDimensionSeed(text, ((MinecraftServerAccess)(s)).getDimensionProvider());
    }
    public static long getDimensionSeed(NbtCompound compound, MinecraftServer s, Item item) {
        NbtList pages = compound.getList("pages", NbtElement.STRING_TYPE);
        String pagesString = pages.get(0).asString();
        if (pages.isEmpty()) {
            return getDimensionSeed("empty", ((MinecraftServerAccess)(s)).getDimensionProvider());
        }
        else if (item == Items.WRITTEN_BOOK) {
            String parsedString = pagesString.substring(pagesString.indexOf(':')+2, pagesString.length()-2);
            return getDimensionSeed(parsedString, ((MinecraftServerAccess)(s)).getDimensionProvider());
        }
        else {
            return getDimensionSeed(pagesString, ((MinecraftServerAccess)(s)).getDimensionProvider());
        }
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
