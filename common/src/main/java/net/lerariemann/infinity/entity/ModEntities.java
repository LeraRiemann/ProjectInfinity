package net.lerariemann.infinity.entity;

import dev.architectury.injectables.annotations.ExpectPlatform;
import dev.architectury.registry.client.level.entity.EntityRendererRegistry;
import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.access.MinecraftServerAccess;
import net.lerariemann.infinity.entity.custom.*;
import net.lerariemann.infinity.entity.client.*;
import net.minecraft.entity.*;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;
import net.minecraft.world.ServerWorldAccess;

public class ModEntities {
    public static void copy(MobEntity from, MobEntity e) {
        e.refreshPositionAndAngles(from.getX(), from.getY(), from.getZ(), from.getYaw(), from.getPitch());
        e.setHealth(from.getHealth());
        e.bodyYaw = from.bodyYaw;
        if (from.hasCustomName()) {
            e.setCustomName(from.getCustomName());
            e.setCustomNameVisible(from.isCustomNameVisible());
        }
        if (from.isPersistent()) {
            e.setPersistent();
        }
        e.setInvulnerable(from.isInvulnerable());
        e.setStackInHand(Hand.MAIN_HAND, from.getStackInHand(Hand.MAIN_HAND));
        e.setStackInHand(Hand.OFF_HAND, from.getStackInHand(Hand.OFF_HAND));
    }

    @ExpectPlatform
    public static <T extends Entity> EntityType<T> register(String id, EntityType.Builder<T> type) {
        throw new AssertionError();
    }
    public static final EntityType<DimensionalSlime> DIMENSIONAL_SLIME = register("dimensional_slime",
            EntityType.Builder.create(DimensionalSlime::new, SpawnGroup.MONSTER)
                    .dimensions(0.52f, 0.52f).maxTrackingRange(10));
    public static final EntityType<DimensionalSkeleton> DIMENSIONAL_SKELETON = register("dimensional_skeleton",
            EntityType.Builder.create(DimensionalSkeleton::new, SpawnGroup.MONSTER)
                    .dimensions(0.6f, 1.99f).maxTrackingRange(8));
    public static final EntityType<DimensionalCreeper> DIMENSIONAL_CREEPER = register("dimensional_creeper",
            EntityType.Builder.create(DimensionalCreeper::new, SpawnGroup.MONSTER)
                    .dimensions(0.6f, 1.7f).maxTrackingRange(8));
    public static final EntityType<ChaosPawn> CHAOS_PAWN = register("chaos_pawn",
            EntityType.Builder.create(ChaosPawn::new, SpawnGroup.MONSTER)
                    .dimensions(0.6f, 1.8f).maxTrackingRange(10));

    @ExpectPlatform
    public static void registerEntities() {
        throw new AssertionError();
    }

    public static boolean canSpawnInDark(EntityType<? extends HostileEntity> type, ServerWorldAccess world, SpawnReason spawnReason, BlockPos pos, Random random) {
        return HostileEntity.canSpawnInDark(type, world, spawnReason, pos, random) &&
                ((MinecraftServerAccess)world.toServerWorld().getServer()).projectInfinity$getDimensionProvider().rule("chaosMobsEnabled");
    }

    @ExpectPlatform
    public static void registerSpawnRestrictions() {
        throw new AssertionError();

    }

    @ExpectPlatform
    public static void registerEntityRenderers() {
        throw new AssertionError();
    }
}
