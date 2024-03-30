package net.lerariemann.infinity.mixin;

import net.lerariemann.infinity.access.MinecraftClientAccess;
import net.lerariemann.infinity.client.InfinityOptions;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin implements MinecraftClientAccess {
    @Unique
    public InfinityOptions infinityoptions;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void injected(RunArgs args, CallbackInfo ci) {
        infinityoptions = InfinityOptions.empty();
    }

    @Unique
    public InfinityOptions getInfinityOptions() {
        return infinityoptions;
    }

    @Unique
    public void setInfinityOptions(InfinityOptions options) {
        infinityoptions = options;
    }
}
