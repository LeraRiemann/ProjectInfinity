package net.lerariemann.infinity.var;

import dev.architectury.registry.registries.DeferredRegister;
import net.lerariemann.infinity.InfinityMod;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

import static net.lerariemann.infinity.InfinityMod.MOD_ID;
import static net.lerariemann.infinity.PlatformMethods.freeze;
import static net.lerariemann.infinity.PlatformMethods.unfreeze;

public class ModSounds {
    public static final Identifier IVORY_MUSIC_HOPE = InfinityMod.getId("music.ivory.hope_instilled");
    public static final Identifier IVORY_MUSIC_CHALLENGER = InfinityMod.getId("music.ivory.challenger");
    public static SoundEvent IVORY_MUSIC_HOPE_EVENT = SoundEvent.of(IVORY_MUSIC_HOPE);
    public static SoundEvent IVORY_MUSIC_CHALLENGER_EVENT = SoundEvent.of(IVORY_MUSIC_CHALLENGER);
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(MOD_ID, RegistryKeys.SOUND_EVENT);

    public static void registerSounds(){
        SOUNDS.register(IVORY_MUSIC_HOPE, () -> IVORY_MUSIC_HOPE_EVENT);
        SOUNDS.register(IVORY_MUSIC_CHALLENGER, () -> IVORY_MUSIC_CHALLENGER_EVENT);
    }
}
