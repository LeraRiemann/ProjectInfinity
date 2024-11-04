package net.lerariemann.infinity.entity.custom;

import net.lerariemann.infinity.entity.ModEntities;
import net.lerariemann.infinity.util.RandomProvider;
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
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.*;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.Objects;
import java.util.Random;

public class ChaosSlime extends SlimeEntity implements TintableEntity {
    public static final TrackedData<BlockState> core = DataTracker.registerData(ChaosSlime.class, TrackedDataHandlerRegistry.BLOCK_STATE);
    public static final TrackedData<Vector3f> color = DataTracker.registerData(ChaosSlime.class, TrackedDataHandlerRegistry.VECTOR3F);

    public ChaosSlime(EntityType<? extends ChaosSlime> entityType, World world) {
        super(entityType, world);
    }
    @Override
    public int getAge() {
        return age;
    }


    @Override
    public boolean hasCustomName() {
        return super.hasCustomName();
    }

    @Override
    public Text getName() {
        return super.getName();
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(core, Blocks.STONE.getDefaultState());
        this.dataTracker.startTracking(color, new Vector3f(0.0f, 0.0f, 0.0f));
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return HostileEntity.createHostileAttributes().add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.2f);
    }

    @Override
    @Nullable
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData, @Nullable NbtCompound entityNbt) {
        RandomProvider p = RandomProvider.getProvider(Objects.requireNonNull(world.getServer()));
        Random r = new Random();
        this.dataTracker.set(core, Registries.BLOCK.get(new Identifier(p.randomName(r, "all_blocks"))).getDefaultState());
        Vector3f c = new Vector3f(r.nextFloat(), r.nextFloat(), r.nextFloat());
        this.dataTracker.set(color, c);
        return super.initialize(world, difficulty, spawnReason, entityData, entityNbt);
    }

    @Override
    public boolean canSpawn(WorldView world) {
        return world.doesNotIntersectEntities(this);
    }

    public void setColor(Vector3f c) {
        this.dataTracker.set(color, c);
    }
    public void setCore(BlockState c) {
        this.dataTracker.set(core, c);
    }
    @Override
    public Vector3f getColor() {
        Vector3f v = getColorNamed();
        if (v!=null) return v;
        return this.dataTracker.get(color);
    }
    @Override
    public float getAlpha() {return 1.0f;}

    public BlockState getCore() {
        return this.dataTracker.get(core);
    }

    public BlockState getCoreForChild() {
        return Blocks.AIR.getDefaultState();
    }

    @Override
    protected ParticleEffect getParticles() {
        return new DustParticleEffect(this.getColor(), 1.0f);
    }
    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return this.getCore().getBlock().getSoundGroup(this.getCore()).getHitSound();
    }
    @Override
    protected SoundEvent getDeathSound() {
        return this.getCore().getBlock().getSoundGroup(this.getCore()).getBreakSound();
    }
    @Override
    protected SoundEvent getSquishSound() {
        return this.getCore().getBlock().getSoundGroup(this.getCore()).getStepSound();
    }
    @Override
    protected SoundEvent getJumpSound() {
        return this.getCore().getBlock().getSoundGroup(this.getCore()).getFallSound();
    }
    @Override
    public Identifier getLootTableId() {
        return this.getCore().getBlock().getLootTableId();
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putFloat("red", this.dataTracker.get(color).x);
        nbt.putFloat("green", this.dataTracker.get(color).y);
        nbt.putFloat("blue", this.dataTracker.get(color).z);
        nbt.putString("core", Registries.BLOCK.getId(this.getCore().getBlock()).toString());
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.setColor(new Vector3f(nbt.getFloat("red"), nbt.getFloat("green"), nbt.getFloat("blue")));
        Block b = Registries.BLOCK.get(new Identifier(nbt.getString("core")));
        this.setCore(b.getDefaultState());
    }

    public static boolean canSpawn(EntityType<ChaosSlime> type, ServerWorldAccess world, SpawnReason spawnReason, BlockPos pos, net.minecraft.util.math.random.Random random) {
        if (world.getDifficulty() != Difficulty.PEACEFUL && ModEntities.chaosMobsEnabled(world)) {
            if (!(world instanceof StructureWorldAccess)) {
                return false;
            }
            return SlimeEntity.canMobSpawn(type, world, spawnReason, pos, random);
        }
        return false;
    }
}
