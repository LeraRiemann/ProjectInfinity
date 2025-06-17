package net.lerariemann.infinity.registry.var;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import dev.architectury.event.events.common.CommandRegistrationEvent;
import dev.architectury.platform.Platform;
import net.lerariemann.infinity.util.InfinityMethods;
import net.lerariemann.infinity.util.teleport.WarpLogic;
import net.minecraft.command.argument.DimensionArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Random;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class ModCommands {
    public static final DynamicCommandExceptionType MALFORM_IDENTIFIER_EXCEPTION = new DynamicCommandExceptionType(
            id -> Text.stringifiedTranslatable("error.infinity.warp.malformed_identifier", id)
    );
    public static final DynamicCommandExceptionType TIMEBOMBED_EXCEPTION = new DynamicCommandExceptionType(
            id -> Text.stringifiedTranslatable("error.infinity.warp.timebombed", id)
    );

    public static void registerCommands() {
        String warp;
        if (Platform.isModLoaded("ftbessentials") || Platform.isModLoaded("fabric-essentials")) // FTB/Fabric Essentials add their own warp command that plays havoc with ours.
            warp = "dimwarp";
        else {
            warp = "warp";
        }
        CommandRegistrationEvent.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(literal(warp)
                .requires(source -> source.hasPermissionLevel(2))
                .then(
                        CommandManager.literal("random").executes((context -> {
                            final long text = InfinityMethods.getRandomSeed(new Random());
                            WarpLogic.requestWarpById(context, text);
                            return 1;
                        }))
                )
                .then(
                        CommandManager.literal("existing").then(
                                argument("existing", DimensionArgumentType.dimension()).executes(context -> {
                                    final Identifier identifier = context.getArgument("existing", Identifier.class);
                                    WarpLogic.requestWarpToExisting(context, identifier);
                                    return 1;
                                })
                        )
                )
                .then(
                        CommandManager.literal("id").then(
                                argument("id", IntegerArgumentType.integer())
                                        .executes(context -> {
                                            final int value = IntegerArgumentType.getInteger(context, "id");
                                            WarpLogic.requestWarpById(context, value);
                                            return 1;
                                        })
                        )
                )
                .then(
                        argument("text", StringArgumentType.string())
                                .executes(context -> {
                                    final String text = StringArgumentType.getString( context, "text");
                                    WarpLogic.requestWarpByText(context, text);
                                    return 1;
                                })
                )
        ));
        CommandRegistrationEvent.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(literal("respawn")
                .requires(source -> source.hasPermissionLevel(2)).executes(context -> {
                    WarpLogic.respawnAlive(context.getSource().getPlayer());
                    return 1;
                })));
    }
}
