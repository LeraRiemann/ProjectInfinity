package net.lerariemann.infinity.options;

import net.minecraft.entity.effect.InstantStatusEffect;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

public record EffectGiver(RegistryEntry<StatusEffect> id, int duration, int amplifier, int cooldown) {
    public static RegistryEntry<StatusEffect> effectOf(String id) {
        RegistryEntry<StatusEffect> entry = Registries.STATUS_EFFECT.getOrThrow(RegistryKey.of(RegistryKeys.STATUS_EFFECT, Identifier.of(id)));
        if (entry == null || entry.value() instanceof InstantStatusEffect) return null;
        return entry;
    }

    public static EffectGiver of(NbtCompound data) {
        if (data.contains("id")) return new EffectGiver(effectOf(data.getString("id")),
                InfinityOptions.test(data, "duration", 300),
                InfinityOptions.test(data, "amplifier", 0),
                Math.min(InfinityOptions.test(data, "cooldown", 100), 100));
        return new EffectGiver(null, 0,0,200);
    }

    public boolean isEmpty() {
        return id == null;
    }
}
