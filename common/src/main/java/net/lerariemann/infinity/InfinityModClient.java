package net.lerariemann.infinity;

import dev.architectury.registry.client.keymappings.KeyMappingRegistry;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.lerariemann.infinity.item.F4Item;
import net.lerariemann.infinity.registry.core.ModEntities;
import net.lerariemann.infinity.registry.core.ModItems;
import net.lerariemann.infinity.registry.var.ModPayloads;
import net.lerariemann.infinity.registry.var.ModScreenHandlers;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import org.lwjgl.glfw.GLFW;

public class InfinityModClient {
    public static KeyBinding f4ConfigKey = new KeyBinding("key.infinity.f4",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_F4,
            "key.categories.misc");

    public static void initializeClient() {
        ModEntities.registerEntityRenderers();
        ModScreenHandlers.register();
        KeyMappingRegistry.register(f4ConfigKey);
        ModPayloads.registerS2CPacketsReceivers();
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (f4ConfigKey.wasPressed()) if (client.player != null
                    && client.player.getStackInHand(Hand.MAIN_HAND).isOf(ModItems.F4.get())) {
                ClientPlayNetworking.send(new ModPayloads.F4DeployingPacket());
                TypedActionResult<ItemStack> result = F4Item.deploy(client.world, client.player, Hand.MAIN_HAND);
                client.player.setStackInHand(Hand.MAIN_HAND, result.getValue());
            }
        });
    }
}