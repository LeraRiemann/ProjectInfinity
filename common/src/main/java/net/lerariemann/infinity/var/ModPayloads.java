package net.lerariemann.infinity.var;

import dev.architectury.networking.NetworkManager;
import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import net.lerariemann.infinity.access.InfinityOptionsAccess;
import net.lerariemann.infinity.access.WorldRendererAccess;
import net.lerariemann.infinity.loading.DimensionGrabber;
import net.lerariemann.infinity.options.InfinityOptions;
import net.lerariemann.infinity.util.InfinityMethods;
import net.lerariemann.infinity.util.ShaderLoader;
import net.lerariemann.infinity.util.CommonIO;
import net.minecraft.client.MinecraftClient;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

import java.nio.file.Path;
import java.util.Objects;

public class ModPayloads {

    public static MinecraftClient client(Object context) {
        return MinecraftClient.getInstance();
    }

    public record WorldAddPayload(Identifier world_id, NbtCompound world_data) implements CustomPayload {
        public static final CustomPayload.Id<WorldAddPayload> ID = new CustomPayload.Id<>(InfinityMethods.getId("add_world"));
        public static final PacketCodec<RegistryByteBuf, WorldAddPayload> CODEC = PacketCodec.tuple(
                Identifier.PACKET_CODEC, WorldAddPayload::world_id,
                PacketCodecs.NBT_COMPOUND, WorldAddPayload::world_data,
                WorldAddPayload::new);
        @Override
        public CustomPayload.Id<? extends CustomPayload> getId() {
            return ID;
        }
    }
    public static void addWorld(WorldAddPayload payload, Object context) {
        client(context).execute(() ->
                (new DimensionGrabber(Objects.requireNonNull(client(context).getNetworkHandler()).getRegistryManager()))
                        .grab_dim_for_client(payload.world_id, payload.world_data));
    }

    public record BiomeAddPayload(Identifier biome_id, NbtCompound biome_data) implements CustomPayload {
        public static final CustomPayload.Id<BiomeAddPayload> ID = new CustomPayload.Id<>(InfinityMethods.getId("add_biome"));
        public static final PacketCodec<RegistryByteBuf, BiomeAddPayload> CODEC = PacketCodec.tuple(
                Identifier.PACKET_CODEC, BiomeAddPayload::biome_id,
                PacketCodecs.NBT_COMPOUND, BiomeAddPayload::biome_data,
                BiomeAddPayload::new);
        @Override
        public CustomPayload.Id<? extends CustomPayload> getId() {
            return ID;
        }
    }
    public static void addBiome(BiomeAddPayload payload, Object context) {
        client(context).execute(() ->
                (new DimensionGrabber(Objects.requireNonNull(client(context).getNetworkHandler()).getRegistryManager()))
                        .grab_biome_for_client(payload.biome_id, payload.biome_data));
    }

    public record ShaderRePayload(NbtCompound shader_data) implements CustomPayload {
        public static final CustomPayload.Id<ShaderRePayload> ID = new CustomPayload.Id<>(InfinityMethods.getId("reload_shader"));
        public static final PacketCodec<RegistryByteBuf, ShaderRePayload> CODEC = PacketCodec.tuple(
                PacketCodecs.NBT_COMPOUND, ShaderRePayload::shader_data,
                ShaderRePayload::new);
        @Override
        public CustomPayload.Id<? extends CustomPayload> getId() {
            return ID;
        }
    }
    public static void receiveShader(ShaderRePayload payload, Object context) {
        InfinityOptions options = new InfinityOptions(payload.shader_data);
        MinecraftClient client = client(context);
        ((InfinityOptionsAccess)client).infinity$setOptions(options);
        NbtCompound shader = options.getShader();
        boolean bl = shader.isEmpty();
        if (bl) client.execute(() -> ShaderLoader.reloadShaders(client, false));
        else {
            client.execute(() -> {
                CommonIO.write(shader, ShaderLoader.shaderDir(client), ShaderLoader.FILENAME);
                ShaderLoader.reloadShaders(client, true);
                if (!resourcesReloaded) {
                    client.reloadResources();
                    resourcesReloaded = true;
                }
            });
        }
    }

    public static boolean resourcesReloaded = Path.of(Platform.getGameFolder() + "/resourcepacks/infinity/assets/infinity/shaders").toFile().exists();

    public record StarsRePayLoad() implements CustomPayload {
        public static final StarsRePayLoad INSTANCE = new StarsRePayLoad();
        public static final CustomPayload.Id<StarsRePayLoad> ID = new CustomPayload.Id<>(InfinityMethods.getId("reload_stars"));
        public static final PacketCodec<RegistryByteBuf, StarsRePayLoad> CODEC = PacketCodec.unit(INSTANCE);
        @Override
        public CustomPayload.Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    public static void receiveStars(StarsRePayLoad payload, Object context) {
        ((WorldRendererAccess)(client(context).worldRenderer)).infinity$setNeedsStars(true);
    }

    public static ShaderRePayload setShaderFromWorld(ServerWorld destination) {
        if (destination == null) return new ShaderRePayload(new NbtCompound());
        return new ShaderRePayload(InfinityOptions.access(destination).data());
    }

    public static void registerPayloadsServer() {
        if (Platform.getEnvironment() == Env.SERVER) {
            NetworkManager.registerS2CPayloadType(WorldAddPayload.ID, WorldAddPayload.CODEC);
            NetworkManager.registerS2CPayloadType(BiomeAddPayload.ID, BiomeAddPayload.CODEC);
            NetworkManager.registerS2CPayloadType(ShaderRePayload.ID, ShaderRePayload.CODEC);
            NetworkManager.registerS2CPayloadType(StarsRePayLoad.ID, StarsRePayLoad.CODEC);
        }
    }

    public static void registerPayloadsClient() {
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, WorldAddPayload.ID, WorldAddPayload.CODEC, ModPayloads::addWorld);
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, BiomeAddPayload.ID, BiomeAddPayload.CODEC, ModPayloads::addBiome);
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, ShaderRePayload.ID, ShaderRePayload.CODEC, ModPayloads::receiveShader);
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, StarsRePayLoad.ID, StarsRePayLoad.CODEC, ModPayloads::receiveStars);
    }
}