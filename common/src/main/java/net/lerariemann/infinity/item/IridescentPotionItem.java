package net.lerariemann.infinity.item;

import net.lerariemann.infinity.iridescence.Iridescence;
import net.lerariemann.infinity.registry.core.ModComponentTypes;
import net.lerariemann.infinity.registry.core.ModStatusEffects;
import net.lerariemann.infinity.util.BackportMethods;
import net.lerariemann.infinity.util.var.ColorLogic;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.item.Items;
import net.minecraft.potion.PotionUtil;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

import java.util.List;

public class IridescentPotionItem extends Item {
    public IridescentPotionItem(Settings settings) {
        super(settings);
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return true;
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        int level = BackportMethods.getOrDefaultInt(stack, ModComponentTypes.F4_CHARGE, 0);
        if (!world.isClient) Iridescence.tryBeginJourney(user, level, true);
        if (user instanceof PlayerEntity player) {
            if (user instanceof ServerPlayerEntity spe) {
                Criteria.CONSUME_ITEM.trigger(spe, stack);
            }
            player.incrementStat(Stats.USED.getOrCreateStat(this));

            if (!player.isCreative()) {
                stack.decrement(1);
                player.getInventory().insertStack(new ItemStack(Items.GLASS_BOTTLE));
            }

        }
        else if (stack.isEmpty()) {
            return new ItemStack(Items.GLASS_BOTTLE);
        }
        user.emitGameEvent(GameEvent.DRINK);
        return stack;
    }

    @Override
    public int getMaxUseTime(ItemStack stack) {
        return 32;
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.DRINK;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        return ItemUsage.consumeHeldItem(world, user, hand);
    }

    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext type) {
        int level = BackportMethods.getOrDefaultInt(stack, ModComponentTypes.F4_CHARGE, 0);
        List<StatusEffectInstance> effects = List.of(new StatusEffectInstance(ModStatusEffects.IRIDESCENT_EFFECT.value(),
                Iridescence.getFullEffectLength(level), level));
        PotionUtil.buildTooltip(effects, tooltip, level);
    }

    @Override
    public Text getName() {
        return Text.translatable(this.getTranslationKey()).setStyle(Style.EMPTY.withColor(ColorLogic.defaultChromatic));
    }
}
