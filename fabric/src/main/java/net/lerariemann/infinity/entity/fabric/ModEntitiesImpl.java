package net.lerariemann.infinity.entity.fabric;

import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.entity.ModEntities;
import net.lerariemann.infinity.entity.client.ChaosPawnRenderer;
import net.lerariemann.infinity.entity.client.DimensionalCreeperRenderer;
import net.lerariemann.infinity.entity.client.DimensionalSkeletonRenderer;
import net.lerariemann.infinity.entity.client.DimensionalSlimeRenderer;
import net.lerariemann.infinity.entity.custom.ChaosPawn;
import net.lerariemann.infinity.entity.custom.DimensionalCreeper;
import net.lerariemann.infinity.entity.custom.DimensionalSkeleton;
import net.lerariemann.infinity.entity.custom.DimensionalSlime;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnLocationTypes;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.entity.mob.AbstractSkeletonEntity;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.world.Heightmap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static net.lerariemann.infinity.entity.ModEntities.*;

public class ModEntitiesImpl {
    public static void registerEntityAttributes(EntityType<? extends MobEntity> entityType, String id) {
        if (Objects.equals(id, "dimensional_slime")) {
            FabricDefaultAttributeRegistry.register(entityType, DimensionalSlime.createAttributes());
            SpawnRestriction.register((EntityType<DimensionalSlime>) entityType, SpawnLocationTypes.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, DimensionalSlime::canSpawn);
        }
        else if (Objects.equals(id, "dimensional_skeleton")) {
            FabricDefaultAttributeRegistry.register(entityType, AbstractSkeletonEntity.createAbstractSkeletonAttributes());
            SpawnRestriction.register((EntityType<HostileEntity>) entityType, SpawnLocationTypes.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, ModEntities::canSpawnInDark);
        }
        else if (Objects.equals(id, "dimensional_creeper")) {
            FabricDefaultAttributeRegistry.register(entityType, CreeperEntity.createCreeperAttributes());
            SpawnRestriction.register((EntityType<HostileEntity>) entityType, SpawnLocationTypes.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, ModEntities::canSpawnInDark);

        }
        else if (Objects.equals(id, "chaos_pawn")) {
            FabricDefaultAttributeRegistry.register(entityType, ChaosPawn.createAttributes());
            SpawnRestriction.register((EntityType<ChaosPawn>)entityType, SpawnLocationTypes.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, ChaosPawn::canSpawn);

        }
    }

    public static void registerOtherSpawnRestrictions() {
        SpawnRestriction.register(EntityType.SNIFFER, SpawnLocationTypes.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, AnimalEntity::isValidNaturalSpawn);
        SpawnRestriction.register(EntityType.CAMEL, SpawnLocationTypes.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, AnimalEntity::isValidNaturalSpawn);
    }
}