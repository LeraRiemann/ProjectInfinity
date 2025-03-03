package net.lerariemann.infinity.util.config;

import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.access.MinecraftServerAccess;
import net.lerariemann.infinity.mixin.SoundListMixin;
import net.lerariemann.infinity.registry.var.ModPayloads;
import net.lerariemann.infinity.util.InfinityMethods;
import net.lerariemann.infinity.util.core.CommonIO;
import net.lerariemann.infinity.util.core.RandomProvider;
import net.lerariemann.infinity.util.loading.DimensionGrabber;
import net.minecraft.block.jukebox.JukeboxSong;
import net.minecraft.client.MinecraftClient;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.resource.Resource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.WorldSavePath;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

/**
 * Pipeline for generating custom sound events and jukebox song definitions for every music track in the game.
 */
public record SoundScanner(Map<Identifier, Resource> soundIds) {
    /** Holds a map which allows to get the list of all sound IDs in existence and .ogg data for each.
     * Seeded by {@link SoundListMixin} on client launch. */
    public static SoundScanner instance;
    public static boolean isPreloaded() {
        return instance != null;
    }
    public static Stream<Identifier> getMatchingLoadedIds() {
        if (isPreloaded()) return instance.soundIds.keySet().stream().filter(s -> s.getPath().contains("music") && !s.getPath().contains("record"));
        return Stream.of();
    }

    /**
     * <p>On player connect, the server sends a {@link ModPayloads.SoundPackS2CPayload} payload to the client.
     * <p>If the server already contains data upon which the client should create its resource pack, it holds this data.
     * <p>Otherwise, this payload is empty, and the server relies on the client to create it and send it back to the server for future use. */
    public static void unpackDownloadedPack(NbtCompound songIds, MinecraftClient cl) {
        //the client unpacks a non-empty payload only when needed, meaning only if it doesn't have necessary files yet
        if (!songIds.isEmpty() && !Files.exists(cl.getResourcePackDir().resolve("infinity/assets/infinity/sounds.json"))) {
            cl.execute(() -> saveResourcePack(cl, songIds.getList("entries", NbtElement.STRING_TYPE).stream()
                    .map(NbtElement::asString).map(Identifier::of), false));
        }
        else if (isPreloaded()) {
            cl.execute(() -> {
                NbtCompound jukeboxes = saveResourcePack(cl, getMatchingLoadedIds(), true);
                NbtCompound res = new NbtCompound();
                NbtList songIdsList = new NbtList();
                getMatchingLoadedIds().forEach(id -> songIdsList.add(NbtString.of(id.toString())));
                res.put("entries", songIdsList);
                res.put("jukeboxes", jukeboxes);
                ModPayloads.sendJukeboxesPayload(res);
            });
        }
    }
    /**
     * Generating and saving a resource pack from a stream of identifiers that correspond to music tracks.
     * @param sendJukeboxes if this is true, the method also generates and returns data that needs to be sent to the server
     * to seed it with corresponding jukebox song definitions.*/
    public static NbtCompound saveResourcePack(MinecraftClient client, Stream<Identifier> songIds, boolean sendJukeboxes) {
        NbtCompound soundsForRP = new NbtCompound();
        NbtCompound subtitlesForRP = new NbtCompound();
        NbtCompound jukeboxes = new NbtCompound();
        songIds.forEach(id -> {
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
            soundsForRP.put(songID, soundForRP);

            if (sendJukeboxes) {
                if (!isPreloaded()) return;
                double length;
                try {
                    length = calculateDuration(instance.soundIds.get(id).getInputStream().readAllBytes());
                } catch (IOException e) {
                    length = 600;
                }
                jukeboxes.put(arr.getLast(), getJukeboxDef(songID, subtitleID, length));
            }
        });
        CommonIO.write(soundsForRP, client.getResourcePackDir().resolve("infinity/assets/infinity"), "sounds.json");
        CommonIO.write(subtitlesForRP, client.getResourcePackDir().resolve("infinity/assets/infinity/lang"), "en_us.json");
        return jukeboxes;
    }

    /** Receiver for a C2S {@link ModPayloads.JukeboxesC2SPayload} payload, which holds data to send to clients in the future for them to
     * generate custom sound resource packs, as well as jukebox song definitions corresponding to this data. */
    public static void unpackUploadedJukeboxes(MinecraftServer server, NbtCompound data) {
        if (!RandomProvider.rule("useSoundSyncPackets")) return;
        if (!data.contains("jukeboxes") || !data.contains("entries")) return;
        if (Files.exists(server.getSavePath(WorldSavePath.DATAPACKS).resolve("client_sound_pack_data.json"))) return;

        NbtCompound allJukeboxes = data.getCompound("jukeboxes");
        Path pathJukeboxes = server.getSavePath(WorldSavePath.DATAPACKS).resolve("infinity/data/infinity/jukebox_song");
        for (String key: allJukeboxes.getKeys()) {
            if (allJukeboxes.get(key) instanceof NbtCompound jukebox) {
                CommonIO.write(jukebox, pathJukeboxes, key + ".json");
            }
        }
        grabJukeboxes(server);

        data.remove("jukeboxes");
        NbtCompound packData = new NbtCompound();
        packData.put("entries", data.get("entries"));
        CommonIO.write(packData, server.getSavePath(WorldSavePath.DATAPACKS), "client_sound_pack_data.json");
    }
    /** Injects freshly received jukebox song definitions into the server's registries, config files and {@link RandomProvider}. */
    public static void grabJukeboxes(MinecraftServer server) {
        Path pathJukeboxes = server.getSavePath(WorldSavePath.DATAPACKS).resolve("infinity/data/infinity/jukebox_song");
        InfinityMod.LOGGER.info("grabbing jukeboxes");
        DimensionGrabber grabber = new DimensionGrabber(server.getRegistryManager());
        grabber.buildGrabber(JukeboxSong.CODEC, RegistryKeys.JUKEBOX_SONG).grabAll(pathJukeboxes);
        grabber.close();
        if (!((MinecraftServerAccess)server).infinity$needsInvocation()) {
            ConfigFactory.of(server.getRegistryManager().get(RegistryKeys.JUKEBOX_SONG)).generate("misc", "jukeboxes");
            InfinityMod.updateProvider(server);
        }
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
