package net.lerariemann.infinity.fluids;

import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.iridescence.IridescentCooldownEffect;
import net.lerariemann.infinity.iridescence.IridescentEffect;
import net.lerariemann.infinity.iridescence.IridescentSetupEffect;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.registry.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModEffectsForge {
    public static final DeferredRegister<StatusEffect> EFFECTS = DeferredRegister.create(RegistryKeys.STATUS_EFFECT, InfinityMod.MOD_ID);
    public static RegistryObject<IridescentEffect> IRIDESCENT_EFFECT;
    public static RegistryObject<IridescentSetupEffect> IRIDESCENT_SETUP;
    public static RegistryObject<IridescentCooldownEffect> IRIDESCENT_COOLDOWN;


    public static void register(IEventBus eventBus) {
        ModStatusEffects.IRIDESCENT_EFFECT = EFFECTS.register("iridescence",
                () -> new IridescentEffect(StatusEffectCategory.NEUTRAL, 0xFF00FF)).getDelegate();
        ModStatusEffects.IRIDESCENT_SETUP = EFFECTS.register("iridescent_setup",
                () -> new IridescentSetupEffect(StatusEffectCategory.NEUTRAL, 0xFF00FF)).getDelegate();
        ModStatusEffects.IRIDESCENT_COOLDOWN = EFFECTS.register("iridescent_cooldown",
                () -> new IridescentCooldownEffect(StatusEffectCategory.NEUTRAL, 0x884488)).getDelegate();
        EFFECTS.register(eventBus);
    }
}
