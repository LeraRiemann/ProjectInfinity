package net.lerariemann.infinity;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.lerariemann.infinity.registry.core.ModEntities;
import net.lerariemann.infinity.registry.core.ModItems;
import net.lerariemann.infinity.registry.var.ModPayloads;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.math.noise.DoublePerlinNoiseSampler;
import net.minecraft.util.math.random.CheckedRandom;
import org.lwjgl.glfw.GLFW;

public class InfinityModClient {
    public final static DoublePerlinNoiseSampler sampler = DoublePerlinNoiseSampler.create(new CheckedRandom(0L), -3, 1.0, 1.0, 1.0, 0.0);
    public static KeyBinding f4ConfigKey;

    public static void initializeClient() {
        ModPayloads.registerPayloadsClient();
        ModEntities.registerEntityRenderers();
        f4ConfigKey = KeyBindingHelper.registerKeyBinding(
                new KeyBinding("key.infinity.f4_config",
                        InputUtil.Type.KEYSYM,
                        GLFW.GLFW_KEY_F4,
                        "key.categories.misc"));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (f4ConfigKey.wasPressed()) if (client.player != null
                    && client.player.getStackInHand(Hand.MAIN_HAND).isOf(ModItems.F4.get())) {
                client.player.sendMessage(Text.literal("This will open a config screen in the future"), false);
            }
        });
    }
}
