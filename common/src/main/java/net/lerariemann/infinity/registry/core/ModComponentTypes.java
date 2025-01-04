package net.lerariemann.infinity.registry.core;

import com.mojang.serialization.Codec;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.lerariemann.infinity.InfinityMod;
import net.minecraft.component.ComponentType;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.Codecs;

import java.util.function.UnaryOperator;

import static net.lerariemann.infinity.InfinityMod.MOD_ID;

public class ModComponentTypes {
    public static final DeferredRegister<ComponentType<?>> COMPONENT_TYPES =
            DeferredRegister.create(MOD_ID, RegistryKeys.DATA_COMPONENT_TYPE);

    public static RegistrySupplier<ComponentType<Identifier>> DESTINATION = registerComponentType("destination",
            (builder) -> builder.codec(Identifier.CODEC).packetCodec(Identifier.PACKET_CODEC));
    public static RegistrySupplier<ComponentType<Identifier>> BIOME_CONTENTS = registerComponentType("biome_contents",
            (builder) -> builder.codec(Identifier.CODEC).packetCodec(Identifier.PACKET_CODEC));
    public static RegistrySupplier<ComponentType<Integer>> COLOR = registerComponentType("color",
            (builder) -> builder.codec(Codec.INT).packetCodec(PacketCodecs.VAR_INT));
    public static RegistrySupplier<ComponentType<String>> DYE_COLOR = registerComponentType("dye_color",
            (builder) -> builder.codec(Codec.STRING).packetCodec(PacketCodecs.STRING));
    public static RegistrySupplier<ComponentType<Integer>> CHARGE = registerComponentType("charge",
            (builder) -> builder.codec(Codecs.NONNEGATIVE_INT).packetCodec(PacketCodecs.VAR_INT));
    public static RegistrySupplier<ComponentType<Integer>> SIZE_X = registerComponentType("size_x",
            (builder) -> builder.codec(Codecs.POSITIVE_INT).packetCodec(PacketCodecs.VAR_INT));
    public static RegistrySupplier<ComponentType<Integer>> SIZE_Y = registerComponentType("size_y",
            (builder) -> builder.codec(Codecs.POSITIVE_INT).packetCodec(PacketCodecs.VAR_INT));

    private static <T> RegistrySupplier<ComponentType<T>> registerComponentType(String id, UnaryOperator<ComponentType.Builder<T>> builderOperator) {
        return COMPONENT_TYPES.register(id, () -> (builderOperator.apply(ComponentType.builder())).build());
    }

    public static void registerComponentTypes() {
        InfinityMod.LOGGER.debug("Registering component types for " + MOD_ID);
        COMPONENT_TYPES.register();
    }
}
