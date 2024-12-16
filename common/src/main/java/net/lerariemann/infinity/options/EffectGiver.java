package net.lerariemann.infinity.options;

import net.minecraft.entity.effect.InstantStatusEffect;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public record EffectGiver(RegistryEntry<StatusEffect> id, int duration, int amplifier, int cooldown) {
    public static RegistryEntry<StatusEffect> effectOf(String id) {
        RegistryEntry<StatusEffect> entry = Registries.STATUS_EFFECT.entryOf(RegistryKey.of(RegistryKeys.STATUS_EFFECT, new Identifier(id)));
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

    public void tryGiveEffect(ServerPlayerEntity player) {
        if (!isEmpty() && player.getWorld().getTime() % cooldown == 0) {
            player.addStatusEffect(new StatusEffectInstance(id, duration, amplifier));
        }
    }

    public boolean isEmpty() {
        return id == null;
    }
}
