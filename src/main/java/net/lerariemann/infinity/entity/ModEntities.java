package net.lerariemann.infinity.entity;

import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.lerariemann.infinity.InfinityMod;
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
import net.minecraft.world.Heightmap;

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

    public static <T extends Entity> EntityType<T> register(String id, FabricEntityTypeBuilder<T> type) {
        return Registry.register(Registries.ENTITY_TYPE, InfinityMod.getId(id), type.build());
    }
    public static final EntityType<DimensionalSlime> DIMENSIONAL_SLIME = register("dimensional_slime",
            FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, DimensionalSlime::new)
                    .dimensions(EntityDimensions.changing(2.04f, 2.04f)).trackRangeChunks(10));
    public static final EntityType<DimensionalSkeleton> DIMENSIONAL_SKELETON = register("dimensional_skeleton",
            FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, DimensionalSkeleton::new)
                    .dimensions(EntityDimensions.fixed(0.6f, 1.99f)).trackRangeChunks(8));
    public static final EntityType<DimensionalCreeper> DIMENSIONAL_CREEPER = register("dimensional_creeper",
            FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, DimensionalCreeper::new)
                    .dimensions(EntityDimensions.fixed(0.6f, 1.7f)).trackRangeChunks(8));
    public static final EntityType<ChaosPawn> CHAOS_PAWN = register("chaos_pawn",
            FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, ChaosPawn::new)
                    .dimensions(EntityDimensions.fixed(0.6f, 1.8f)).trackRangeChunks(10));

    public static void registerEntities() {
        FabricDefaultAttributeRegistry.register(DIMENSIONAL_SLIME, DimensionalSlime.createAttributes());
        FabricDefaultAttributeRegistry.register(DIMENSIONAL_SKELETON, AbstractSkeletonEntity.createAbstractSkeletonAttributes());
        FabricDefaultAttributeRegistry.register(DIMENSIONAL_CREEPER, CreeperEntity.createCreeperAttributes());
        FabricDefaultAttributeRegistry.register(CHAOS_PAWN, ChaosPawn.createAttributes());
    }

    public static void registerSpawnRestrictions() {
        SpawnRestriction.register(DIMENSIONAL_SLIME, SpawnRestriction.Location.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, DimensionalSlime::canSpawn);
        SpawnRestriction.register(DIMENSIONAL_SKELETON, SpawnRestriction.Location.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, HostileEntity::canSpawnInDark);
        SpawnRestriction.register(DIMENSIONAL_CREEPER, SpawnRestriction.Location.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, HostileEntity::canSpawnInDark);
        SpawnRestriction.register(CHAOS_PAWN, SpawnRestriction.Location.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, ChaosPawn::canSpawn);
        SpawnRestriction.register(EntityType.SNIFFER, SpawnRestriction.Location.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, AnimalEntity::isValidNaturalSpawn);
        SpawnRestriction.register(EntityType.CAMEL, SpawnRestriction.Location.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, AnimalEntity::isValidNaturalSpawn);
    }

    public static void registerEntityRenderers() {
        EntityRendererRegistry.register(DIMENSIONAL_SLIME, DimensionalSlimeRenderer::new);
        EntityRendererRegistry.register(DIMENSIONAL_SKELETON, DimensionalSkeletonRenderer::new);
        EntityRendererRegistry.register(DIMENSIONAL_CREEPER, DimensionalCreeperRenderer::new);
        EntityRendererRegistry.register(CHAOS_PAWN, ChaosPawnRenderer::new);
    }
}
