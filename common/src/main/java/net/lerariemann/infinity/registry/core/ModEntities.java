package net.lerariemann.infinity.registry.core;

import dev.architectury.registry.client.level.entity.EntityModelLayerRegistry;
import dev.architectury.registry.client.level.entity.EntityRendererRegistry;
import dev.architectury.registry.level.entity.EntityAttributeRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.fabricmc.fabric.impl.object.builder.FabricEntityType;
import net.lerariemann.infinity.entity.client.*;
import net.lerariemann.infinity.entity.custom.*;
import net.lerariemann.infinity.util.InfinityMethods;
import net.minecraft.entity.*;
import net.minecraft.entity.mob.AbstractSkeletonEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Difficulty;
import net.minecraft.world.Heightmap;
import net.minecraft.world.ServerWorldAccess;

import static net.lerariemann.infinity.InfinityMod.LOGGER;
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

    public static final RegistrySupplier<EntityType<ChaosSlime>> CHAOS_SLIME = INFINITY_ENTITIES
            .register("chaos_slime", () -> FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, ChaosSlime::new)
                    .dimensions(EntityDimensions.changing(0.52f, 0.52f))
                    .trackRangeBlocks(10)
                    .build());
    public static final RegistrySupplier<EntityType<ChaosSkeleton>> CHAOS_SKELETON = INFINITY_ENTITIES
            .register("chaos_skeleton", () -> EntityType.Builder.create(ChaosSkeleton::new, SpawnGroup.MONSTER)
                    .setDimensions(0.6f, 1.99f)
                    .maxTrackingRange(8)
                    .build("chaos_skeleton"));
    public static final RegistrySupplier<EntityType<ChaosCreeper>> CHAOS_CREEPER = INFINITY_ENTITIES
            .register("chaos_creeper", () -> EntityType.Builder.create(ChaosCreeper::new, SpawnGroup.MONSTER)
                    .setDimensions(0.6f, 1.7f)
                    .maxTrackingRange(8)
                    .build("chaos_creeper"));
    public static final RegistrySupplier<EntityType<ChaosPawn>> CHAOS_PAWN = INFINITY_ENTITIES
            .register("chaos_pawn", () -> EntityType.Builder.create(ChaosPawn::new, SpawnGroup.MONSTER)
                    .setDimensions(0.6f, 2.0f)
                    .maxTrackingRange(10)
                    .build("chaos_pawn"));
    public static final RegistrySupplier<EntityType<AntEntity>> ANT = INFINITY_ENTITIES
            .register("ant", () -> EntityType.Builder.create(AntEntity::new, SpawnGroup.CREATURE)
                    .setDimensions(0.6f, 0.3f)
                    .maxTrackingRange(10)
                    .build("ant"));
    public static final RegistrySupplier<EntityType<BishopEntity>> BISHOP = INFINITY_ENTITIES
            .register("bishop", () -> EntityType.Builder.create(BishopEntity::new, SpawnGroup.MONSTER)
                    .setDimensions(0.6f, 2.7f)
                    .maxTrackingRange(10)
                    .build("bishop"));

    public static void registerEntities() {
        INFINITY_ENTITIES.register();
        registerAttributes();
    }

    public static void registerAttributes() {
        EntityAttributeRegistry.register(CHAOS_SLIME, ChaosSlime::createAttributes);
        EntityAttributeRegistry.register(CHAOS_SKELETON, AbstractSkeletonEntity::createAbstractSkeletonAttributes);
        EntityAttributeRegistry.register(CHAOS_CREEPER, ChaosCreeper::createCreeperAttributes);
        EntityAttributeRegistry.register(CHAOS_PAWN, ChaosPawn::createAttributes);
        EntityAttributeRegistry.register(ANT, AntEntity::createAttributes);
        EntityAttributeRegistry.register(BISHOP, BishopEntity::createAttributes);
    }

    public static <T extends MobEntity> void registerSafe(EntityType<T> type, SpawnRestriction.Location location, Heightmap.Type heightmapType, SpawnRestriction.SpawnPredicate<T> predicate) {
        if (!SpawnRestriction.RESTRICTIONS.containsKey(type))
            SpawnRestriction.register(type, location, heightmapType, predicate);
    }

    public static void registerSpawnRestrictions() {
        SpawnRestriction.register(CHAOS_SLIME.get(), SpawnRestriction.Location.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, ChaosSlime::canSpawn);
        SpawnRestriction.register(CHAOS_SKELETON.get(), SpawnRestriction.Location.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, ModEntities::canChaosMonsterSpawn);
        SpawnRestriction.register(CHAOS_CREEPER.get(), SpawnRestriction.Location.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, ModEntities::canChaosMonsterSpawn);
        SpawnRestriction.register(CHAOS_PAWN.get(), SpawnRestriction.Location.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, ModEntities::canChessFigureSpawn);
        SpawnRestriction.register(ANT.get(), SpawnRestriction.Location.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, ModEntities::canChessFigureSpawn);
        SpawnRestriction.register(BISHOP.get(), SpawnRestriction.Location.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, ModEntities::canChessFigureSpawn);

        registerSafe(EntityType.SNIFFER, SpawnRestriction.Location.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, AnimalEntity::isValidNaturalSpawn);
        registerSafe(EntityType.CAMEL, SpawnRestriction.Location.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, AnimalEntity::isValidNaturalSpawn);
        registerSafe(EntityType.ZOGLIN, SpawnRestriction.Location.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, HostileEntity::canSpawnInDark);
    }

    public static boolean canChaosMonsterSpawn(EntityType<? extends HostileEntity> type, ServerWorldAccess world, SpawnReason spawnReason, BlockPos pos, Random random) {
        return HostileEntity.canSpawnInDark(type, world, spawnReason, pos, random)
                && InfinityMethods.chaosMobsEnabled();
    }
    public static boolean canChessFigureSpawn(EntityType<? extends AbstractChessFigure> type, ServerWorldAccess world, SpawnReason spawnReason, BlockPos pos, Random random) {
        return MobEntity.canMobSpawn(type, world, spawnReason, pos, random)
                && world.getDifficulty() != Difficulty.PEACEFUL
                && InfinityMethods.chaosMobsEnabled();
    }

    public static void registerEntityRenderers() {
        EntityModelLayerRegistry.register(AntModel.MODEL_LAYER, AntModel::getTexturedModelData);
        EntityModelLayerRegistry.register(BishopModel.MODEL_LAYER, BishopModel::getTexturedModelData);

        EntityRendererRegistry.register(CHAOS_SLIME, ChaosSlimeRenderer::new);
        EntityRendererRegistry.register(CHAOS_SKELETON, ChaosSkeletonRenderer::new);
        EntityRendererRegistry.register(CHAOS_CREEPER, ChaosCreeperRenderer::new);
        EntityRendererRegistry.register(CHAOS_PAWN, ChaosPawnRenderer::new);
        EntityRendererRegistry.register(ANT, AntRenderer::new);
        EntityRendererRegistry.register(BISHOP, BishopRenderer::new);
    }
}
