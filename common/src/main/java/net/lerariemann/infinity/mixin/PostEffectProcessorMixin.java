package net.lerariemann.infinity.mixin;

import net.minecraft.client.gl.PostEffectPass;
import net.minecraft.client.gl.PostEffectProcessor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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

    @Inject(method="render", at = @At("HEAD"), cancellable = true)
    void inj(float tickDelta, CallbackInfo ci) {
        if (name.contains("infinity")) {
            time += tickDelta;

            while (time > 200.0F) {
                time -= 200.0F;
            }

            int i = 9728;

            for (PostEffectPass postEffectPass : passes) {
                int j = postEffectPass.getTexFilter();
                if (i != j) {
                    setTexFilter(j);
                    i = j;
                }

                postEffectPass.render(this.time / 200.0F);
            }

            this.setTexFilter(9728);
            ci.cancel();
        }
    }
}
