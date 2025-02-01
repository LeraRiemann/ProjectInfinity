package net.lerariemann.infinity.util;

import net.lerariemann.infinity.util.screen.F4Screen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;

public class ClientMethods {
    public static void setF4Screen(PlayerEntity player) {
        if (player instanceof ClientPlayerEntity clientPlayer) {
            MinecraftClient.getInstance().setScreen(F4Screen.of(clientPlayer));
        }
    }
}
