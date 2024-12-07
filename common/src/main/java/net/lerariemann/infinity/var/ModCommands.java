package net.lerariemann.infinity.var;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import dev.architectury.event.events.common.CommandRegistrationEvent;
import net.lerariemann.infinity.util.WarpLogic;

import static net.lerariemann.infinity.util.WarpLogic.*;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;


public class ModCommands {
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
                    WarpLogic.warp(context, WarpLogic.getIdentifier(text));
                    return 1;
                }))));

        CommandRegistrationEvent.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(literal("respawn")
                .requires(source -> source.hasPermissionLevel(2)).executes(context -> {
                    WarpLogic.respawnAlive(context.getSource().getPlayer());
                    return 1;
                })));
    }
}