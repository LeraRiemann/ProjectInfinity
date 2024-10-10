package net.lerariemann.infinity.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.fabricmc.loader.api.FabricLoader;

public class ModMenuCompat implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        //Display Cloth Config screen if mod present, else error.
        if (FabricLoader.getInstance().isModLoaded("cloth-config")) return new ModConfigFactory();
        else {
            return parent -> null;
        }
    }
}
