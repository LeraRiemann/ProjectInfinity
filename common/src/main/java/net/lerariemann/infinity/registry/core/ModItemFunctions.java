package net.lerariemann.infinity.registry.core;

import dev.architectury.registry.item.ItemPropertiesRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.lerariemann.infinity.block.custom.BiomeBottleBlock;
import net.lerariemann.infinity.item.function.*;
import net.lerariemann.infinity.options.InfinityOptions;
import net.lerariemann.infinity.util.BackportMethods;
import net.lerariemann.infinity.util.InfinityMethods;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.ItemDispenserBehavior;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.component.ComponentChanges;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SingleStackInventory;
import net.minecraft.item.FluidModificationItem;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.SpecialRecipeSerializer;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Function;

import static net.lerariemann.infinity.InfinityMod.MOD_ID;
import static net.lerariemann.infinity.registry.core.ModItems.TRANSFINITE_KEY;

public class ModItemFunctions {

    public static String SIZE_X = "f4_size_x";
    public static String SIZE_Y = "f4_size_y";
    public static String F4_CHARGE = "f4_charge";
    public static String DESTINATION = "key_destination";
    public static String COLOR = "key_color";

    public static final DeferredRegister<LootFunctionType> LOOT_FUNCTION_TYPES =
            DeferredRegister.create(MOD_ID, RegistryKeys.LOOT_FUNCTION_TYPE);
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(MOD_ID, RegistryKeys.RECIPE_SERIALIZER);
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(MOD_ID, RegistryKeys.RECIPE_TYPE);

    public static RegistrySupplier<RecipeSerializer<BiomeBottleCombiningRecipe>> BIOME_BOTTLE_COMBINING =
            RECIPE_SERIALIZERS.register("biome_bottle_combining", () ->
                    new SpecialRecipeSerializer<>(BiomeBottleCombiningRecipe::new));
    public static RegistrySupplier<RecipeSerializer<F4RechargingRecipe>> F4_RECHARGING =
            RECIPE_SERIALIZERS.register("f4_recharging", () ->
                    new SpecialRecipeSerializer<>(F4RechargingRecipe::new));

    public static RegistrySupplier<RecipeSerializer<ChromaticColoringRecipe>> CHROMATIC_COLORING =
            RECIPE_SERIALIZERS.register("chromatic_coloring", () ->
                    new ChromaticColoringRecipe.Serializer(ChromaticColoringRecipe::new));
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
        LOOT_FUNCTION_TYPES.register();
        RECIPE_SERIALIZERS.register();
        RECIPE_TYPES.register();
    }

    public static void registerDispenserBehaviour() {
        DispenserBlock.registerBehavior(ModItems.IRIDESCENCE_BUCKET.get(), new ItemDispenserBehavior() {
            public ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
                FluidModificationItem fluidModificationItem = (FluidModificationItem)stack.getItem();
                BlockPos blockPos = pointer.getPos().offset(pointer.getBlockState().get(DispenserBlock.FACING));
                World world = pointer.getWorld();
                if (fluidModificationItem.placeFluid(null, world, blockPos, null)) {
                    fluidModificationItem.onEmptied(null, world, stack, blockPos);
                    return stack.getRecipeRemainder();
                } else {
                    return new ItemDispenserBehavior().dispense(pointer, stack);
                }
            }
        });
    }

    public static void checkCollisionRecipes(ServerWorld w, ItemEntity itemEntity,
                                             RecipeType<CollisionCraftingRecipe> recipeType,
                                             NbtCompound compound) {
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
                    public void setStack(int slot, ItemStack stack) {}

                    @Override
                    public void markDirty() {}

                    @Override
                    public boolean canPlayerUse(PlayerEntity player) {
                        return false;
                    }
                }, w);
        if (match.isEmpty()) return;

        ItemStack resStack = match.get().value().getResult(w.getRegistryManager());
        componentFunction.apply(resStack.getItem()).ifPresent(resStack::applyChanges);

        Vec3d v = itemEntity.getVelocity();
        ItemEntity result = new ItemEntity(w, itemEntity.getX(), itemEntity.getY(), itemEntity.getZ(),
                resStack.copyWithCount(itemEntity.getStack().getCount()),
                -v.x, -v.y, -v.z);
        w.spawnEntity(result);
        itemEntity.remove(Entity.RemovalReason.CHANGED_DIMENSION);
    }

    @Environment(EnvType.CLIENT)
    public static float iridPredicate(@Nullable ItemStack stack, ClientWorld world, @Nullable LivingEntity entity, int seed) {
        if (entity == null) return 0;
        return (InfinityOptions.access(world).iridMap.getColor(entity.getBlockPos()) / 100.0f);
    }

    @Environment(EnvType.CLIENT)
    public static void registerModelPredicates() {
        ItemPropertiesRegistry.register(TRANSFINITE_KEY.get(), InfinityMethods.getId("key"), (stack, world, entity, seed) -> {
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
        ItemPropertiesRegistry.register(ModItems.BIOME_BOTTLE_ITEM.get(), InfinityMethods.getId("bottle"),
                (stack, world, entity, seed) -> {
                    int charge = BiomeBottleBlock.getCharge(stack);
                    return MathHelper.clamp(charge / 1000.0f, 0f, 1f);
                });
        ItemPropertiesRegistry.register(ModItems.IRIDESCENT_CARPET.get(), InfinityMethods.getId("iridescent"),
                ModItemFunctions::iridPredicate);
        ItemPropertiesRegistry.register(ModItems.IRIDESCENT_WOOL.get(), InfinityMethods.getId("iridescent"),
                ModItemFunctions::iridPredicate);
        ItemPropertiesRegistry.register(ModItems.F4.get(), InfinityMethods.getId("f4"),
                (stack, world, entity, seed) -> {
                    Identifier id = BackportMethods.getDimensionIdentifier(stack);
                    if (id == null) return 0.0f;
                    if (id.toString().equals(InfinityMethods.ofRandomDim)) return 0.02f;
                    return 0.01f;
                });
    }
}