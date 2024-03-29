package net.lerariemann.infinity.mixin;

import net.lerariemann.infinity.access.GameRendererAccess;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(GameRenderer.class)
public class GameRendererMixin implements GameRendererAccess {
    @Shadow void loadPostProcessor(Identifier id) {
    }

    @Override
    public void loadPP(Identifier id) {
        loadPostProcessor(id);
    }
}
