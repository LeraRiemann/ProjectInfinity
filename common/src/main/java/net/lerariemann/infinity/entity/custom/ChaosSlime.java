package net.lerariemann.infinity.entity.custom;

import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.util.InfinityMethods;
import net.lerariemann.infinity.util.core.ConfigType;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.loot.LootTable;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.*;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

public class ChaosSlime extends SlimeEntity implements TintableEntity {
    public static final TrackedData<BlockState> core = DataTracker.registerData(ChaosSlime.class, TrackedDataHandlerRegistry.BLOCK_STATE);
    public static final TrackedData<Integer> color = DataTracker.registerData(ChaosSlime.class, TrackedDataHandlerRegistry.INTEGER);

    public ChaosSlime(EntityType<? extends ChaosSlime> entityType, World world) {
        super(entityType, world);
    }

    @Override
    public int getColorNamed() {
        return hasCustomName() ? TintableEntity.getColorNamed(getName().getString(), age, getId()) : -1;
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(core, Blocks.STONE.getDefaultState());
        builder.add(color, 0);
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return HostileEntity.createHostileAttributes().add(EntityAttributes.MOVEMENT_SPEED, 0.2f);
    }

    @Override
    @Nullable
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData) {
        Random r = new Random();
        this.dataTracker.set(core, Registries.BLOCK.get(Identifier.of(InfinityMod.provider.randomName(r, ConfigType.ALL_BLOCKS))).getDefaultState());
        this.dataTracker.set(color, r.nextInt(16777216));
        return super.initialize(world, difficulty, spawnReason, entityData);
    }

    @Override
    public boolean canSpawn(WorldView world) {
        return world.doesNotIntersectEntities(this);
    }

    public void setColor(int c) {
        this.dataTracker.set(color, c);
    }
    @Override
    public int getColor() {
        return this.dataTracker.get(color);
    }

    public void setCore(BlockState c) {
        this.dataTracker.set(core, c);
    }
    public BlockState getCore() {
        return this.dataTracker.get(core);
    }
    public BlockState getCoreForChild() {
        return Blocks.AIR.getDefaultState();
    }

    @Override
    protected ParticleEffect getParticles() {
        return new DustParticleEffect(this.getColorForRender(), 1.0f);
    }
    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return this.getCore().getSoundGroup().getHitSound();
    }
    @Override
    protected SoundEvent getDeathSound() {
        return this.getCore().getSoundGroup().getBreakSound();
    }
    @Override
    protected SoundEvent getSquishSound() {
        return this.getCore().getSoundGroup().getStepSound();
    }
    @Override
    protected SoundEvent getJumpSound() {
        return this.getCore().getSoundGroup().getFallSound();
    }
//    @Override
    // TODO
    public RegistryKey<LootTable> getLootTableId() {
        return this.getCore().getBlock().getLootTableKey().get();
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putInt("color", getColor());
        nbt.putString("core", Registries.BLOCK.getId(this.getCore().getBlock()).toString());
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.setColor(nbt.getInt("color"));
        Block b = Registries.BLOCK.get(Identifier.of(nbt.getString("core")));
        this.setCore(b.getDefaultState());
    }

    public static boolean canSpawn(EntityType<ChaosSlime> type, ServerWorldAccess world, SpawnReason spawnReason, BlockPos pos, net.minecraft.util.math.random.Random random) {
        if (world.getDifficulty() != Difficulty.PEACEFUL && InfinityMethods.chaosMobsEnabled()) {
            if (!(world instanceof StructureWorldAccess)) {
                return false;
            }
            return SlimeEntity.canMobSpawn(type, world, spawnReason, pos, random);
        }
        return false;
    }
}
