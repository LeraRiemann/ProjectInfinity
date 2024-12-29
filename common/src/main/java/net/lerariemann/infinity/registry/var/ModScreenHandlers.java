package net.lerariemann.infinity.registry.var;

import dev.architectury.registry.menu.MenuRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.lerariemann.infinity.util.screen.F4Screen;
import net.lerariemann.infinity.util.screen.F4ScreenHandler;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.screen.ScreenHandlerType;

import static net.lerariemann.infinity.InfinityMod.MOD_ID;

public class ModScreenHandlers {
    private static final DeferredRegister<ScreenHandlerType<?>> SCREEN_HANDLERS =
            DeferredRegister.create(MOD_ID, RegistryKeys.SCREEN_HANDLER);

    public static final RegistrySupplier<ScreenHandlerType<F4ScreenHandler>> F4 =
            SCREEN_HANDLERS.register("f4", () -> MenuRegistry.ofExtended(F4ScreenHandler::new));

    public static void register() {
        SCREEN_HANDLERS.register();
        HandledScreens.register(F4.get(), F4Screen::new);
    }
}
