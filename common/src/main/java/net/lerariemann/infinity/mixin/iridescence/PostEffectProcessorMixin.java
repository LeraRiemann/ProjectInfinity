package net.lerariemann.infinity.mixin.iridescence;

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
    @Shadow
    private float lastTickDelta;

    /* Hook for the iridescence shader to receive the time uniform */
    @Inject(method="render", at = @At("HEAD"), cancellable = true)
    void inj(float tickDelta, CallbackInfo ci) {
        if (name.contains("infinity")) {
            if (tickDelta < this.lastTickDelta) {
                this.time += 1.0F - this.lastTickDelta;
                this.time += tickDelta;
            } else {
                this.time += tickDelta - this.lastTickDelta;
            }

            for(this.lastTickDelta = tickDelta; this.time > 2.0F; this.time -= 4000.0F) {
            }

            for(PostEffectPass postEffectPass : this.passes) {
                postEffectPass.render(this.time / 4000.0F);
            }
            ci.cancel();
        }
    }
}
