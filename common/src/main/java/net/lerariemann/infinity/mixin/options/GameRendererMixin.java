package net.lerariemann.infinity.mixin.options;

import net.lerariemann.infinity.access.GameRendererAccess;
import net.lerariemann.infinity.iridescence.Iridescence;
import net.lerariemann.infinity.options.InfinityOptions;
import net.lerariemann.infinity.util.loading.ShaderLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin implements GameRendererAccess {
    @Shadow
    @Final
    private ResourceManager resourceManager;
    @Shadow
    protected abstract void loadPostProcessor(Identifier id);

    @Override
    public void infinity$loadPP(Identifier id) {
        if (resourceManager.getResource(id).isPresent()) {
            loadPostProcessor(id);
        }
        else {
            MinecraftClient.getInstance().options.refreshResourcePacks(MinecraftClient.getInstance().getResourcePackManager());
        }
    }

    @Inject(method = "onCameraEntitySet", at = @At("TAIL"), cancellable = true)
    private void preserveShaderThirdPerson(CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (Iridescence.shouldApplyShader(client.player))
            ShaderLoader.reloadShaders(client, true, true);
        InfinityOptions options = InfinityOptions.ofClient();
        if (options.getShader().isEmpty()) {
            ci.cancel();
        } else {
            ShaderLoader.reloadShaders(MinecraftClient.getInstance(), true, false);
        }
    }
}
