package net.lerariemann.infinity.fluids.forge;

import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.iridescence.IridescentCooldownEffect;
import net.lerariemann.infinity.iridescence.IridescentEffect;
import net.lerariemann.infinity.registry.core.ModStatusEffects;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.registry.RegistryKeys;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import org.apache.logging.log4j.LogManager;

public class ModEffectsForge {
    public static final DeferredRegister<StatusEffect> EFFECTS = DeferredRegister.create(RegistryKeys.STATUS_EFFECT, InfinityMod.MOD_ID);
    public static RegistryObject<IridescentEffect> IRIDESCENT_EFFECT;
    public static RegistryObject<IridescentEffect.Setup> IRIDESCENT_SETUP;
    public static RegistryObject<IridescentCooldownEffect> IRIDESCENT_COOLDOWN;
    public static RegistryObject<StatusEffect> AFTERGLOW;


    public static void register(IEventBus eventBus) {
        IRIDESCENT_EFFECT = EFFECTS.register("iridescence",
                () -> new IridescentEffect(StatusEffectCategory.NEUTRAL, 0xFF00FF));
        IRIDESCENT_SETUP = EFFECTS.register("iridescent_setup",
                () -> new IridescentEffect.Setup(StatusEffectCategory.NEUTRAL, 0xFF00FF));
        IRIDESCENT_COOLDOWN = EFFECTS.register("iridescent_cooldown",
                () -> new IridescentCooldownEffect(StatusEffectCategory.NEUTRAL, 0x884488));
        AFTERGLOW = EFFECTS.register("afterglow",
                ModStatusEffects::getAfterglowInstanceForReg);
        LogManager.getLogger().info("Registered effects!!");
        EFFECTS.register(eventBus);
    }
}