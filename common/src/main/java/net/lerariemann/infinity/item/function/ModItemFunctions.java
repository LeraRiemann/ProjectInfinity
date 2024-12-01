package net.lerariemann.infinity.item.function;

import com.mojang.serialization.Codec;
import dev.architectury.registry.item.ItemPropertiesRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.block.custom.BiomeBottle;
import net.lerariemann.infinity.item.ModItems;
import net.lerariemann.infinity.options.InfinityOptions;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.ComponentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.SpecialRecipeSerializer;
import net.minecraft.recipe.input.SingleStackRecipeInput;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import static net.lerariemann.infinity.InfinityMod.MOD_ID;

public class ModItemFunctions {
    public static final DeferredRegister<ComponentType<?>> COMPONENT_TYPES =
            DeferredRegister.create(MOD_ID, RegistryKeys.DATA_COMPONENT_TYPE);
    public static final DeferredRegister<LootFunctionType<?>> LOOT_FUNCTION_TYPES =
            DeferredRegister.create(MOD_ID, RegistryKeys.LOOT_FUNCTION_TYPE);
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(MOD_ID, RegistryKeys.RECIPE_SERIALIZER);
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(MOD_ID, RegistryKeys.RECIPE_TYPE);


    public static RegistrySupplier<ComponentType<Identifier>> KEY_DESTINATION = registerComponentType("key_destination",
            (builder) -> builder.codec(Identifier.CODEC).packetCodec(Identifier.PACKET_CODEC));
    public static RegistrySupplier<ComponentType<Identifier>> BIOME_CONTENTS = registerComponentType("biome_contents",
            (builder) -> builder.codec(Identifier.CODEC).packetCodec(Identifier.PACKET_CODEC));
    public static RegistrySupplier<ComponentType<Integer>> COLOR = registerComponentType("color",
            (builder) -> builder.codec(Codec.INT).packetCodec(PacketCodecs.VAR_INT));
    public static RegistrySupplier<ComponentType<Integer>> CHARGE = registerComponentType("charge",
            (builder) -> builder.codec(Codec.INT).packetCodec(PacketCodecs.VAR_INT));
    public static RegistrySupplier<ComponentType<Boolean>> DO_NOT_OPEN = registerComponentType("do_not_open",
            (builder) -> builder.codec(Codec.BOOL).packetCodec(PacketCodecs.BOOL));

    public static RegistrySupplier<LootFunctionType<SetLevelLootFunction>> SET_BIOME_BOTTLE_LEVEL =
            LOOT_FUNCTION_TYPES.register("set_biome_bottle_level", () -> new LootFunctionType<>(SetLevelLootFunction.CODEC));
    public static RegistrySupplier<LootFunctionType<SetAltarStateLootFunction>> SET_ALTAR_STATE =
            LOOT_FUNCTION_TYPES.register("set_altar_state", () -> new LootFunctionType<>(SetAltarStateLootFunction.CODEC));

    public static RegistrySupplier<RecipeSerializer<BiomeBottleCombiningRecipe>> BIOME_BOTTLE_COMBINING =
            RECIPE_SERIALIZERS.register("biome_bottle_combining", () ->
                    new SpecialRecipeSerializer<>(BiomeBottleCombiningRecipe::new));
    public static RegistrySupplier<RecipeSerializer<CollisionCraftingRecipe>> PORTAL_CRAFTING =
            RECIPE_SERIALIZERS.register("collision_portal", () ->
                    new CollisionCraftingRecipe.Serializer(CollisionCraftingRecipe.OfPortal::new));
    public static RegistrySupplier<RecipeSerializer<CollisionCraftingRecipe>> IRIDESCENCE_CRAFTING =
            RECIPE_SERIALIZERS.register("collision_iridescence", () ->
                    new CollisionCraftingRecipe.Serializer(CollisionCraftingRecipe.OfIridescence::new));

    public static RegistrySupplier<RecipeType<CollisionCraftingRecipe>> PORTAL_CRAFTING_TYPE =
            RECIPE_TYPES.register("collision_portal", () -> CollisionCraftingRecipe.Type.PORTAL);
    public static RegistrySupplier<RecipeType<CollisionCraftingRecipe>> IRIDESCENCE_CRAFTING_TYPE =
            RECIPE_TYPES.register("collision_iridescence", () -> CollisionCraftingRecipe.Type.IRIDESCENCE);

    public static void registerItemFunctions() {
        InfinityMod.LOGGER.debug("Registering component types for " + MOD_ID);
        COMPONENT_TYPES.register();
        LOOT_FUNCTION_TYPES.register();
        RECIPE_SERIALIZERS.register();
        RECIPE_TYPES.register();
    }

    private static <T> RegistrySupplier<ComponentType<T>> registerComponentType(String id, UnaryOperator<ComponentType.Builder<T>> builderOperator) {
        return COMPONENT_TYPES.register(id, () -> (builderOperator.apply(ComponentType.builder())).build());
    }

    public static void checkCollisionRecipes(ServerWorld w, ItemEntity itemEntity,
                                             RecipeType<CollisionCraftingRecipe> recipeType,
                                             Function<Item, Optional<ComponentMap>> componentFunction) {
        if (itemEntity.isRemoved()) return;
        ItemStack itemStack = itemEntity.getStack();
        Optional<RecipeEntry<CollisionCraftingRecipe>> match = w.getRecipeManager()
                .getFirstMatch(recipeType, new SingleStackRecipeInput(itemStack), w);
        if (match.isEmpty()) return;

        ItemStack resStack = match.get().value().getResult(w.getRegistryManager());
        componentFunction.apply(resStack.getItem()).ifPresent(resStack::applyComponentsFrom);

        Vec3d v = itemEntity.getVelocity();
        ItemEntity result = new ItemEntity(w, itemEntity.getX(), itemEntity.getY(), itemEntity.getZ(),
                resStack.copyWithCount(itemEntity.getStack().getCount()),
                -v.x, -v.y, -v.z);
        w.spawnEntity(result);
        itemEntity.remove(Entity.RemovalReason.CHANGED_DIMENSION);
    }

    public static float iridPredicate(@Nullable ItemStack stack, ClientWorld world, @Nullable LivingEntity entity, int seed) {
        if (entity == null) return 0;
        return (InfinityOptions.access(world).iridMap.getColor(entity.getBlockPos()) / 100.0f);
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
        ItemPropertiesRegistry.register(ModItems.IRIDESCENT_CARPET.get(), InfinityMod.getId("iridescent"),
                ModItemFunctions::iridPredicate);
        ItemPropertiesRegistry.register(ModItems.IRIDESCENT_WOOL.get(), InfinityMod.getId("iridescent"),
                ModItemFunctions::iridPredicate);
    }
}