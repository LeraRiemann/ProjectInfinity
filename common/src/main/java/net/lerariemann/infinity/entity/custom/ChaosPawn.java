package net.lerariemann.infinity.entity.custom;

import net.lerariemann.infinity.access.MobEntityAccess;
import net.lerariemann.infinity.util.RandomProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.Angerable;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.loot.LootTable;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.TimeHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.*;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;

public class ChaosPawn extends HostileEntity implements Angerable {
    public static final TrackedData<NbtCompound> colors = DataTracker.registerData(ChaosPawn.class, TrackedDataHandlerRegistry.NBT_COMPOUND);
    public static final TrackedData<Integer> special_case = DataTracker.registerData(ChaosPawn.class, TrackedDataHandlerRegistry.INTEGER);
    private int angerTime;
    @Nullable
    private UUID angryAt;

    public ChaosPawn(EntityType<? extends ChaosPawn> entityType, World world) {
        super(entityType, world);
    }
    public void setColors(NbtCompound i) {
        this.dataTracker.set(colors, i);
    }
    public NbtCompound getColors() {
        return this.dataTracker.get(colors);
    }

    @Override
    public void setAngerTime(int angerTime) {
        this.angerTime = angerTime;
    }
    @Override
    public int getAngerTime() {
        return this.angerTime;
    }
    @Override
    public void setAngryAt(@Nullable UUID angryAt) {
        this.angryAt = angryAt;
    }
    @Override
    @Nullable
    public UUID getAngryAt() {
        return this.angryAt;
    }
    @Override
    public void chooseRandomAngerTime() {
        this.setAngerTime(TimeHelper.betweenSeconds(20, 40).get(this.random));
    }
    @Override
    public boolean hasCustomName() {
        return super.hasCustomName();
    }
    @Override
    public Text getName() {
        return super.getName();
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return HostileEntity.createHostileAttributes().add(EntityAttributes.ATTACK_DAMAGE, 5.0).add(EntityAttributes.FOLLOW_RANGE, 35.0);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(colors, new NbtCompound());
        builder.add(special_case, -1);
    }
    @Override
    protected void initGoals() {
        this.goalSelector.add(0, new SwimGoal(this));
        this.targetSelector.add(1, new RevengeGoal(this).setGroupRevenge());
        this.goalSelector.add(2, new MeleeAttackGoal(this, 1.0, false));
        this.targetSelector.add(3, new ActiveTargetGoal<>(this, DimensionalSlime.class, true));
        this.targetSelector.add(3, new UniversalAngerGoal<>(this, true));
        this.goalSelector.add(5, new EatGrassGoal(this));
        this.goalSelector.add(7, new WanderAroundFarGoal(this, 1.0));
        this.goalSelector.add(8, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));
        this.goalSelector.add(8, new LookAroundGoal(this));
    }

    @Override
    public void onEatingGrass() {
        super.onEatingGrass();
        this.setAllColors(this.getWorld().getBiome(this.getBlockPos()).value().getGrassColorAt(this.getX(), this.getZ()));
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.put("colors", this.getColors());
        nbt.putInt("case", this.dataTracker.get(special_case));
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.setColors(nbt.getCompound("colors"));
        this.dataTracker.set(special_case, nbt.getInt("case"));
    }

    @Override
    public RegistryKey<LootTable> getLootTableKey() {
        Identifier i = switch (this.dataTracker.get(special_case)) {
            case 0 -> Identifier.of("infinity:entities/chaos_pawn_black");
            case 1 -> Identifier.of("infinity:entities/chaos_pawn_white");
            default -> Identifier.of("");
        };
        return RegistryKey.of(RegistryKeys.LOOT_TABLE, i);
    }

    public void setAllColors(int color) {
        NbtCompound c = new NbtCompound();
        Arrays.stream((new String[]{"head", "body", "left_arm", "right_arm", "left_leg", "right_leg"})).forEach(s -> c.putInt(s, color));
        c.putInt("hat", 0xFFFFFF ^ color);
        this.setColors(c);
    }

    public void setAllColors(Random r, BlockState state) {
        if (state.isOf(Blocks.WHITE_WOOL)) {
            this.dataTracker.set(special_case, 1);
            setAllColors(16777215);
            return;
        }
        if (state.isOf(Blocks.BLACK_WOOL)) {
            this.dataTracker.set(special_case, 0);
            setAllColors(0);
            return;
        }
        NbtCompound c = new NbtCompound();
        int i = r.nextInt(16777216);
        Arrays.stream((new String[]{"body", "left_arm", "right_arm", "left_leg", "right_leg"})).forEach(s -> c.putInt(s, r.nextInt(16777216)));
        c.putInt("head", i);
        c.putInt("hat", i ^ 0xFFFFFF);
        this.setColors(c);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENTITY_PLAYER_BREATH;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_PLAYER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_PLAYER_DEATH;
    }

    @Override
    @Nullable
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData) {
        Random r = new Random();
        setAllColors(r, world.getBlockState(this.getBlockPos().down(2)));
        double i = 15*r.nextExponential();
        Objects.requireNonNull(this.getAttributeInstance(EntityAttributes.MAX_HEALTH)).setBaseValue(i);
        this.setHealth((float)i);
        int a;
        if ((a = (int)(0.1*i)) > 0) {
            this.equipLootStack(EquipmentSlot.HEAD, Registries.ITEM.get(Identifier.of(
                    RandomProvider.getProvider(Objects.requireNonNull(world.getServer())).randomName(r, "items")))
                    .getDefaultStack().copyWithCount(a));
            ((MobEntityAccess)this).projectInfinity$setPersistent(false);
        }
        return super.initialize(world, difficulty, spawnReason, entityData);
    }

    @Override
    protected void mobTick(ServerWorld world) {
        this.tickAngerLogic(world, false);
        super.mobTick(world);
    }

    public static boolean canSpawn(EntityType<ChaosPawn> type, ServerWorldAccess world, SpawnReason spawnReason, BlockPos pos, net.minecraft.util.math.random.Random random) {
        return world.getDifficulty() != Difficulty.PEACEFUL &&
                RandomProvider.getProvider(world.toServerWorld().getServer()).rule("chaosMobsEnabled");
    }
}
