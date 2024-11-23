package net.lerariemann.infinity.entity;

import dev.architectury.registry.client.level.entity.EntityRendererRegistry;
import dev.architectury.registry.level.entity.EntityAttributeRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.lerariemann.infinity.entity.custom.ChaosPawn;
import net.lerariemann.infinity.util.RandomProvider;
import net.lerariemann.infinity.entity.client.ChaosPawnRenderer;
import net.lerariemann.infinity.entity.client.ChaosCreeperRenderer;
import net.lerariemann.infinity.entity.client.ChaosSkeletonRenderer;
import net.lerariemann.infinity.entity.client.ChaosSlimeRenderer;
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

    public static final RegistrySupplier<EntityType<ChaosSlime>> CHAOS_SLIME = INFINITY_ENTITIES.register("chaos_slime", () -> EntityType.Builder.create(ChaosSlime::new, SpawnGroup.MONSTER).dimensions(0.52f, 0.52f).maxTrackingRange(10).build("chaos_slime"));
    public static final RegistrySupplier<EntityType<ChaosSkeleton>> CHAOS_SKELETON = INFINITY_ENTITIES.register("chaos_skeleton", () -> EntityType.Builder.create(ChaosSkeleton::new, SpawnGroup.MONSTER).dimensions(0.6f, 1.99f).maxTrackingRange(8).build("chaos_skeleton"));
    public static final RegistrySupplier<EntityType<ChaosCreeper>> CHAOS_CREEPER = INFINITY_ENTITIES.register("chaos_creeper", () -> EntityType.Builder.create(ChaosCreeper::new, SpawnGroup.MONSTER).dimensions(0.6f, 1.7f).maxTrackingRange(8).build("chaos_creeper"));
    public static final RegistrySupplier<EntityType<ChaosPawn>> CHAOS_PAWN = INFINITY_ENTITIES.register("chaos_pawn", () -> EntityType.Builder.create(ChaosPawn::new, SpawnGroup.MONSTER).dimensions(0.6f, 1.8f).maxTrackingRange(10).build("chaos_pawn"));

    public static void registerEntities() {
        INFINITY_ENTITIES.register();
        registerAttributes();
    }

    public static void registerAttributes() {
        EntityAttributeRegistry.register(CHAOS_SLIME, ChaosSlime::createAttributes);
        EntityAttributeRegistry.register(CHAOS_SKELETON, AbstractSkeletonEntity::createAbstractSkeletonAttributes);
        EntityAttributeRegistry.register(CHAOS_CREEPER, ChaosCreeper::createCreeperAttributes);
        EntityAttributeRegistry.register(CHAOS_PAWN, ChaosPawn::createAttributes);
    }

    public static void registerSpawnRestrictions() {
        SpawnRestriction.register(CHAOS_SLIME.get(), SpawnLocationTypes.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, ChaosSlime::canSpawn);
        SpawnRestriction.register(CHAOS_SKELETON.get(), SpawnLocationTypes.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, ModEntities::canChaosMonsterSpawn);
        SpawnRestriction.register(CHAOS_CREEPER.get(), SpawnLocationTypes.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, ModEntities::canChaosMonsterSpawn);
        SpawnRestriction.register(CHAOS_PAWN.get(), SpawnLocationTypes.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, ChaosPawn::canSpawn);
        SpawnRestriction.register(EntityType.SNIFFER, SpawnLocationTypes.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, AnimalEntity::isValidNaturalSpawn);
        SpawnRestriction.register(EntityType.CAMEL, SpawnLocationTypes.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, AnimalEntity::isValidNaturalSpawn);
        SpawnRestriction.register(EntityType.ZOGLIN, SpawnLocationTypes.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, HostileEntity::canSpawnInDark);
        SpawnRestriction.register(EntityType.BREEZE, SpawnLocationTypes.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, HostileEntity::canSpawnInDark);
    }

    public static boolean canChaosMonsterSpawn(EntityType<? extends HostileEntity> type, ServerWorldAccess world, SpawnReason spawnReason, BlockPos pos, Random random) {
        return HostileEntity.canSpawnInDark(type, world, spawnReason, pos, random) && chaosMobsEnabled(world);
    }
    
    public static boolean chaosMobsEnabled(ServerWorldAccess world) {
        return RandomProvider.getProvider(world.getServer()).rule("chaosMobsEnabled");
    }

    public static void registerEntityRenderers() {
        EntityRendererRegistry.register(CHAOS_SLIME, ChaosSlimeRenderer::new);
        EntityRendererRegistry.register(CHAOS_SKELETON, ChaosSkeletonRenderer::new);
        EntityRendererRegistry.register(CHAOS_CREEPER, ChaosCreeperRenderer::new);
        EntityRendererRegistry.register(CHAOS_PAWN, ChaosPawnRenderer::new);
    }
}
