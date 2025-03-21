package net.lerariemann.infinity.registry.core;

import dev.architectury.platform.Platform;
import net.lerariemann.infinity.iridescence.IridescentCooldownEffect;
import net.lerariemann.infinity.iridescence.IridescentEffect;
import net.lerariemann.infinity.util.InfinityMethods;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;

public class ModStatusEffects {
    public static RegistryEntry<StatusEffect> IRIDESCENT_EFFECT;
    public static RegistryEntry<StatusEffect> IRIDESCENT_SETUP;
    public static RegistryEntry<StatusEffect> IRIDESCENT_COOLDOWN;
    public static RegistryEntry<StatusEffect> AFTERGLOW;

    public static void registerModEffectsFabric() {
        IRIDESCENT_EFFECT = Registry.registerReference(Registries.STATUS_EFFECT, InfinityMethods.getId("iridescence"),
                new IridescentEffect(StatusEffectCategory.NEUTRAL, 0xFF66FF));
        IRIDESCENT_SETUP = Registry.registerReference(Registries.STATUS_EFFECT, InfinityMethods.getId("iridescent_setup"),
                new IridescentEffect.Setup(StatusEffectCategory.NEUTRAL, 0xFF00FF));
        IRIDESCENT_COOLDOWN = Registry.registerReference(Registries.STATUS_EFFECT, InfinityMethods.getId("iridescent_cooldown"),
                new IridescentCooldownEffect(StatusEffectCategory.NEUTRAL, 0x884488));
        AFTERGLOW = Registry.registerReference(Registries.STATUS_EFFECT, InfinityMethods.getId("afterglow"),
                getAfterglowInstanceForReg());
    }

    public static StatusEffect getAfterglowInstanceForReg() {
        return new StatusEffect(StatusEffectCategory.BENEFICIAL, 0xAA77DD)
                .addAttributeModifier(EntityAttributes.GENERIC_ATTACK_SPEED,
                        InfinityMethods.getId("effect.afterglow"), 0.1F,
                        EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
                .addAttributeModifier(EntityAttributes.GENERIC_MOVEMENT_SPEED,
                        InfinityMethods.getId("effect.afterglow"), 0.2F,
                        EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
                .addAttributeModifier(EntityAttributes.GENERIC_GRAVITY,
                        InfinityMethods.getId("effect.afterglow"), -0.1F,
                        EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    }

    public static void registerModEffects() {
        if (Platform.isFabric()) registerModEffectsFabric();
    }

    public interface SpecialEffect {
        void onRemoved(LivingEntity entity);
        void tryApplySpecial(LivingEntity entity, int duration, int amplifier);
    }
}

