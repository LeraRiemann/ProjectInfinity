package net.lerariemann.infinity.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import net.minecraft.client.gui.screen.Screen;

public class ModConfigFactory implements ConfigScreenFactory<Screen> {
    @Override
    public Screen create(Screen parent) {
        return ClothConfigFactory.create(parent);
    }
}
