package net.lerariemann.infinity.var;

import com.mojang.serialization.Codec;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.lerariemann.infinity.InfinityMod;
import net.minecraft.component.ComponentType;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

import java.util.function.UnaryOperator;

import static net.lerariemann.infinity.InfinityMod.MOD_ID;

public class ModComponentTypes {
    public static RegistrySupplier<ComponentType<Identifier>> KEY_DESTINATION;
    public static RegistrySupplier<ComponentType<Integer>> KEY_COLOR;
    public static RegistrySupplier<ComponentType<Boolean>> DO_NOT_OPEN;
    public static final DeferredRegister<ComponentType<?>> COMPONENT_TYPES =
            DeferredRegister.create(MOD_ID, RegistryKeys.DATA_COMPONENT_TYPE);

    public static void registerComponentTypes() {
        InfinityMod.LOGGER.debug("Registering component types for " + InfinityMod.MOD_ID);
        KEY_DESTINATION = register("key_destination",
                (builder) -> builder.codec(Identifier.CODEC).packetCodec(Identifier.PACKET_CODEC));
        KEY_COLOR = register("key_color",
                (builder) -> builder.codec(Codec.INT).packetCodec(PacketCodecs.VAR_INT));
        DO_NOT_OPEN = register("do_not_open", (builder) -> builder.codec(Codec.BOOL).packetCodec(PacketCodecs.BOOL));
        COMPONENT_TYPES.register();
    }

    private static <T> RegistrySupplier<ComponentType<T>> register(String id, UnaryOperator<ComponentType.Builder<T>> builderOperator) {
        return COMPONENT_TYPES.register(id, () -> (builderOperator.apply(ComponentType.builder())).build());
    }
}
