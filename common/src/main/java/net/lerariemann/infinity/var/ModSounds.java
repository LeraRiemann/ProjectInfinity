package net.lerariemann.infinity.var;

import dev.architectury.registry.registries.DeferredRegister;
import net.lerariemann.infinity.util.InfinityMethods;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

import static net.lerariemann.infinity.InfinityMod.MOD_ID;

public class ModSounds {
    public static final Identifier IVORY_MUSIC_HOPE = InfinityMethods.getId("music.ivory.hope_instilled");
    public static final Identifier IVORY_MUSIC_CHALLENGER = InfinityMethods.getId("music.ivory.challenger");
    public static SoundEvent IVORY_MUSIC_HOPE_EVENT = SoundEvent.of(IVORY_MUSIC_HOPE);
    public static SoundEvent IVORY_MUSIC_CHALLENGER_EVENT = SoundEvent.of(IVORY_MUSIC_CHALLENGER);
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(MOD_ID, RegistryKeys.SOUND_EVENT);

    public static void registerSounds(){
        SOUNDS.register(IVORY_MUSIC_HOPE, () -> IVORY_MUSIC_HOPE_EVENT);
        SOUNDS.register(IVORY_MUSIC_CHALLENGER, () -> IVORY_MUSIC_CHALLENGER_EVENT);
        SOUNDS.register();
    }
}
