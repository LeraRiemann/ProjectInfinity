package net.lerariemann.infinity.mixin.options;

import net.lerariemann.infinity.access.InfinityOptionsAccess;
import net.lerariemann.infinity.options.InfinityOptions;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin implements InfinityOptionsAccess {
    @Shadow
    public static MinecraftClient getInstance() {
        return null;
    }
    @Shadow
    public ClientWorld world;

    @Unique
    public InfinityOptions infinity$options;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void injected(RunArgs args, CallbackInfo ci) {
        infinity$options = InfinityOptions.empty();
    }

    @Unique
    public InfinityOptions infinity$getOptions() {
        return infinity$options;
    }

    @Unique
    public void infinity$setOptions(InfinityOptions options) {
        infinity$options = options;
        ((InfinityOptionsAccess) world).infinity$setOptions(options);
    }
}
