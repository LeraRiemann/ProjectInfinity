package net.lerariemann.infinity.entity;

import dev.architectury.registry.client.level.entity.EntityRendererRegistry;
import dev.architectury.registry.level.entity.EntityAttributeRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.lerariemann.infinity.util.RandomProvider;
import net.lerariemann.infinity.entity.client.ChaosPawnRenderer;
import net.lerariemann.infinity.entity.client.DimensionalCreeperRenderer;
import net.lerariemann.infinity.entity.client.DimensionalSkeletonRenderer;
import net.lerariemann.infinity.entity.client.DimensionalSlimeRenderer;
import net.lerariemann.infinity.entity.custom.*;
import net.minecraft.entity.*;
import net.minecraft.entity.mob.AbstractSkeletonEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;
import net.minecraft.world.ServerWorldAccess;

import static net.lerariemann.infinity.InfinityMod.MOD_ID;

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
    public static final DeferredRegister<EntityType<?>> INFINITY_ENTITIES = DeferredRegister.create(MOD_ID, RegistryKeys.ENTITY_TYPE);

    public static final RegistrySupplier<EntityType<DimensionalSlime>> DIMENSIONAL_SLIME = INFINITY_ENTITIES.register("dimensional_slime", () -> EntityType.Builder.create(DimensionalSlime::new, SpawnGroup.MONSTER).dimensions(0.52f, 0.52f).maxTrackingRange(10).build(null));
    public static final RegistrySupplier<EntityType<DimensionalSkeleton>> DIMENSIONAL_SKELETON = INFINITY_ENTITIES.register("dimensional_skeleton", () -> EntityType.Builder.create(DimensionalSkeleton::new, SpawnGroup.MONSTER).dimensions(0.6f, 1.99f).maxTrackingRange(8).build(null));
    public static final RegistrySupplier<EntityType<DimensionalCreeper>> DIMENSIONAL_CREEPER = INFINITY_ENTITIES.register("dimensional_creeper", () -> EntityType.Builder.create(DimensionalCreeper::new, SpawnGroup.MONSTER).dimensions(0.6f, 1.7f).maxTrackingRange(8).build(null));
    public static final RegistrySupplier<EntityType<ChaosPawn>> CHAOS_PAWN = INFINITY_ENTITIES.register("chaos_pawn", () -> EntityType.Builder.create(ChaosPawn::new, SpawnGroup.MONSTER).dimensions(0.6f, 1.8f).maxTrackingRange(10).build(null));

    public static void registerEntities() {
        INFINITY_ENTITIES.register();
        registerAttributes();
    }

    public static void registerAttributes() {
        EntityAttributeRegistry.register(DIMENSIONAL_SLIME, DimensionalSlime::createAttributes);
        EntityAttributeRegistry.register(DIMENSIONAL_SKELETON, AbstractSkeletonEntity::createAbstractSkeletonAttributes);
        EntityAttributeRegistry.register(DIMENSIONAL_CREEPER, DimensionalCreeper::createCreeperAttributes);
        EntityAttributeRegistry.register(CHAOS_PAWN, ChaosPawn::createAttributes);
    }

    public static void registerSpawnRestrictions() {
        SpawnRestriction.register(DIMENSIONAL_SLIME.get(), SpawnLocationTypes.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, DimensionalSlime::canSpawn);
        SpawnRestriction.register(DIMENSIONAL_SKELETON.get(), SpawnLocationTypes.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, ModEntities::canSpawnInDark);
        SpawnRestriction.register(DIMENSIONAL_CREEPER.get(), SpawnLocationTypes.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, ModEntities::canSpawnInDark);
        SpawnRestriction.register(CHAOS_PAWN.get(), SpawnLocationTypes.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, ChaosPawn::canSpawn);
        SpawnRestriction.register(EntityType.SNIFFER, SpawnLocationTypes.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, AnimalEntity::isValidNaturalSpawn);
        SpawnRestriction.register(EntityType.CAMEL, SpawnLocationTypes.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, AnimalEntity::isValidNaturalSpawn);
        SpawnRestriction.register(EntityType.ZOGLIN, SpawnLocationTypes.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, HostileEntity::canSpawnInDark);
    }

    public static boolean canSpawnInDark(EntityType<? extends HostileEntity> type, ServerWorldAccess world, SpawnReason spawnReason, BlockPos pos, Random random) {
        return HostileEntity.canSpawnInDark(type, world, spawnReason, pos, random) &&
                RandomProvider.getProvider(world.toServerWorld().getServer()).rule("chaosMobsEnabled");
    }

    public static void registerEntityRenderers() {
        EntityRendererRegistry.register(DIMENSIONAL_SLIME, DimensionalSlimeRenderer::new);
        EntityRendererRegistry.register(DIMENSIONAL_SKELETON, DimensionalSkeletonRenderer::new);
        EntityRendererRegistry.register(DIMENSIONAL_CREEPER, DimensionalCreeperRenderer::new);
        EntityRendererRegistry.register(CHAOS_PAWN, ChaosPawnRenderer::new);
    }
}
