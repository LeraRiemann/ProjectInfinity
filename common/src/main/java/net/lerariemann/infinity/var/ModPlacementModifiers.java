package net.lerariemann.infinity.var;

import com.mojang.serialization.MapCodec;
import net.lerariemann.infinity.InfinityMod;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.gen.feature.FeaturePlacementContext;
import net.minecraft.world.gen.placementmodifier.AbstractConditionalPlacementModifier;
import net.minecraft.world.gen.placementmodifier.PlacementModifier;
import net.minecraft.world.gen.placementmodifier.PlacementModifierType;

import static net.lerariemann.infinity.PlatformMethods.freeze;
import static net.lerariemann.infinity.PlatformMethods.unfreeze;

public class ModPlacementModifiers {
    public static class CenterProximityPlacementModifier extends AbstractConditionalPlacementModifier {
        public static final MapCodec<CenterProximityPlacementModifier> MODIFIER_CODEC = (Codecs.POSITIVE_INT.fieldOf("radius")).xmap(
                CenterProximityPlacementModifier::new, a -> a.radius);
        private final int radius;

        private CenterProximityPlacementModifier(int radius) {
            this.radius = radius;
        }

        public static CenterProximityPlacementModifier of(int radius) {
            return new CenterProximityPlacementModifier(radius);
        }

        @Override
        protected boolean shouldPlace(FeaturePlacementContext context, Random random, BlockPos pos) {
            return pos.getX()*pos.getX() + pos.getY()*pos.getY() + pos.getZ()*pos.getZ() < this.radius*this.radius;
        }

        @Override
        public PlacementModifierType<?> getType() {
            return PlacementModifierType.RARITY_FILTER;
        }
    }

    static <P extends PlacementModifier> PlacementModifierType<P> register(String id, MapCodec<P> codec) {
        return Registry.register(Registries.PLACEMENT_MODIFIER_TYPE, InfinityMod.getId(id), () -> codec);
    }

    public static void registerModifiers() {
        unfreeze(Registries.PLACEMENT_MODIFIER_TYPE);
        register("center_proximity", ModPlacementModifiers.CenterProximityPlacementModifier.MODIFIER_CODEC);
        freeze(Registries.PLACEMENT_MODIFIER_TYPE);
    }
}
