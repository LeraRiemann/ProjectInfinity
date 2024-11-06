package net.lerariemann.infinity.mixin.options;

import net.lerariemann.infinity.access.GameRendererAccess;
import net.lerariemann.infinity.access.InfinityOptionsAccess;
import net.lerariemann.infinity.options.InfinityOptions;
import net.lerariemann.infinity.options.ShaderLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin implements GameRendererAccess {
    @Shadow
    private void setPostProcessor(Identifier id) {
    }

    @Override
    public void projectInfinity$loadPP(Identifier id) {
        setPostProcessor(id);
    }

    @Inject(method = "onCameraEntitySet", at = @At("TAIL"), cancellable = true)
    private void preserveShaderThirdPerson(CallbackInfo ci) {
        InfinityOptions options = ((InfinityOptionsAccess)MinecraftClient.getInstance()).infinity$getOptions();
        if (options.getShader().isEmpty()) {
            ci.cancel();
        }
        else {
            ShaderLoader.reloadShaders(MinecraftClient.getInstance(), true);
        }
    }
}
