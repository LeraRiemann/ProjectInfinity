package net.lerariemann.infinity.mixin.fixes;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.LakeFeature;
import net.minecraft.world.gen.feature.util.FeatureContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Mojang goofed their lakes into deprecation, i'm fixing :D */
@Mixin(LakeFeature.class)
public class LakeFeatureMixin {
    @WrapOperation(method="generate", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/StructureWorldAccess;getBiome(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/registry/entry/RegistryEntry;"))
    RegistryEntry<Biome> inj(StructureWorldAccess instance, BlockPos blockPos, Operation<RegistryEntry<Biome>> original,
                      @Local(argsOnly = true) FeatureContext<LakeFeature.Config> context) {
        return context.getWorld().getBiome(context.getOrigin());
    }
}
