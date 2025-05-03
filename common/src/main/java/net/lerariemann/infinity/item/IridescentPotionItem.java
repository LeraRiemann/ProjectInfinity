package net.lerariemann.infinity.item;

import net.lerariemann.infinity.iridescence.Iridescence;
import net.lerariemann.infinity.registry.core.ModComponentTypes;
import net.lerariemann.infinity.registry.core.ModStatusEffects;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.item.Items;
import net.minecraft.item.consume.UseAction;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

import java.util.List;

public class IridescentPotionItem extends Item {
    public IridescentPotionItem(Settings settings) {
        super(settings);
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        int level = stack.getOrDefault(ModComponentTypes.CHARGE.get(), 0);
        if (!world.isClient) Iridescence.tryBeginJourney(user, level, true);
        if (user instanceof PlayerEntity player) {
            if (user instanceof ServerPlayerEntity spe) {
                Criteria.CONSUME_ITEM.trigger(spe, stack);
            }
            player.incrementStat(Stats.USED.getOrCreateStat(this));
            stack.decrementUnlessCreative(1, player);

            if (!player.isInCreativeMode())
                player.getInventory().insertStack(new ItemStack(Items.GLASS_BOTTLE));
        }
        else if (stack.isEmpty()) {
            return new ItemStack(Items.GLASS_BOTTLE);
        }
        user.emitGameEvent(GameEvent.DRINK);
        return stack;
    }

    @Override
    public int getMaxUseTime(ItemStack stack, LivingEntity user) {
        return 32;
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.DRINK;
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        return ItemUsage.consumeHeldItem(world, user, hand);
    }

    @Override
    public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType type) {
        int level = stack.getOrDefault(ModComponentTypes.CHARGE.get(), 0);
        List<StatusEffectInstance> effects = List.of(new StatusEffectInstance(ModStatusEffects.IRIDESCENT_EFFECT,
                Iridescence.getFullEffectLength(level), level));
        PotionContentsComponent.buildTooltip(effects, tooltip::add, 1.0F, context.getUpdateTickRate());
    }
}
