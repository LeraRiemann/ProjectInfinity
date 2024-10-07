package net.lerariemann.infinity.var;

import com.mojang.serialization.MapCodec;
import dev.architectury.injectables.annotations.ExpectPlatform;
import dev.architectury.platform.Platform;
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
    @ExpectPlatform
    static <P extends PlacementModifier> PlacementModifierType<P> register(String id, MapCodec<P> codec) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void registerModifiers() {
        throw new AssertionError();
    }
}
