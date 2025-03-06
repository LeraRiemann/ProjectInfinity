package net.lerariemann.infinity.registry.var;

import dev.architectury.injectables.annotations.ExpectPlatform;
import dev.architectury.platform.Platform;
import net.lerariemann.infinity.access.InfinityOptionsAccess;
import net.lerariemann.infinity.access.WorldRendererAccess;
import net.lerariemann.infinity.iridescence.Iridescence;
import net.lerariemann.infinity.item.F4Item;
import net.lerariemann.infinity.registry.core.ModComponentTypes;
import net.lerariemann.infinity.registry.core.ModItems;
import net.lerariemann.infinity.util.PlatformMethods;
import net.lerariemann.infinity.util.loading.DimensionGrabber;
import net.lerariemann.infinity.options.InfinityOptions;
import net.lerariemann.infinity.util.InfinityMethods;
import net.lerariemann.infinity.util.loading.ShaderLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.ComponentMap;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.dimension.DimensionType;

import java.nio.file.Path;

public class ModPayloads {

    public record WorldAddS2CPayload(Identifier world_id, NbtCompound world_data) implements CustomPayload {
        public static final CustomPayload.Id<WorldAddS2CPayload> ID = new CustomPayload.Id<>(InfinityMethods.getId("add_world"));
        public static final PacketCodec<RegistryByteBuf, WorldAddS2CPayload> CODEC = PacketCodec.tuple(
                Identifier.PACKET_CODEC, WorldAddS2CPayload::world_id,
                PacketCodecs.NBT_COMPOUND, WorldAddS2CPayload::world_data,
                WorldAddS2CPayload::new);
        @Override
        public CustomPayload.Id<? extends CustomPayload> getId() {
            return ID;
        }
    }
    public static void sendWorldAddPayload(ServerPlayerEntity player, Identifier id, NbtCompound data) {
        PlatformMethods.sendS2CPayload(player, new WorldAddS2CPayload(id, data));
    }
    public static void receiveWorldAddPayload(MinecraftClient client, Identifier id, NbtCompound data) {
        client.execute(() -> DimensionGrabber.grabObjectForClient(client, DimensionType.CODEC, RegistryKeys.DIMENSION_TYPE, id, data));
    }

    public record BiomeAddS2CPayload(Identifier biome_id, NbtCompound biome_data) implements CustomPayload {
        public static final CustomPayload.Id<BiomeAddS2CPayload> ID = new CustomPayload.Id<>(InfinityMethods.getId("add_biome"));
        public static final PacketCodec<RegistryByteBuf, BiomeAddS2CPayload> CODEC = PacketCodec.tuple(
                Identifier.PACKET_CODEC, BiomeAddS2CPayload::biome_id,
                PacketCodecs.NBT_COMPOUND, BiomeAddS2CPayload::biome_data,
                BiomeAddS2CPayload::new);
        @Override
        public CustomPayload.Id<? extends CustomPayload> getId() {
            return ID;
        }
    }
    public static void sendBiomeAddPayload(ServerPlayerEntity player, Identifier id, NbtCompound data) {
        PlatformMethods.sendS2CPayload(player, new BiomeAddS2CPayload(id, data));
    }
    public static void receiveBiomeAddPayload(MinecraftClient client, Identifier id, NbtCompound data) {
        client.execute(() -> DimensionGrabber.grabObjectForClient(client, Biome.CODEC, RegistryKeys.BIOME, id, data));
    }

    public record ShaderS2CPayload(NbtCompound shader_data, boolean iridescence) implements CustomPayload {
        public static final CustomPayload.Id<ShaderS2CPayload> ID = new CustomPayload.Id<>(InfinityMethods.getId("reload_shader"));
        public static final PacketCodec<RegistryByteBuf, ShaderS2CPayload> CODEC = PacketCodec.tuple(
                PacketCodecs.NBT_COMPOUND, ShaderS2CPayload::shader_data,
                PacketCodecs.BOOL, ShaderS2CPayload::iridescence,
                ShaderS2CPayload::new);
        @Override
        public CustomPayload.Id<? extends CustomPayload> getId() {
            return ID;
        }
    }
    public static void sendShaderPayload(ServerPlayerEntity player) {
        sendShaderPayload(player, player.getServerWorld(), Iridescence.shouldApplyShader(player));
    }
    public static void sendShaderPayload(ServerPlayerEntity player, ServerWorld world) {
        sendShaderPayload(player, world, Iridescence.shouldApplyShader(player));
    }
    public static void sendShaderPayload(ServerPlayerEntity player, ServerWorld world, boolean iridescence) {
        PlatformMethods.sendS2CPayload(player, new ShaderS2CPayload(
                world == null ? new NbtCompound() : InfinityOptions.access(world).data(),
                iridescence));
    }
    public static void receiveShaderPayload(MinecraftClient client, NbtCompound data, boolean iridescence) {
        InfinityOptions options = new InfinityOptions(data);
        ((InfinityOptionsAccess)client).infinity$setOptions(options);
        client.execute(() -> ShaderLoader.reloadShaders(client, options.getShader(), iridescence));
    }
    public static boolean resourcesReloaded = Path.of(Platform.getGameFolder() + "/resourcepacks/infinity/assets/infinity/shaders").toFile().exists();

