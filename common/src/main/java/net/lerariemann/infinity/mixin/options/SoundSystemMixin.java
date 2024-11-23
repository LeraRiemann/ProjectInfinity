package net.lerariemann.infinity.mixin.options;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.lerariemann.infinity.access.InfinityOptionsAccess;
import net.lerariemann.infinity.options.InfinityOptions;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.SoundSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SoundSystem.class)
public class SoundSystemMixin {
    @ModifyExpressionValue(method = "getAdjustedPitch", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/sound/SoundInstance;getPitch()F"))
    float inj(float original) {
        InfinityOptions options =((InfinityOptionsAccess) MinecraftClient.getInstance()).infinity$getOptions();
        if (!options.isEmpty()) return options.getSoundPitch().apply(original);
        return original;
    }
}
