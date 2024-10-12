package net.lerariemann.infinity.config.fabric;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import net.lerariemann.infinity.config.ClothConfigFactory;
import net.minecraft.client.gui.screen.Screen;

public class ModConfigFactory implements ConfigScreenFactory<Screen> {
    @Override
    public Screen create(Screen parent) {
        return ClothConfigFactory.create(parent);
    }
}
