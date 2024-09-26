package net.lerariemann.infinity.entity;

import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.access.MinecraftServerAccess;
import net.lerariemann.infinity.entity.custom.*;
import net.lerariemann.infinity.entity.client.*;
import net.minecraft.entity.*;
import net.minecraft.entity.mob.AbstractSkeletonEntity;
import net.minecraft.entity.mob.CreeperEntity;
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

    public static <T extends Entity> EntityType<T> register(String id, EntityType.Builder<T> type) {
        return Registry.register(Registries.ENTITY_TYPE, InfinityMod.getId(id), type.build());
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

    public static void registerEntities() {
        FabricDefaultAttributeRegistry.register(DIMENSIONAL_SLIME, DimensionalSlime.createAttributes());
        FabricDefaultAttributeRegistry.register(DIMENSIONAL_SKELETON, AbstractSkeletonEntity.createAbstractSkeletonAttributes());
        FabricDefaultAttributeRegistry.register(DIMENSIONAL_CREEPER, CreeperEntity.createCreeperAttributes());
        FabricDefaultAttributeRegistry.register(CHAOS_PAWN, ChaosPawn.createAttributes());
    }

    public static boolean canSpawnInDark(EntityType<? extends HostileEntity> type, ServerWorldAccess world, SpawnReason spawnReason, BlockPos pos, Random random) {
        return HostileEntity.canSpawnInDark(type, world, spawnReason, pos, random) &&
                ((MinecraftServerAccess)world.toServerWorld().getServer()).projectInfinity$getDimensionProvider().rule("chaosMobsEnabled");
    }

    public static void registerSpawnRestrictions() {
        SpawnRestriction.register(DIMENSIONAL_SLIME, SpawnLocationTypes.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, DimensionalSlime::canSpawn);
        SpawnRestriction.register(DIMENSIONAL_SKELETON, SpawnLocationTypes.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, ModEntities::canSpawnInDark);
        SpawnRestriction.register(DIMENSIONAL_CREEPER, SpawnLocationTypes.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, ModEntities::canSpawnInDark);
        SpawnRestriction.register(CHAOS_PAWN, SpawnLocationTypes.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, ChaosPawn::canSpawn);
        SpawnRestriction.register(EntityType.SNIFFER, SpawnLocationTypes.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, AnimalEntity::isValidNaturalSpawn);
        SpawnRestriction.register(EntityType.CAMEL, SpawnLocationTypes.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, AnimalEntity::isValidNaturalSpawn);
    }

    public static void registerEntityRenderers() {
        EntityRendererRegistry.register(DIMENSIONAL_SLIME, DimensionalSlimeRenderer::new);
        EntityRendererRegistry.register(DIMENSIONAL_SKELETON, DimensionalSkeletonRenderer::new);
        EntityRendererRegistry.register(DIMENSIONAL_CREEPER, DimensionalCreeperRenderer::new);
        EntityRendererRegistry.register(CHAOS_PAWN, ChaosPawnRenderer::new);
    }
}
