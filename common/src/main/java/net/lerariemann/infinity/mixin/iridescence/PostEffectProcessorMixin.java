package net.lerariemann.infinity.mixin.iridescence;

import net.lerariemann.infinity.util.loading.ShaderLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.PostEffectPass;
import net.minecraft.client.gl.PostEffectProcessor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.time.LocalTime;
import java.util.List;

@Mixin(PostEffectProcessor.class)
public abstract class PostEffectProcessorMixin {
    @Shadow
    private float time;
    @Final
    @Shadow
    private String name;
    @Final
    @Shadow
    private List<PostEffectPass> passes;

    @Shadow protected abstract void setTexFilter(int texFilter);

    @Shadow public abstract void setUniforms(String name, float value);

    /* Hook for the iridescence shader to receive the time uniform */
    @Inject(method="render", at = @At("HEAD"), cancellable = true)
    void inj(float tickDelta, CallbackInfo ci) {
        if (name.contains("infinity")) {
            setUniforms("IridTimeSec", ((int)(LocalTime.now().toNanoOfDay() / 1000000)) / 1000.0f);
            setUniforms("IridLevel", ShaderLoader.iridLevel.get());
            setUniforms("IridProgress", (float)ShaderLoader.iridProgress.get());
            setUniforms("IridDistortion", MinecraftClient.getInstance().options.getDistortionEffectScale().getValue().floatValue());

            time += tickDelta;

            while (time > 2000.0F) {
                time -= 2000.0F;
            }

            int i = 9728;

            for (PostEffectPass postEffectPass : passes) {
                int j = postEffectPass.getTexFilter();
                if (i != j) {
                    setTexFilter(j);
                    i = j;
                }

                postEffectPass.render(this.time / 2000.0F);
            }

            this.setTexFilter(9728);
            ci.cancel();
        }
    }
}
