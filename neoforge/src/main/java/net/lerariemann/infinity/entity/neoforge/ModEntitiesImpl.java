package net.lerariemann.infinity.entity.neoforge;

import dev.architectury.registry.level.entity.EntityAttributeRegistry;
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
import net.minecraft.entity.attribute.DefaultAttributeRegistry;
import net.minecraft.entity.attribute.EntityAttributes;
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

    public static void registerOtherSpawnRestrictions() {
        SpawnRestriction.register(EntityType.SNIFFER, SpawnLocationTypes.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, AnimalEntity::isValidNaturalSpawn);
        SpawnRestriction.register(EntityType.CAMEL, SpawnLocationTypes.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, AnimalEntity::isValidNaturalSpawn);
    }
}