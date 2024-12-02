package net.lerariemann.infinity.iridescence;

import dev.architectury.platform.Platform;
import net.lerariemann.infinity.util.InfinityMethods;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;

public class ModStatusEffects {
    public static RegistryEntry<? extends StatusEffect> IRIDESCENT_EFFECT;
    public static RegistryEntry<? extends StatusEffect> IRIDESCENT_SETUP;
    public static RegistryEntry<? extends StatusEffect> IRIDESCENT_COOLDOWN;

    public static void registerModEffectsFabric() {
        IRIDESCENT_EFFECT = Registry.registerReference(Registries.STATUS_EFFECT, InfinityMethods.getId("iridescence"),
                new IridescentEffect(StatusEffectCategory.NEUTRAL, 0xFF00FF));
        IRIDESCENT_SETUP = Registry.registerReference(Registries.STATUS_EFFECT, InfinityMethods.getId("iridescent_setup"),
                new IridescentSetupEffect(StatusEffectCategory.NEUTRAL, 0xFF00FF));
        IRIDESCENT_COOLDOWN = Registry.registerReference(Registries.STATUS_EFFECT, InfinityMethods.getId("iridescent_cooldown"),
                new IridescentCooldownEffect(StatusEffectCategory.NEUTRAL, 0x884488));
    }

    public static void registerModEffects() {
        if (Platform.isFabric()) registerModEffectsFabric();
    }

    public interface SpecialEffect {
        void onRemoved(LivingEntity entity);
        void tryApplySpecial(LivingEntity entity, int duration, int amplifier);
    }
}

