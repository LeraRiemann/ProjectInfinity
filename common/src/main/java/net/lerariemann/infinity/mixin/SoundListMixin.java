package net.lerariemann.infinity.mixin;

import net.lerariemann.infinity.util.config.SoundScanner;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(targets = "net.minecraft.client.sound.SoundManager$SoundList")
public class SoundListMixin {
    @Shadow private Map<Identifier, Resource> foundSounds;

    @Inject(method = "findSounds(Lnet/minecraft/resource/ResourceManager;)V",
    at = @At("RETURN"))
    void inj(ResourceManager resourceManager, CallbackInfo ci) {
        SoundScanner.instance = new SoundScanner(foundSounds.keySet());
    }
}
