package net.lerariemann.infinity.mixin.iridescence;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.lerariemann.infinity.iridescence.Iridescence;
import net.lerariemann.infinity.registry.var.ModTags;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @Shadow public abstract boolean isIn(TagKey<Item> tag);

    @Inject(method="getTooltip", at = @At(value = "INVOKE",
            target = "Ljava/util/List;add(Ljava/lang/Object;)Z", ordinal = 0))
    void inj(Item.TooltipContext context, PlayerEntity player, TooltipType type, CallbackInfoReturnable<List<Text>> cir) {
        if (isIn(ModTags.IRIDESCENT_ITEMS) && cir.getReturnValue() != null) {
            var text = cir.getReturnValue().getFirst();
            Style style = text.getStyle().withColor(Iridescence.getTimeBasedColor());
            cir.getReturnValue().set(0, text.getWithStyle(style).getFirst());
        }
    }
}
