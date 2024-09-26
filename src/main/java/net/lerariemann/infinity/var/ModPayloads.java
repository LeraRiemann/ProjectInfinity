package net.lerariemann.infinity.var;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.access.InfinityOptionsAccess;
import net.lerariemann.infinity.access.WorldRendererAccess;
import net.lerariemann.infinity.loading.DimensionGrabber;
import net.lerariemann.infinity.options.InfinityOptions;
import net.lerariemann.infinity.options.ShaderLoader;
import net.lerariemann.infinity.util.CommonIO;
import net.minecraft.client.MinecraftClient;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public class ModPayloads {
    public record WorldAddPayload(Identifier world_id, NbtCompound optiondata) implements CustomPayload {
        public static final CustomPayload.Id<WorldAddPayload> ID = new CustomPayload.Id<>(InfinityMod.getId("reload_worlds"));
        public static final PacketCodec<RegistryByteBuf, WorldAddPayload> CODEC = PacketCodec.tuple(
                Identifier.PACKET_CODEC, WorldAddPayload::world_id,
                PacketCodecs.NBT_COMPOUND, WorldAddPayload::optiondata,
                WorldAddPayload::new);
        @Override
        public CustomPayload.Id<? extends CustomPayload> getId() {
            return ID;
        }
    }
    public static void addWorld(WorldAddPayload payload, ClientPlayNetworking.Context context) {
        Identifier id = payload.world_id;
        NbtCompound optiondata = payload.optiondata;
        NbtCompound dimdata = optiondata.getCompound("dimdata");
        NbtList biomeslist = optiondata.getList("biomes", NbtElement.COMPOUND_TYPE);
        List<Identifier> biomeids = new ArrayList<>();
        List<NbtCompound> biomes = new ArrayList<>();
        for (NbtElement e: biomeslist) if (e instanceof NbtCompound biome) {
            biomeids.add(InfinityMod.getId(biome.getString("id")));
            biomes.add(biome.getCompound("data"));
        }
        context.client().execute(() -> (new DimensionGrabber(context.client().getNetworkHandler().getRegistryManager())).grab_for_client(id, dimdata, biomeids, biomes));
    }

    public record ShaderRePayload(NbtCompound shader_data) implements CustomPayload {
        public static final CustomPayload.Id<ShaderRePayload> ID = new CustomPayload.Id<>(InfinityMod.getId("reload_shader"));
        public static final PacketCodec<RegistryByteBuf, ShaderRePayload> CODEC = PacketCodec.tuple(
                PacketCodecs.NBT_COMPOUND, ShaderRePayload::shader_data,
                ShaderRePayload::new);
        @Override
        public CustomPayload.Id<? extends CustomPayload> getId() {
            return ID;
        }
    }
    public static void receiveShader(ShaderRePayload payload, ClientPlayNetworking.Context context) {
        InfinityOptions options = new InfinityOptions(payload.shader_data);
        MinecraftClient client = context.client();
        ((InfinityOptionsAccess)client).projectInfinity$setInfinityOptions(options);
        NbtCompound shader = options.getShader();
        boolean bl = shader.isEmpty();
        if (bl) client.execute(() -> ShaderLoader.reloadShaders(client, false));
        else {
            client.execute(() -> {
                CommonIO.write(shader, ShaderLoader.shaderDir(client), ShaderLoader.FILENAME);
                ShaderLoader.reloadShaders(client, true);
            });
        }
    }

    public record StarsRePayLoad() implements CustomPayload {
        public static final StarsRePayLoad INSTANCE = new StarsRePayLoad();
        public static final CustomPayload.Id<StarsRePayLoad> ID = new CustomPayload.Id<>(InfinityMod.getId("reload_stars"));
        public static final PacketCodec<RegistryByteBuf, StarsRePayLoad> CODEC = PacketCodec.unit(INSTANCE);
        @Override
        public CustomPayload.Id<? extends CustomPayload> getId() {
            return ID;
        }
    }
    public static void receiveStars(StarsRePayLoad payload, ClientPlayNetworking.Context context) {
        ((WorldRendererAccess)(context.client().worldRenderer)).projectInfinity$setNeedsStars(true);
    }

    public static ShaderRePayload setShaderFromWorld(ServerWorld destination) {
        return new ShaderRePayload(((InfinityOptionsAccess)(destination)).projectInfinity$getInfinityOptions().data());
    }

    public static void registerPayloadsServer() {
        PayloadTypeRegistry.playS2C().register(WorldAddPayload.ID, WorldAddPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(ShaderRePayload.ID, ShaderRePayload.CODEC);
        PayloadTypeRegistry.playS2C().register(StarsRePayLoad.ID, StarsRePayLoad.CODEC);
    }
    public static void registerPayloadsClient() {
        ClientPlayNetworking.registerGlobalReceiver(ModPayloads.WorldAddPayload.ID, ModPayloads::addWorld);
        ClientPlayNetworking.registerGlobalReceiver(ModPayloads.ShaderRePayload.ID, ModPayloads::receiveShader);
        ClientPlayNetworking.registerGlobalReceiver(ModPayloads.StarsRePayLoad.ID, ModPayloads::receiveStars);
    }
}
