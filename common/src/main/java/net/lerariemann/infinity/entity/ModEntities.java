package net.lerariemann.infinity.entity;

import dev.architectury.registry.client.level.entity.EntityRendererRegistry;
import dev.architectury.registry.level.entity.EntityAttributeRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
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

    public static final RegistrySupplier<EntityType<ChaosSlime>> CHAOS_SLIME = INFINITY_ENTITIES.register("chaos_slime", () -> FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, ChaosSlime::new).dimensions(EntityDimensions.changing(0.52f, 0.52f)).trackRangeChunks(10).build());
    public static final RegistrySupplier<EntityType<ChaosSkeleton>> CHAOS_SKELETON = INFINITY_ENTITIES.register("chaos_skeleton", () -> FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, ChaosSkeleton::new).dimensions(EntityDimensions.changing(0.6f, 1.99f)).trackRangeChunks(8).build());
    public static final RegistrySupplier<EntityType<ChaosCreeper>> CHAOS_CREEPER = INFINITY_ENTITIES.register("chaos_creeper", () ->FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, ChaosCreeper::new).dimensions(EntityDimensions.changing(0.6f, 1.7f)).trackRangeChunks(8).build());
    public static final RegistrySupplier<EntityType<ChaosPawn>> CHAOS_PAWN = INFINITY_ENTITIES.register("chaos_pawn", () -> FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, ChaosPawn::new).dimensions(EntityDimensions.changing(0.6f, 1.8f)).trackRangeChunks(10).build());

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
        SpawnRestriction.register(CHAOS_SLIME.get(), SpawnRestriction.Location.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, ChaosSlime::canSpawn);
        SpawnRestriction.register(CHAOS_SKELETON.get(), SpawnRestriction.Location.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, ModEntities::canChaosMonsterSpawn);
        SpawnRestriction.register(CHAOS_CREEPER.get(), SpawnRestriction.Location.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, ModEntities::canChaosMonsterSpawn);
        SpawnRestriction.register(CHAOS_PAWN.get(), SpawnRestriction.Location.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, ChaosPawn::canSpawn);
        SpawnRestriction.register(EntityType.SNIFFER, SpawnRestriction.Location.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, AnimalEntity::isValidNaturalSpawn);
        SpawnRestriction.register(EntityType.CAMEL, SpawnRestriction.Location.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, AnimalEntity::isValidNaturalSpawn);
        SpawnRestriction.register(EntityType.ZOGLIN, SpawnRestriction.Location.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, HostileEntity::canSpawnInDark);
    }

    public static boolean canChaosMonsterSpawn(EntityType<? extends HostileEntity> type, ServerWorldAccess world, SpawnReason spawnReason, BlockPos pos, Random random) {
        return HostileEntity.canSpawnInDark(type, world, spawnReason, pos, random) && chaosMobsEnabled(world);
    }

    public static boolean chaosMobsEnabled(ServerWorldAccess world) {
        return RandomProvider.getProvider(world.toServerWorld().getServer()).rule("chaosMobsEnabled");
    }

    public static void registerEntityRenderers() {
        EntityRendererRegistry.register(CHAOS_SLIME, ChaosSlimeRenderer::new);
        EntityRendererRegistry.register(CHAOS_SKELETON, ChaosSkeletonRenderer::new);
        EntityRendererRegistry.register(CHAOS_CREEPER, ChaosCreeperRenderer::new);
        EntityRendererRegistry.register(CHAOS_PAWN, ChaosPawnRenderer::new);
    }
}
