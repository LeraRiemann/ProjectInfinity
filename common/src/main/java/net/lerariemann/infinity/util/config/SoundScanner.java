package net.lerariemann.infinity.util.config;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.util.Identifier;

import java.nio.file.Path;
import java.util.Set;

public record SoundScanner(Set<Identifier> soundIds) {
    public static SoundScanner instance;

    public static boolean isPreloaded() {
        return instance != null;
    }
    public static void save(Path savingPath) {
        if (isPreloaded()) instance.uploadTo(savingPath);
    }
    public static Set<Identifier> getLoadedIds() {
        if (isPreloaded()) return instance.soundIds;
        return Set.of();
    }
    private void uploadTo(Path savingPath) {
        NbtCompound allEvents = new NbtCompound();
        soundIds.stream().map(Identifier::toString).filter(s -> s.contains("music")).forEach(str -> {
            NbtCompound soundEvent = new NbtCompound();
            String name = str.replace(":", ".").replace("/", ".");
            NbtList sounds = new NbtList();
            sounds.add(NbtString.of(str));
            soundEvent.put("sounds", sounds);
            soundEvent.putString("subtitle", "subtitles.gen."+name);
            allEvents.put(name, soundEvent);
        });
    }
}
