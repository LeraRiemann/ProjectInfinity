package net.lerariemann.infinity.var;

import com.mojang.serialization.MapCodec;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.gen.feature.FeaturePlacementContext;
import net.minecraft.world.gen.placementmodifier.AbstractConditionalPlacementModifier;
import net.minecraft.world.gen.placementmodifier.PlacementModifier;
import net.minecraft.world.gen.placementmodifier.PlacementModifierType;

import static net.lerariemann.infinity.InfinityMod.MOD_ID;

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

    public static final DeferredRegister<PlacementModifierType<?>> PLACEMENT_MODIFIER_TYPES = DeferredRegister.create(MOD_ID, RegistryKeys.PLACEMENT_MODIFIER_TYPE);


    static RegistrySupplier<PlacementModifierType<?>> register(String id, MapCodec<? extends PlacementModifier> codec) {
            return PLACEMENT_MODIFIER_TYPES.register(id, () -> () -> (MapCodec<PlacementModifier>) codec);
    }

    public static void registerModifiers() {
        register("center_proximity", ModPlacementModifiers.CenterProximityPlacementModifier.MODIFIER_CODEC);
    }
}
