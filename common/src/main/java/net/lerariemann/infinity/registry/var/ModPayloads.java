package net.lerariemann.infinity.registry.var;

import dev.architectury.platform.Platform;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.lerariemann.infinity.access.InfinityOptionsAccess;
import net.lerariemann.infinity.access.WorldRendererAccess;
import net.lerariemann.infinity.iridescence.Iridescence;
import net.lerariemann.infinity.item.F4Item;
import net.lerariemann.infinity.registry.core.ModComponentTypes;
import net.lerariemann.infinity.registry.core.ModItems;
import net.lerariemann.infinity.util.loading.DimensionGrabber;
import net.lerariemann.infinity.options.InfinityOptions;
import net.lerariemann.infinity.util.InfinityMethods;
import net.lerariemann.infinity.util.loading.ShaderLoader;
import net.lerariemann.infinity.util.core.CommonIO;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.ComponentMap;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;

import java.nio.file.Path;
import java.util.Objects;

public class ModPayloads {

    public static MinecraftClient client(Object context) {
        ClientPlayNetworking.Context clientContext = (ClientPlayNetworking.Context) context;
        return clientContext.client();
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

    public record ShaderRePayload(NbtCompound shader_data, boolean iridescence) implements CustomPayload {
        public static final CustomPayload.Id<ShaderRePayload> ID = new CustomPayload.Id<>(InfinityMethods.getId("reload_shader"));
        public static final PacketCodec<RegistryByteBuf, ShaderRePayload> CODEC = PacketCodec.tuple(
                PacketCodecs.NBT_COMPOUND, ShaderRePayload::shader_data,
                PacketCodecs.BOOL, ShaderRePayload::iridescence,
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

    public static ShaderRePayload setShader(ServerPlayerEntity player) {
        return setShaderFromWorld(player.getServerWorld(), player);
    }
    public static ShaderRePayload setShaderFromWorld(ServerWorld destination, ServerPlayerEntity player) {
        return setShaderFromWorld(destination, Iridescence.shouldApplyShader(player));
    }
    public static ShaderRePayload setShaderFromWorld(ServerWorld destination, boolean bl) {
        if (destination == null) return new ShaderRePayload(new NbtCompound(), bl);
        return new ShaderRePayload(InfinityOptions.access(destination).data(), bl);
    }

    public record F4Payload(int slot, int width, int height) implements CustomPayload {
        public static final CustomPayload.Id<F4Payload> ID = new CustomPayload.Id<>(InfinityMethods.getId("receive_f4"));
        public static final PacketCodec<RegistryByteBuf, F4Payload> CODEC = PacketCodec.tuple(
                PacketCodecs.VAR_INT, F4Payload::slot,
                PacketCodecs.VAR_INT, F4Payload::width,
                PacketCodecs.VAR_INT, F4Payload::height,
                F4Payload::new);
        @Override
        public CustomPayload.Id<? extends CustomPayload> getId() {
            return ID;
        }
    }
    public static void receiveF4(F4Payload payload, ServerPlayNetworking.Context context) {
        ItemStack st = context.player().getInventory().getStack(payload.slot);
        if (st.isOf(ModItems.F4.get())) {
            ItemStack newStack = st.copy();
            newStack.applyComponentsFrom(ComponentMap.builder()
                    .add(ModComponentTypes.SIZE_X.get(), Math.clamp(payload.width, 1, 21))
                    .add(ModComponentTypes.SIZE_Y.get(), Math.clamp(payload.height, 1, 21))
                    .build());
            context.player().getInventory().setStack(payload.slot, newStack);
        }
    }
    public record DeployF4() implements CustomPayload {
        public static final DeployF4 INSTANCE = new DeployF4();
        public static final CustomPayload.Id<DeployF4> ID = new CustomPayload.Id<>(InfinityMethods.getId("deploy_f4"));
        public static final PacketCodec<RegistryByteBuf, DeployF4> CODEC = PacketCodec.unit(INSTANCE);
        @Override
        public CustomPayload.Id<? extends CustomPayload> getId() {
            return ID;
        }
    }
    public static void deployF4(DeployF4 payload, ServerPlayNetworking.Context context) {
        ServerPlayerEntity player = context.player();
        ItemStack st = player.getStackInHand(Hand.MAIN_HAND);
        if (st.isOf(ModItems.F4.get())) {
            TypedActionResult<ItemStack> result = F4Item.deploy(player.getServerWorld(), player, Hand.MAIN_HAND);
            player.setStackInHand(Hand.MAIN_HAND, result.getValue());
        }
    }

    public static void registerPayloadsServer() {
        PayloadTypeRegistry.playS2C().register(WorldAddPayload.ID, WorldAddPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(BiomeAddPayload.ID, BiomeAddPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(ShaderRePayload.ID, ShaderRePayload.CODEC);
        PayloadTypeRegistry.playS2C().register(StarsRePayLoad.ID, StarsRePayLoad.CODEC);
        PayloadTypeRegistry.playC2S().register(F4Payload.ID, F4Payload.CODEC);
        PayloadTypeRegistry.playC2S().register(DeployF4.ID, DeployF4.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(F4Payload.ID, ModPayloads::receiveF4);
        ServerPlayNetworking.registerGlobalReceiver(DeployF4.ID, ModPayloads::deployF4);
    }

    public static void registerPayloadsClient() {
        ClientPlayNetworking.registerGlobalReceiver(WorldAddPayload.ID, ModPayloads::addWorld);
        ClientPlayNetworking.registerGlobalReceiver(BiomeAddPayload.ID, ModPayloads::addBiome);
        ClientPlayNetworking.registerGlobalReceiver(ShaderRePayload.ID, ModPayloads::receiveShader);
        ClientPlayNetworking.registerGlobalReceiver(StarsRePayLoad.ID, ModPayloads::receiveStars);
    }
}