    public record StarsS2CPayload() implements CustomPayload {
        public static final StarsS2CPayload INSTANCE = new StarsS2CPayload();
        public static final CustomPayload.Id<StarsS2CPayload> ID = new CustomPayload.Id<>(InfinityMethods.getId("reload_stars"));
        public static final PacketCodec<RegistryByteBuf, StarsS2CPayload> CODEC = PacketCodec.unit(INSTANCE);
        @Override
        public CustomPayload.Id<? extends CustomPayload> getId() {
            return ID;
        }
    }
    public static void sendStarsPayload(ServerPlayerEntity player) {
        PlatformMethods.sendS2CPayload(player, StarsS2CPayload.INSTANCE);
    }
    public static void receiveStarsPayload(MinecraftClient client) {
        ((WorldRendererAccess)(client.worldRenderer)).infinity$setNeedsStars(true);
    }

    public record F4UpdateC2SPayload(int slot, int width, int height) implements CustomPayload {
        public static final CustomPayload.Id<F4UpdateC2SPayload> ID = new CustomPayload.Id<>(InfinityMethods.getId("receive_f4"));
        public static final PacketCodec<RegistryByteBuf, F4UpdateC2SPayload> CODEC = PacketCodec.tuple(
                PacketCodecs.VAR_INT, F4UpdateC2SPayload::slot,
                PacketCodecs.VAR_INT, F4UpdateC2SPayload::width,
                PacketCodecs.VAR_INT, F4UpdateC2SPayload::height,
                F4UpdateC2SPayload::new);
        @Override
        public CustomPayload.Id<? extends CustomPayload> getId() {
            return ID;
        }
    }
    public static void sendF4UpdatePayload(int slot, int width, int height) {
        PlatformMethods.sendC2SPayload(new F4UpdateC2SPayload(slot, width, height));
    }
    public static void receiveF4UpdatePayload(ServerPlayerEntity player, int slot, int width, int height) {
        ItemStack st = player.getInventory().getStack(slot);
        if (st.isOf(ModItems.F4.get())) {
            ItemStack newStack = st.copy();
            newStack.applyComponentsFrom(ComponentMap.builder()
                    .add(ModComponentTypes.SIZE_X.get(), Math.clamp(width, 1, 21))
                    .add(ModComponentTypes.SIZE_Y.get(), Math.clamp(height, 1, 21))
                    .build());
            player.getInventory().setStack(slot, newStack);
        }
    }

    public record F4DeployC2SPayload() implements CustomPayload {
        public static final F4DeployC2SPayload INSTANCE = new F4DeployC2SPayload();
        public static final CustomPayload.Id<F4DeployC2SPayload> ID = new CustomPayload.Id<>(InfinityMethods.getId("deploy_f4"));
        public static final PacketCodec<RegistryByteBuf, F4DeployC2SPayload> CODEC = PacketCodec.unit(INSTANCE);
        @Override
        public CustomPayload.Id<? extends CustomPayload> getId() {
            return ID;
        }
    }
    public static void sendF4DeployPayload() {
        PlatformMethods.sendC2SPayload(F4DeployC2SPayload.INSTANCE);
    }
    public static void receiveF4DeployPayload(ServerPlayerEntity player) {
        ItemStack st = player.getStackInHand(Hand.MAIN_HAND);
        if (st.isOf(ModItems.F4.get())) {
            TypedActionResult<ItemStack> result = F4Item.deploy(player.getServerWorld(), player, Hand.MAIN_HAND);
            player.setStackInHand(Hand.MAIN_HAND, result.getValue());
        }
    }

    public record SoundPackS2CPayload(NbtCompound songIds) implements CustomPayload {
        public static final CustomPayload.Id<SoundPackS2CPayload> ID = new CustomPayload.Id<>(InfinityMethods.getId("download_sound_pack"));
        public static final PacketCodec<RegistryByteBuf, SoundPackS2CPayload> CODEC = PacketCodec.tuple(
                PacketCodecs.NBT_COMPOUND, SoundPackS2CPayload::songIds, SoundPackS2CPayload::new);

        @Override
        public CustomPayload.Id<? extends CustomPayload> getId() {
            return ID;
        }
    }
    public static void sendSoundPackPayload(ServerPlayerEntity player, NbtCompound data) {
        PlatformMethods.sendS2CPayload(player, new SoundPackS2CPayload(data));
    }

    public record JukeboxesC2SPayload(NbtCompound data) implements CustomPayload {
        public static final CustomPayload.Id<JukeboxesC2SPayload> ID = new CustomPayload.Id<>(InfinityMethods.getId("upload_jukeboxes"));
        public static final PacketCodec<RegistryByteBuf, JukeboxesC2SPayload> CODEC = PacketCodec.tuple(
                PacketCodecs.NBT_COMPOUND, JukeboxesC2SPayload::data, JukeboxesC2SPayload::new);
        @Override
        public CustomPayload.Id<? extends CustomPayload> getId() {
            return ID;
        }
    }
    public static void sendJukeboxesPayload(NbtCompound data) {
        PlatformMethods.sendC2SPayload(new JukeboxesC2SPayload(data));
    }

    @ExpectPlatform
    public static void registerPayloadsServer() {
        throw new AssertionError();
    }
    @ExpectPlatform
    public static void registerPayloadsClient() {
        throw new AssertionError();
    }
}