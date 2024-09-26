package net.lerariemann.infinity.var;

import net.lerariemann.infinity.InfinityMod;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class ModSounds {
    public static final Identifier IVORY_MUSIC_HOPE = InfinityMod.getId("music.ivory.hope_instilled");
    public static final Identifier IVORY_MUSIC_CHALLENGER = InfinityMod.getId("music.ivory.challenger");
    public static SoundEvent IVORY_MUSIC_HOPE_EVENT = SoundEvent.of(IVORY_MUSIC_HOPE);
    public static SoundEvent IVORY_MUSIC_CHALLENGER_EVENT = SoundEvent.of(IVORY_MUSIC_CHALLENGER);

    public static void registerSounds(){
        Registry.register(Registries.SOUND_EVENT, IVORY_MUSIC_HOPE, IVORY_MUSIC_HOPE_EVENT);
        Registry.register(Registries.SOUND_EVENT, IVORY_MUSIC_CHALLENGER, IVORY_MUSIC_CHALLENGER_EVENT);
    }
}
