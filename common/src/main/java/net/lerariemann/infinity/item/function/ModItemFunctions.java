package net.lerariemann.infinity.item.function;

import dev.architectury.registry.item.ItemPropertiesRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.block.custom.BiomeBottle;
import net.lerariemann.infinity.item.ModItems;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SingleStackInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.SpecialRecipeSerializer;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Function;

import static net.lerariemann.infinity.InfinityMod.MOD_ID;
import static net.lerariemann.infinity.item.ModItems.TRANSFINITE_KEY;

public class ModItemFunctions {
    public static final DeferredRegister<LootFunctionType> LOOT_FUNCTION_TYPES =
            DeferredRegister.create(MOD_ID, RegistryKeys.LOOT_FUNCTION_TYPE);
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(MOD_ID, RegistryKeys.RECIPE_SERIALIZER);
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(MOD_ID, RegistryKeys.RECIPE_TYPE);



    public static final RegistrySupplier<LootFunctionType> SET_BIOME_BOTTLE_LEVEL = LOOT_FUNCTION_TYPES.register("set_biome_bottle_level", () ->
            new LootFunctionType(new SetLevelLootFunction.Serializer()));
    public static final RegistrySupplier<LootFunctionType> SET_ALTAR_STATE = LOOT_FUNCTION_TYPES.register("set_altar_state", () ->
            new LootFunctionType(new SetAltarStateLootFunction.Serializer()));

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
        InfinityMod.LOGGER.debug("Registering component types for " + InfinityMod.MOD_ID);
        LOOT_FUNCTION_TYPES.register();
        RECIPE_SERIALIZERS.register();
        RECIPE_TYPES.register();
    }

    public static void checkCollisionRecipes(ServerWorld w, ItemEntity itemEntity,
                                             RecipeType<CollisionCraftingRecipe> recipeType,
                                             Function<Item, NbtCompound> componentFunction) {
        if (itemEntity.isRemoved()) return;
        ItemStack itemStack = itemEntity.getStack();
        Optional<CollisionCraftingRecipe> match = w.getRecipeManager()
                .getFirstMatch(recipeType, new SingleStackInventory() {
                    @Override
                    public ItemStack getStack(int slot) {
                        return itemStack;
                    }

                    @Override
                    public ItemStack removeStack(int slot, int amount) {
                        return null;
                    }

                    @Override
                    public void setStack(int slot, ItemStack stack) {

                    }

                    @Override
                    public void markDirty() {

                    }

                    @Override
                    public boolean canPlayerUse(PlayerEntity player) {
                        return false;
                    }
                }, w);
        if (match.isEmpty()) return;

        ItemStack resStack = match.get().getOutput(w.getRegistryManager());
//        componentFunction.apply(resStack.getItem()).ifPresent(resStack::applyComponentsFrom);

        Vec3d v = itemEntity.getVelocity();
        ItemEntity result = new ItemEntity(w, itemEntity.getX(), itemEntity.getY(), itemEntity.getZ(),
                resStack.copyWithCount(itemEntity.getStack().getCount()),
                -v.x, -v.y, -v.z);
        w.spawnEntity(result);
        itemEntity.remove(Entity.RemovalReason.CHANGED_DIMENSION);
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
