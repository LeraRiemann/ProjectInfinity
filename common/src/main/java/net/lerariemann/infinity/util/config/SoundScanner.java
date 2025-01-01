package net.lerariemann.infinity.util.config;

import net.lerariemann.infinity.util.InfinityMethods;
import net.lerariemann.infinity.util.core.CommonIO;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
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

                    double length;
                    try {
                        length = calculateDuration(soundIds.get(id).getInputStream().readAllBytes());
                    } catch (IOException e) {
                        length = 600;
                    }
                    CommonIO.write(getJukeboxDef(songID, subtitleID, length), //datapack side
                            savingPath.resolve("datapacks/infinity/data/infinity/jukebox_song"),
                            arr.getLast() + ".json");
                });
        CommonIO.write(soundsForRP, savingPath.resolve("resourcepacks/infinity/assets/infinity"), "sounds.json");
        CommonIO.write(subtitlesForRP, savingPath.resolve("resourcepacks/infinity/assets/infinity/lang"), "en_us.json");
    }

    /**
     Getting a duration of an OGG file from its byte data; <a href="https://stackoverflow.com/a/44407355">implementation from StackOverflow</a>.
     */
    public static double calculateDuration(byte[] track) throws IOException {
        int rate = -1;
        int length = -1;
        int size = track.length;

        for (int i = size-1-8-2-4; i>=0 && length<0; i--) { //4 bytes for "OggS", 2 unused bytes, 8 bytes for length
            // Looking for length (value after last "OggS")
            if (
                    track[i]==(byte)'O'
                            && track[i+1]==(byte)'g'
                            && track[i+2]==(byte)'g'
                            && track[i+3]==(byte)'S'
            ) {
                byte[] byteArray = new byte[]{track[i+6],track[i+7],track[i+8],track[i+9],track[i+10],track[i+11],track[i+12],track[i+13]};
                ByteBuffer bb = ByteBuffer.wrap(byteArray);
                bb.order(ByteOrder.LITTLE_ENDIAN);
                length = bb.getInt(0);
            }
        }
        for (int i = 0; i<size-8-2-4 && rate<0; i++) {
            // Looking for rate (first value after "vorbis")
            if (
                    track[i]==(byte)'v'
                            && track[i+1]==(byte)'o'
                            && track[i+2]==(byte)'r'
                            && track[i+3]==(byte)'b'
                            && track[i+4]==(byte)'i'
                            && track[i+5]==(byte)'s'
            ) {
                byte[] byteArray = new byte[]{track[i+11],track[i+12],track[i+13],track[i+14]};
                ByteBuffer bb = ByteBuffer.wrap(byteArray);
                bb.order(ByteOrder.LITTLE_ENDIAN);
                rate = bb.getInt(0);
            }
        }
        return length / (double) rate;
    }

    private static @NotNull NbtCompound getJukeboxDef(String songID, String subtitleID, double length) {
        NbtCompound jukebox_def = new NbtCompound();
        NbtCompound sound_event = new NbtCompound();
        sound_event.putString("sound_id", "infinity:" + songID);
        jukebox_def.put("sound_event", sound_event);
        NbtCompound description = new NbtCompound();
        description.putString("translate", subtitleID);
        jukebox_def.put("description", description);
        jukebox_def.putFloat("length_in_seconds", (float)length);
        jukebox_def.putInt("comparator_output", 15);
        return jukebox_def;
    }
}
