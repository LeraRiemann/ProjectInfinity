package net.lerariemann.infinity.config.fabric;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.architectury.platform.Platform;

public class ModMenuCompat implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        //Display Cloth Config screen if mod present, else error.
        if (Platform.isModLoaded("cloth-config")) return new ModConfigFactory();
        else {
            return parent -> null;
        }
    }
}
