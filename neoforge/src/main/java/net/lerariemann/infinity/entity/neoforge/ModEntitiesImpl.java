package net.lerariemann.infinity.entity.neoforge;

import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.entity.ModEntities;
import net.lerariemann.infinity.entity.client.ChaosPawnRenderer;
import net.lerariemann.infinity.entity.client.DimensionalCreeperRenderer;
import net.lerariemann.infinity.entity.client.DimensionalSkeletonRenderer;
import net.lerariemann.infinity.entity.client.DimensionalSlimeRenderer;
import net.lerariemann.infinity.entity.custom.ChaosPawn;
import net.lerariemann.infinity.entity.custom.DimensionalSlime;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnLocationTypes;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.entity.mob.AbstractSkeletonEntity;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.world.Heightmap;

import static net.lerariemann.infinity.entity.ModEntities.*;

public class ModEntitiesImpl {
    //TODO reimplement
    public static void registerEntities() {

//        FabricDefaultAttributeRegistry.register(DIMENSIONAL_SLIME, DimensionalSlime.createAttributes());
//        FabricDefaultAttributeRegistry.register(DIMENSIONAL_SKELETON, AbstractSkeletonEntity.createAbstractSkeletonAttributes());
//        FabricDefaultAttributeRegistry.register(DIMENSIONAL_CREEPER, CreeperEntity.createCreeperAttributes());
//        FabricDefaultAttributeRegistry.register(CHAOS_PAWN, ChaosPawn.createAttributes());
    }
    public static void registerSpawnRestrictions() {
//        SpawnRestriction.register(DIMENSIONAL_SLIME, SpawnLocationTypes.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, DimensionalSlime::canSpawn);
//        SpawnRestriction.register(DIMENSIONAL_SKELETON, SpawnLocationTypes.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, ModEntities::canSpawnInDark);
//        SpawnRestriction.register(DIMENSIONAL_CREEPER, SpawnLocationTypes.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, ModEntities::canSpawnInDark);
//        SpawnRestriction.register(CHAOS_PAWN, SpawnLocationTypes.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, ChaosPawn::canSpawn);
//        SpawnRestriction.register(EntityType.SNIFFER, SpawnLocationTypes.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, AnimalEntity::isValidNaturalSpawn);
//        SpawnRestriction.register(EntityType.CAMEL, SpawnLocationTypes.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, AnimalEntity::isValidNaturalSpawn);
    }
    public static void registerEntityRenderers() {
//        EntityRendererRegistry.register(DIMENSIONAL_SLIME, DimensionalSlimeRenderer::new);
//        EntityRendererRegistry.register(DIMENSIONAL_SKELETON, DimensionalSkeletonRenderer::new);
//        EntityRendererRegistry.register(DIMENSIONAL_CREEPER, DimensionalCreeperRenderer::new);
//        EntityRendererRegistry.register(CHAOS_PAWN, ChaosPawnRenderer::new);
    }

    public static <T extends Entity> EntityType<T> register(String id, EntityType.Builder<T> type) {
        return Registry.register(Registries.ENTITY_TYPE, InfinityMod.getId(id), type.build(null));
    }
}