package net.lerariemann.infinity.item;

import com.mojang.serialization.Codec;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.lerariemann.infinity.InfinityMod;
import net.minecraft.component.ComponentType;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialRecipeSerializer;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

import java.util.function.UnaryOperator;

import static net.lerariemann.infinity.InfinityMod.MOD_ID;

public class ModItemFunctions {
    public static RegistrySupplier<ComponentType<Identifier>> KEY_DESTINATION;
    public static RegistrySupplier<ComponentType<Identifier>> BIOME_CONTENTS;
    public static RegistrySupplier<ComponentType<Integer>> COLOR;
    public static RegistrySupplier<ComponentType<Integer>> CHARGE;
    public static RegistrySupplier<ComponentType<Boolean>> DO_NOT_OPEN;
    public static final DeferredRegister<ComponentType<?>> COMPONENT_TYPES =
            DeferredRegister.create(MOD_ID, RegistryKeys.DATA_COMPONENT_TYPE);

    public static RegistrySupplier<LootFunctionType<SetLevelLootFunction>> SET_BIOME_BOTTLE_LEVEL;
    public static final DeferredRegister<LootFunctionType<?>> LOOT_FUNCTION_TYPES =
            DeferredRegister.create(MOD_ID, RegistryKeys.LOOT_FUNCTION_TYPE);

    public static RegistrySupplier<RecipeSerializer<BiomeBottleCombiningRecipe>> BIOME_BOTTLE_COMBINING;
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(MOD_ID, RegistryKeys.RECIPE_SERIALIZER);

    public static void registerComponentTypes() {
        InfinityMod.LOGGER.debug("Registering component types for " + InfinityMod.MOD_ID);
        KEY_DESTINATION = register("key_destination",
                (builder) -> builder.codec(Identifier.CODEC).packetCodec(Identifier.PACKET_CODEC));
        BIOME_CONTENTS = register("biome_contents",
                (builder) -> builder.codec(Identifier.CODEC).packetCodec(Identifier.PACKET_CODEC));
        COLOR = register("color",
                (builder) -> builder.codec(Codec.INT).packetCodec(PacketCodecs.VAR_INT));
        CHARGE = register("charge",
                (builder) -> builder.codec(Codec.INT).packetCodec(PacketCodecs.VAR_INT));
        DO_NOT_OPEN = register("do_not_open", (builder) -> builder.codec(Codec.BOOL).packetCodec(PacketCodecs.BOOL));
        COMPONENT_TYPES.register();

        SET_BIOME_BOTTLE_LEVEL = LOOT_FUNCTION_TYPES.register("set_biome_bottle_level", () ->
                new LootFunctionType<>(SetLevelLootFunction.CODEC));
        LOOT_FUNCTION_TYPES.register();

        BIOME_BOTTLE_COMBINING = RECIPE_SERIALIZERS.register("biome_bottle_combining", () ->
                new SpecialRecipeSerializer<>(BiomeBottleCombiningRecipe::new));
        RECIPE_SERIALIZERS.register();
    }

    private static <T> RegistrySupplier<ComponentType<T>> register(String id, UnaryOperator<ComponentType.Builder<T>> builderOperator) {
        return COMPONENT_TYPES.register(id, () -> (builderOperator.apply(ComponentType.builder())).build());
    }
}
