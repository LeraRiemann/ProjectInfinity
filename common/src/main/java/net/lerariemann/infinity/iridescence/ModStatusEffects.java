package net.lerariemann.infinity.iridescence;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.lerariemann.infinity.InfinityMod;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.registry.RegistryKeys;

public class ModStatusEffects {
    public static final DeferredRegister<StatusEffect> STATUE_EFFECTS = DeferredRegister.create(InfinityMod.MOD_ID, RegistryKeys.STATUS_EFFECT);
    public static final RegistrySupplier<StatusEffect> IRIDESCENT_EFFECT = STATUE_EFFECTS.register("iridescence",
            () -> new IridescentEffect(StatusEffectCategory.NEUTRAL, 0xFF00FF));
    public static final RegistrySupplier<StatusEffect> IRIDESCENT_SETUP = STATUE_EFFECTS.register("iridescent_setup",
            () -> new IridescentSetupEffect(StatusEffectCategory.NEUTRAL, 0xFF00FF));
    public static final RegistrySupplier<StatusEffect> IRIDESCENT_COOLDOWN = STATUE_EFFECTS.register("iridescent_cooldown",
            () -> new IridescentCooldownEffect(StatusEffectCategory.NEUTRAL, 0x884488));

    public static void registerModEffects() {
        STATUE_EFFECTS.register();
    }

    public interface SpecialEffect {
        void onRemoved(LivingEntity entity);
        void tryApplySpecial(LivingEntity entity, int duration, int amplifier);
    }
}

