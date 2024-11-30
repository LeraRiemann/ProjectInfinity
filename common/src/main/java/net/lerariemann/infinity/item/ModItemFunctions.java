package net.lerariemann.infinity.item;

import com.mojang.serialization.Codec;
import dev.architectury.registry.item.ItemPropertiesRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.block.custom.BiomeBottle;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.ComponentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialRecipeSerializer;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
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
    public static RegistrySupplier<LootFunctionType<SetAltarStateLootFunction>> SET_ALTAR_STATE;
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
        SET_ALTAR_STATE = LOOT_FUNCTION_TYPES.register("set_altar_state", () ->
                new LootFunctionType<>(SetAltarStateLootFunction.CODEC));
        LOOT_FUNCTION_TYPES.register();

        BIOME_BOTTLE_COMBINING = RECIPE_SERIALIZERS.register("biome_bottle_combining", () ->
                new SpecialRecipeSerializer<>(BiomeBottleCombiningRecipe::new));
        RECIPE_SERIALIZERS.register();
    }

    private static <T> RegistrySupplier<ComponentType<T>> register(String id, UnaryOperator<ComponentType.Builder<T>> builderOperator) {
        return COMPONENT_TYPES.register(id, () -> (builderOperator.apply(ComponentType.builder())).build());
    }

    public static void checkCollisionRecipes(ServerWorld w, ItemEntity itemEntity,
                                             Map<Item, String> recipes,
                                             Function<Item, Optional<ComponentMap>> componentFunction) {
        if (itemEntity.isRemoved()) return;
        Item item = itemEntity.getStack().getItem();
        if (!recipes.containsKey(item)) return;
        Item newItem = Registries.ITEM.get(Identifier.of(recipes.get(item)));
        ItemStack resStack = new ItemStack(newItem);
        componentFunction.apply(newItem).ifPresent(resStack::applyComponentsFrom);

        Vec3d v = itemEntity.getVelocity();
        ItemEntity result = new ItemEntity(w, itemEntity.getX(), itemEntity.getY(), itemEntity.getZ(),
                resStack.copyWithCount(itemEntity.getStack().getCount()),
                -v.x, -v.y, -v.z);
        w.spawnEntity(result);
        itemEntity.remove(Entity.RemovalReason.CHANGED_DIMENSION);
    }

    @Environment(EnvType.CLIENT)
    public static void registerModelPredicates() {
        ItemPropertiesRegistry.register(ModItems.TRANSFINITE_KEY.get(), InfinityMod.getId("key"), (stack, world, entity, seed) -> {
            Identifier id = stack.getComponents().get(ModItemFunctions.KEY_DESTINATION.get());
            if (id == null) return 0.02f;
            String s = id.toString();
            if (s.contains("infinity:generated_")) return 0.01f;
            return switch(s) {
                case "minecraft:random" -> 0.02f;
                case "minecraft:the_end" -> 0.03f;
                case "infinity:pride" -> 0.04f;
                default -> 0;
            };
        });
        ItemPropertiesRegistry.register(ModItems.BIOME_BOTTLE_ITEM.get(), InfinityMod.getId("bottle"),
                (stack, world, entity, seed) -> {
                    int charge = BiomeBottle.getCharge(stack);
                    return Math.clamp(charge / 1000.0f, 0f, 1f);
                });
    }
}
