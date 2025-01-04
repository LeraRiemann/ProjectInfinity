package net.lerariemann.infinity.mixin.fixes;

import com.llamalad7.mixinextras.sugar.Local;
import net.lerariemann.infinity.access.MobEntityAccess;
import net.lerariemann.infinity.util.InfinityMethods;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.SpawnHelper;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Many mobs including vanilla elder guardians, which are never intended to spawn from a biome, are always set as persistent.
 * This leads to them not respecting mobcaps and lagging everything out.
 * This mixin fixes that :D */
@Mixin(SpawnHelper.class)
public class SpawnHelperMixin {
    @Inject(method = "spawnEntitiesInChunk(Lnet/minecraft/entity/SpawnGroup;Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/world/chunk/Chunk;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/SpawnHelper$Checker;Lnet/minecraft/world/SpawnHelper$Runner;)V",
    at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/mob/MobEntity;initialize(Lnet/minecraft/world/ServerWorldAccess;Lnet/minecraft/world/LocalDifficulty;Lnet/minecraft/entity/SpawnReason;Lnet/minecraft/entity/EntityData;Lnet/minecraft/nbt/NbtCompound;)Lnet/minecraft/entity/EntityData;"))
    private static void inj(SpawnGroup group, ServerWorld world, Chunk chunk, BlockPos pos, SpawnHelper.Checker checker, SpawnHelper.Runner runner, CallbackInfo ci,
                            @Local MobEntity mobEntity) {
        if (InfinityMethods.isBiomeInfinity(world, pos)) {
            ((MobEntityAccess)mobEntity).infinity$setPersistent(false);
        }
    }
}
