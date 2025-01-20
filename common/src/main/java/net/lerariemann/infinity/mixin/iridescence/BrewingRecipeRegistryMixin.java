package net.lerariemann.infinity.mixin.iridescence;

import net.lerariemann.infinity.registry.core.ModItems;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.recipe.BrewingRecipeRegistry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(BrewingRecipeRegistry.class)
public class BrewingRecipeRegistryMixin {
    @Inject(method = "isValidIngredient", at = @At("RETURN"), cancellable = true)
    void inj(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(cir.getReturnValue() || stack.isOf(ModItems.CHROMATIC_MATTER.get()));
    }

    @Inject(method="hasPotionRecipe", at = @At("RETURN"), cancellable = true)
    void inj(ItemStack input, ItemStack ingredient, CallbackInfoReturnable<Boolean> cir) {
        if (ingredient.isOf(ModItems.CHROMATIC_MATTER.get())) {
            Optional<RegistryEntry<Potion>> optional = input.getOrDefault(DataComponentTypes.POTION_CONTENTS, PotionContentsComponent.DEFAULT).potion();
            if (optional.isPresent() && optional.get().matchesId(Identifier.ofVanilla("awkward"))) cir.setReturnValue(true);
        }
    }

    @Inject(method = "craft", at = @At("HEAD"), cancellable = true)
    void inj2(ItemStack ingredient, ItemStack input, CallbackInfoReturnable<ItemStack> cir) {
        if (!input.isEmpty() && ingredient.isOf(ModItems.CHROMATIC_MATTER.get())) {
            Optional<RegistryEntry<Potion>> optional = input.getOrDefault(DataComponentTypes.POTION_CONTENTS, PotionContentsComponent.DEFAULT).potion();
            if (optional.isPresent() && optional.get().matchesId(Identifier.ofVanilla("awkward")))
                cir.setReturnValue(ModItems.CHROMATIC_BOTTLE.get().getDefaultStack());
        }
    }
}
