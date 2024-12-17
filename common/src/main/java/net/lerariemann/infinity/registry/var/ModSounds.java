package net.lerariemann.infinity.registry.var;

import dev.architectury.registry.registries.DeferredRegister;
import net.lerariemann.infinity.util.InfinityMethods;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

import static net.lerariemann.infinity.InfinityMod.MOD_ID;

public class ModSounds {
    public static final Identifier IVORY_MUSIC_HOPE = InfinityMethods.getId("music.ivory.hope_instilled");
    public static final Identifier IVORY_MUSIC_CHALLENGER = InfinityMethods.getId("music.ivory.challenger");
    public static final Identifier BACKPORT_VAULT = InfinityMethods.getId("backport.vault.open_shutter");
    public static SoundEvent IVORY_MUSIC_HOPE_EVENT = SoundEvent.of(IVORY_MUSIC_HOPE);
    public static SoundEvent IVORY_MUSIC_CHALLENGER_EVENT = SoundEvent.of(IVORY_MUSIC_CHALLENGER);
    public static SoundEvent BACKPORT_VAULT_EVENT = SoundEvent.of(BACKPORT_VAULT);
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(MOD_ID, RegistryKeys.SOUND_EVENT);

    public static void registerSounds(){
        SOUNDS.register(IVORY_MUSIC_HOPE, () -> IVORY_MUSIC_HOPE_EVENT);
        SOUNDS.register(IVORY_MUSIC_CHALLENGER, () -> IVORY_MUSIC_CHALLENGER_EVENT);
        SOUNDS.register(BACKPORT_VAULT, () -> BACKPORT_VAULT_EVENT);
        SOUNDS.register();
    }
}
