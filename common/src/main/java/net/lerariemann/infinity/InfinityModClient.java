package net.lerariemann.infinity;

import dev.architectury.registry.client.keymappings.KeyMappingRegistry;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.lerariemann.infinity.item.function.F4Screen;
import net.lerariemann.infinity.registry.core.ModEntities;
import net.lerariemann.infinity.registry.core.ModItems;
import net.lerariemann.infinity.registry.var.ModPayloads;
import net.lerariemann.infinity.registry.var.ModScreenHandlers;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Hand;
import net.minecraft.util.math.noise.DoublePerlinNoiseSampler;
import net.minecraft.util.math.random.CheckedRandom;
import org.lwjgl.glfw.GLFW;

public class InfinityModClient {
    public final static DoublePerlinNoiseSampler sampler = DoublePerlinNoiseSampler.create(new CheckedRandom(0L), -3, 1.0, 1.0, 1.0, 0.0);
    public static KeyBinding f4ConfigKey = new KeyBinding("key.infinity.f4_config",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_F4,
            "key.categories.misc");

    public static void initializeClient() {
        ModPayloads.registerPayloadsClient();
        ModEntities.registerEntityRenderers();
        ModScreenHandlers.register();
        KeyMappingRegistry.register(f4ConfigKey);
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (f4ConfigKey.wasPressed()) if (client.player != null
                    && client.player.getStackInHand(Hand.MAIN_HAND).isOf(ModItems.F4.get())) {
                client.setScreen(F4Screen.of(client.player));
            }
        });
    }
}
