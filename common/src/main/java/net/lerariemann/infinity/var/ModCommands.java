package net.lerariemann.infinity.var;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import dev.architectury.event.events.common.CommandRegistrationEvent;
import net.lerariemann.infinity.util.ConfigGenerator;
import net.lerariemann.infinity.util.WarpLogic;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class ModCommands {
    public static void registerCommands() {
        CommandRegistrationEvent.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(literal("warp-id")
                .requires(source -> source.hasPermissionLevel(2))
                .then(argument("id", IntegerArgumentType.integer()).executes(context -> {
                    final int value = IntegerArgumentType.getInteger(context, "id");
                    WarpLogic.warpId(context, value);
                    return 1;
                }))));
        CommandRegistrationEvent.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(literal("warp")
                .requires(source -> source.hasPermissionLevel(2))
                .then(argument("text", StringArgumentType.string()).executes(context -> {
                    final String text = StringArgumentType.getString(context, "text");
                    WarpLogic.warp(context, WarpLogic.getIdentifier(text, context.getSource().getServer()));
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
