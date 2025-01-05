package net.lerariemann.infinity.util.var;

import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.util.InfinityMethods;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.WaterCreatureEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Difficulty;
import net.minecraft.world.ServerWorldAccess;

public interface InfinitySpawnHelper {
    static <T extends Entity> boolean canSpawn(EntityType<T> type, ServerWorldAccess world, SpawnReason spawnReason, BlockPos pos, Random random) {
        boolean bl;
        SpawnGroup sg = type.getSpawnGroup();
        if (isSpawnGroupWater(sg) || (WaterCreatureEntity.class.isAssignableFrom(type.getBaseClass())))
            bl = world.getFluidState(pos).isIn(FluidTags.WATER);
        else bl = (world.getBlockState(pos.down()).allowsSpawning(world, pos.down(), type));

        bl = bl || spawnReason.equals(SpawnReason.SPAWNER);

        if (sg.equals(SpawnGroup.MONSTER))
            bl = bl && (HostileEntity.isSpawnDark(world, pos, random) && world.getDifficulty() != Difficulty.PEACEFUL);
        if (Registries.ENTITY_TYPE.getId(type).getNamespace().equals(InfinityMod.MOD_ID)) bl = bl && InfinityMethods.chaosMobsEnabled();
        return bl;
    }

    static boolean isSpawnGroupWater(SpawnGroup sg) {
        return switch (sg) {
            case WATER_CREATURE, WATER_AMBIENT, UNDERGROUND_WATER_CREATURE, AXOLOTLS -> true;
            default -> false;
        };
    }
}
