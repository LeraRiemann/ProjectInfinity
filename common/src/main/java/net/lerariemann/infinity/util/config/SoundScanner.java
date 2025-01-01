package net.lerariemann.infinity.util.config;

import net.lerariemann.infinity.util.InfinityMethods;
import net.lerariemann.infinity.util.core.CommonIO;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

public record SoundScanner(Map<Identifier, Resource> soundIds) {
    public static SoundScanner instance;

    public static boolean isPreloaded() {
        return instance != null;
    }
    public static boolean save(Path savingPath) {
        if (isPreloaded()) {
            instance.uploadTo(savingPath);
            return true;
        }
        return false;
    }
    public static Stream<Identifier> getMatchingLoadedIds() {
        if (isPreloaded()) return instance.soundIds.keySet().stream().filter(s -> s.getPath().contains("music"));
        return Stream.of();
    }
    public static Set<Identifier> getLoadedIds() {
        if (isPreloaded()) return instance.soundIds.keySet();
        return Set.of();
    }
    private void uploadTo(Path savingPath) {
        NbtCompound soundsForRP = new NbtCompound();
        NbtCompound subtitlesForRP = new NbtCompound();
        soundIds.keySet().stream().filter(s -> s.getPath().contains("music"))
                .forEach(id -> {
                    String str = id.toString().replace(".ogg", "").replace("sounds/", "");
                    List<String> arr = Arrays.stream(str.split("[:/]")).toList(); //preloading IDs
                    String songID = "disc." + arr.getFirst() + "." + arr.getLast();
                    String subtitleID = "infinity:subtitles." + songID;
                    String subtitleData = InfinityMethods.formatAsTitleCase(arr.getFirst() + " - " + arr.getLast());

                    NbtList soundForRPList = new NbtList();
                    soundForRPList.add(NbtString.of(str));
                    NbtCompound soundForRP = new NbtCompound();
                    soundForRP.put("sounds", soundForRPList);
                    soundForRP.putString("subtitle", subtitleID);
                    subtitlesForRP.putString(subtitleID, subtitleData);
                    soundsForRP.put(songID, soundForRP); //resourcepack side

                    CommonIO.write(getJukeboxDef(songID, subtitleID),
                            savingPath.resolve("datapacks/infinity/data/infinity/jukebox_song"),
                            arr.getLast() + ".json");
                });
        CommonIO.write(soundsForRP, savingPath.resolve("resourcepacks/infinity/assets/infinity"), "sounds.json");
        CommonIO.write(subtitlesForRP, savingPath.resolve("resourcepacks/infinity/assets/infinity/lang"), "en_us.json");
    }

    private static @NotNull NbtCompound getJukeboxDef(String songID, String subtitleID) {
        NbtCompound jukebox_def = new NbtCompound();
        NbtCompound sound_event = new NbtCompound();
        sound_event.putString("sound_id", "infinity:" + songID);
        jukebox_def.put("sound_event", sound_event);
        NbtCompound description = new NbtCompound();
        description.putString("translate", subtitleID);
        jukebox_def.put("description", description);
        jukebox_def.putFloat("length_in_seconds", 600);
        jukebox_def.putInt("comparator_output", 15);
        return jukebox_def;
    }
}
