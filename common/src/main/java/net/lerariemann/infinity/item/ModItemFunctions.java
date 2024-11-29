package net.lerariemann.infinity.item;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import dev.architectury.registry.item.ItemPropertiesRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.block.custom.BiomeBottle;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialRecipeSerializer;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.JsonSerializer;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

import static net.lerariemann.infinity.InfinityMod.MOD_ID;
import static net.lerariemann.infinity.item.ModItems.TRANSFINITE_KEY;

public class ModItemFunctions {

    public static RegistrySupplier<LootFunctionType> SET_BIOME_BOTTLE_LEVEL;
    public static RegistrySupplier<LootFunctionType> SET_ALTAR_STATE;
    public static final DeferredRegister<LootFunctionType> LOOT_FUNCTION_TYPES =
            DeferredRegister.create(MOD_ID, RegistryKeys.LOOT_FUNCTION_TYPE);

    public static RegistrySupplier<RecipeSerializer<BiomeBottleCombiningRecipe>> BIOME_BOTTLE_COMBINING;
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(MOD_ID, RegistryKeys.RECIPE_SERIALIZER);

    public static void registerComponentTypes() {
        InfinityMod.LOGGER.debug("Registering component types for " + MOD_ID);
//        KEY_DESTINATION = register("key_destination",
//                (builder) -> builder.codec(Identifier.CODEC).packetCodec(Identifier.PACKET_CODEC));
//        BIOME_CONTENTS = register("biome_contents",
//                (builder) -> builder.codec(Identifier.CODEC).packetCodec(Identifier.PACKET_CODEC));
//        COLOR = register("color",
//                (builder) -> builder.codec(Codec.INT).packetCodec(PacketCodecs.VAR_INT));
//        CHARGE = register("charge",
//                (builder) -> builder.codec(Codec.INT).packetCodec(PacketCodecs.VAR_INT));
//        DO_NOT_OPEN = register("do_not_open", (builder) -> builder.codec(Codec.BOOL).packetCodec(PacketCodecs.BOOL));

        SET_BIOME_BOTTLE_LEVEL = LOOT_FUNCTION_TYPES.register("set_biome_bottle_level", () ->
                new LootFunctionType(new JsonSerializer<SetLevelLootFunction>() {
                    @Override
                    public void toJson(JsonObject json, SetLevelLootFunction object, JsonSerializationContext context) {

                    }

                    @Override
                    public SetLevelLootFunction fromJson(JsonObject json, JsonDeserializationContext context) {
                        return null;
                    }
                }));
        SET_ALTAR_STATE = LOOT_FUNCTION_TYPES.register("set_altar_state", () ->
                new LootFunctionType(new JsonSerializer<SetAltarStateLootFunction>() {
                    @Override
                    public void toJson(JsonObject json, SetAltarStateLootFunction object, JsonSerializationContext context) {

                    }

                    @Override
                    public SetAltarStateLootFunction fromJson(JsonObject json, JsonDeserializationContext context) {
                        return null;
                    }
                }));
        LOOT_FUNCTION_TYPES.register();

        BIOME_BOTTLE_COMBINING = RECIPE_SERIALIZERS.register("biome_bottle_combining", () ->
                new SpecialRecipeSerializer<>(BiomeBottleCombiningRecipe::new));
        RECIPE_SERIALIZERS.register();
    }



    public static @Nullable String getBiomeComponents(ItemStack stack) {
        if (stack.getNbt() != null) {
            return stack.getNbt().getString("bottle_biome");
        }
        return null;
    }

    public static @Nullable String getDimensionComponents(ItemStack stack) {
        if (stack.getNbt() != null) {
            return stack.getNbt().getString("key_destination");
        }
        return null;
    }

    @Environment(EnvType.CLIENT)
    public static void registerModelPredicates() {
        ItemPropertiesRegistry.register(TRANSFINITE_KEY.get(), InfinityMod.getId("key"), (stack, world, entity, seed) -> {
            String id;
            if (stack.getNbt() != null) {
                id = stack.getNbt().getString("key_destination");
            }
            else id = "minecraft:random";
            if (id == null) return 0;
            if (id.contains("infinity:generated_")) return 0.01f;
            return switch(id) {
                case "minecraft:random" -> 0.02f;
                case "minecraft:the_end" -> 0.03f;
                case "infinity:pride" -> 0.04f;
                default -> 0;
            };
        });
        ItemPropertiesRegistry.register(ModItems.BIOME_BOTTLE_ITEM.get(), InfinityMod.getId("bottle"),
                (stack, world, entity, seed) -> {
                    int charge = BiomeBottle.getCharge(stack);
                    return MathHelper.clamp(charge / 1000.0f, 0f, 1f);
                });
    }
}
