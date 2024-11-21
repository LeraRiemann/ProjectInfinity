package net.lerariemann.infinity.fluids;

import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.iridescence.IridescentCooldownEffect;
import net.lerariemann.infinity.iridescence.IridescentEffect;
import net.lerariemann.infinity.iridescence.IridescentSetupEffect;
import net.lerariemann.infinity.iridescence.ModStatusEffects;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import org.apache.logging.log4j.LogManager;

public class ModEffectsForge {
    public static final DeferredRegister<StatusEffect> EFFECTS = DeferredRegister.create(RegistryKeys.STATUS_EFFECT, InfinityMod.MOD_ID);

    public static void register(IEventBus eventBus) {
        ModStatusEffects.IRIDESCENT_EFFECT = EFFECTS.register("iridescence",
                () -> new IridescentEffect(StatusEffectCategory.NEUTRAL, 0xFF00FF)).getHolder().get();
        ModStatusEffects.IRIDESCENT_SETUP = EFFECTS.register("iridescent_setup",
                () -> new IridescentSetupEffect(StatusEffectCategory.NEUTRAL, 0xFF00FF)).getHolder().get();
        ModStatusEffects.IRIDESCENT_COOLDOWN = EFFECTS.register("iridescent_cooldown",
                () -> new IridescentCooldownEffect(StatusEffectCategory.NEUTRAL, 0x884488)).getHolder().get();
        LogManager.getLogger().info("Registered effects!!");
        EFFECTS.register(eventBus);
    }
}
