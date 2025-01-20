package net.lerariemann.infinity.mixin.iridescence;

import com.llamalad7.mixinextras.sugar.Local;
import net.lerariemann.infinity.iridescence.Iridescence;
import net.lerariemann.infinity.registry.core.ModItems;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.GlassBottleItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GlassBottleItem.class)
public abstract class GlassBottleItemMixin {
    @Shadow protected abstract ItemStack fill(ItemStack stack, PlayerEntity player, ItemStack outputStack);

    @Inject(method="use", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/World;emitGameEvent(Lnet/minecraft/entity/Entity;Lnet/minecraft/registry/entry/RegistryEntry;Lnet/minecraft/util/math/BlockPos;)V",
            shift = At.Shift.AFTER), cancellable = true)
    void inj(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir, @Local ItemStack itemStack, @Local BlockPos pos) {
        if (Iridescence.isIridescence(world, pos))
            cir.setReturnValue(TypedActionResult.success(fill(itemStack, user,
                    ModItems.IRIDESCENT_POTION.get().getDefaultStack()), world.isClient()));
    }
}
