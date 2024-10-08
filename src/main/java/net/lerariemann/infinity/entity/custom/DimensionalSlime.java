package net.lerariemann.infinity.entity.custom;

import net.lerariemann.infinity.access.MinecraftServerAccess;
import net.lerariemann.infinity.dimensions.RandomProvider;
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
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.world.*;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Random;

public class DimensionalSlime extends SlimeEntity implements TintableEntity {
    public static final TrackedData<BlockState> core = DataTracker.registerData(DimensionalSlime.class, TrackedDataHandlerRegistry.BLOCK_STATE);
    public static final TrackedData<Integer> color = DataTracker.registerData(DimensionalSlime.class, TrackedDataHandlerRegistry.INTEGER);

    public DimensionalSlime(EntityType<? extends DimensionalSlime> entityType, World world) {
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
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(core, Blocks.STONE.getDefaultState());
        builder.add(color, 0);
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return HostileEntity.createHostileAttributes().add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.2f);
    }

    @Override
    @Nullable
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData) {
        RandomProvider p = ((MinecraftServerAccess)(Objects.requireNonNull(world.getServer()))).projectInfinity$getDimensionProvider();
        Random r = new Random();
        this.dataTracker.set(core, Registries.BLOCK.get(Identifier.of(p.randomName(r, "all_blocks"))).getDefaultState());
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
    public void setCore(BlockState c) {
        this.dataTracker.set(core, c);
    }
    @Override
    public int getColorForRender() {
        int v = getColorNamed();
        if (v!=-1) return v;
        return ColorHelper.Argb.fullAlpha(this.dataTracker.get(color));
    }

    public BlockState getCore() {
        return this.dataTracker.get(core);
    }

    public BlockState getCoreForChild() {
        return Blocks.AIR.getDefaultState();
    }

    @Override
    protected ParticleEffect getParticles() {
        return new DustParticleEffect(colorFromInt(this.getColorForRender()), 1.0f);
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
    @Override
    public RegistryKey<LootTable> getLootTableId() {
        return this.getCore().getBlock().getLootTableKey();
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putInt("color", this.dataTracker.get(color));
        nbt.putString("core", Registries.BLOCK.getId(this.getCore().getBlock()).toString());
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.setColor(nbt.getInt("color"));
        Block b = Registries.BLOCK.get(Identifier.of(nbt.getString("core")));
        this.setCore(b.getDefaultState());
    }

    public static boolean canSpawn(EntityType<DimensionalSlime> type, ServerWorldAccess world, SpawnReason spawnReason, BlockPos pos, net.minecraft.util.math.random.Random random) {
        if (world.getDifficulty() != Difficulty.PEACEFUL && ((MinecraftServerAccess)world.toServerWorld().getServer()).projectInfinity$getDimensionProvider().rule("chaosMobsEnabled")) {
            if (!(world instanceof StructureWorldAccess)) {
                return false;
            }
            return SlimeEntity.canMobSpawn(type, world, spawnReason, pos, random);
        }
        return false;
    }
}
