package net.lerariemann.infinity.var.neoforge;

import com.mojang.serialization.MapCodec;
import dev.architectury.platform.Platform;
import net.fabricmc.fabric.mixin.registry.sync.BaseMappedRegistryAccessor;
import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.var.ModPlacementModifiers;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.world.gen.placementmodifier.PlacementModifier;
import net.minecraft.world.gen.placementmodifier.PlacementModifierType;

public class ModPlacementModifiersImpl {
    static <P extends PlacementModifier> PlacementModifierType<P> register(String id, MapCodec<P> codec) {
        ((BaseMappedRegistryAccessor) Registries.PLACEMENT_MODIFIER_TYPE).invokeUnfreeze();
        return Registry.register(Registries.PLACEMENT_MODIFIER_TYPE, InfinityMod.getId(id), () -> codec);

    }
    public static void registerModifiers() {
        register("center_proximity", ModPlacementModifiers.CenterProximityPlacementModifier.MODIFIER_CODEC);
        Registries.PLACEMENT_MODIFIER_TYPE.freeze();
    }
}
