package net.lerariemann.infinity.entity.custom;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Dynamic;
import net.lerariemann.infinity.entity.ModEntities;
import net.lerariemann.infinity.iridescence.Iridescence;
import net.lerariemann.infinity.util.RandomProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.Sensor;
import net.minecraft.entity.ai.brain.sensor.SensorType;
import net.minecraft.entity.ai.brain.task.GoToRememberedPositionTask;
import net.minecraft.entity.ai.brain.task.LookAroundTask;
import net.minecraft.entity.ai.brain.task.MoveToTargetTask;
import net.minecraft.entity.ai.brain.task.PacifyTask;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.*;
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
import net.minecraft.util.math.random.Random;
import net.minecraft.world.*;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Supplier;

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
        return HostileEntity.createHostileAttributes().add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 5.0).add(EntityAttributes.GENERIC_FOLLOW_RANGE, 35.0);
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
        this.targetSelector.add(3, new ChaosCleanseGoal<>(this, ChaosSlime.class, true));
        this.targetSelector.add(3, new ChaosCleanseGoal<>(this, ChaosSkeleton.class, true));
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
        nbt.put("colors", getColors());
        nbt.putInt("case", dataTracker.get(special_case));
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.setColors(nbt.getCompound("colors"));
        this.dataTracker.set(special_case, nbt.getInt("case"));
    }

    @Override
    public RegistryKey<LootTable> getLootTableId() {
        Identifier i = switch (dataTracker.get(special_case)) {
            case 0 -> Identifier.of("infinity:entities/chaos_pawn_black");
            case 1 -> Identifier.of("infinity:entities/chaos_pawn_white");
            default -> Identifier.of("");
        };
        return RegistryKey.of(RegistryKeys.LOOT_TABLE, i);
    }

    public static NbtCompound getColorSetup(Supplier<Integer> colorSupplier) {
        NbtCompound c = new NbtCompound();
        Arrays.stream((new String[]{"body", "left_arm", "right_arm", "left_leg", "right_leg"})).forEach(
                s -> c.putInt(s, colorSupplier.get()));
        int head = colorSupplier.get();
        c.putInt("head", head);
        c.putInt("hat", 0xFFFFFF ^ head);
        return c;
    }

    public void setAllColors(int color) {
        this.setColors(getColorSetup(() -> color));
    }

    public boolean isChess() {
        return dataTracker.get(special_case) != -1;
    }

    public void setAllColors(Random r, BlockState state) {
        if (state.isOf(Blocks.WHITE_WOOL) || state.isOf(Blocks.WHITE_CONCRETE)) {
            dataTracker.set(special_case, 1);
            setAllColors(0xFFFFFF);
            return;
        }
        if (state.isOf(Blocks.BLACK_WOOL) || state.isOf(Blocks.BLACK_CONCRETE)) {
            dataTracker.set(special_case, 0);
            setAllColors(0);
            return;
        }
        this.randomizeColors(r);
    }
    
    public void unchess() {
        dataTracker.set(special_case, -1);
        randomizeColors(getRandom());
        playSound(SoundEvents.ENTITY_ZOMBIE_VILLAGER_CONVERTED, 1.0f, 1.0f);
    }

    public void randomizeColors(Random r) {
        setColors(getColorSetup(() -> r.nextInt(16777216)));
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
        Random r = getRandom();
        setAllColors(r, world.getBlockState(this.getBlockPos().down(2)));
        double i = r.nextDouble() * 40;
        Objects.requireNonNull(this.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH)).setBaseValue(i);
        this.setHealth((float)i);
        return super.initialize(world, difficulty, spawnReason, entityData);
    }

    @Override
    protected void mobTick() {
        this.tickAngerLogic((ServerWorld)this.getWorld(), false);
        super.mobTick();
    }

    public static boolean canSpawn(EntityType<ChaosPawn> type, ServerWorldAccess world, SpawnReason spawnReason, BlockPos pos, net.minecraft.util.math.random.Random random) {
        return world.getDifficulty() != Difficulty.PEACEFUL && ModEntities.chaosMobsEnabled(world);
    }

    @Override
    protected void dropEquipment(ServerWorld world, DamageSource source, boolean causedByPlayer) {
        super.dropEquipment(world, source, causedByPlayer);
        if (!this.isChess()) {
            String s = RandomProvider.getProvider(Objects.requireNonNull(world.getServer())).registry.get("items").getRandomElement(world.random);
            double i = Objects.requireNonNull(this.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH)).getBaseValue() / 10;
            this.dropStack(Registries.ITEM.get(Identifier.of(s)).getDefaultStack().copyWithCount((int)(i*i)));
        }
    }

    public static class ChaosCleanseGoal<T extends LivingEntity> extends ActiveTargetGoal<T> {
        public ChaosCleanseGoal(MobEntity mob, Class<T> targetClass, boolean checkVisibility) {
            super(mob, targetClass, checkVisibility);
        }

        @Override
        public boolean canStart() {
            if (mob instanceof ChaosPawn e && !e.isChess()) return false;
            return super.canStart();
        }
    }

    protected static final ImmutableList<? extends MemoryModuleType<?>> MEMORY_MODULE_TYPES = ImmutableList.of(
            MemoryModuleType.NEAREST_REPELLENT
    );

    protected static final ImmutableList<SensorType<? extends Sensor<? super ChaosPawn>>> SENSOR_TYPES = ImmutableList.of(
            ModEntities.PAWN_SENSOR.get()
    );

    @Override
    protected Brain.Profile<ChaosPawn> createBrainProfile() {
        return Brain.createProfile(MEMORY_MODULE_TYPES, SENSOR_TYPES);
    }

    @Override
    protected Brain<?> deserializeBrain(Dynamic<?> dynamic) {
        Brain<ChaosPawn> brain = this.createBrainProfile().deserialize(dynamic);
        brain.setTaskList(Activity.CORE, 0, ImmutableList.of(new LookAroundTask(45, 90), new MoveToTargetTask()));
        brain.setTaskList(Activity.IDLE, 10, ImmutableList.of(
                PacifyTask.create(MemoryModuleType.NEAREST_REPELLENT, 200),
                GoToRememberedPositionTask.createPosBased(MemoryModuleType.NEAREST_REPELLENT, 1.0F, 8, true)
        ));
        brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        brain.resetPossibleActivities();
        return brain;
    }

    @Override
    public float getPathfindingFavor(BlockPos pos, WorldView world) {
        if (isIridescenceAround(pos)) {
            return -1.0F;
        } else {
            return 0.0F;
        }
    }

    public boolean isIridescenceAround(BlockPos pos) {
        Optional<BlockPos> optional = getBrain().getOptionalRegisteredMemory(MemoryModuleType.NEAREST_REPELLENT);
        return optional.isPresent() && (optional.get()).isWithinDistance(pos, 8.0);
    }

    public static class PawnSensor extends Sensor<LivingEntity> {
        @Override
        public Set<MemoryModuleType<?>> getOutputMemoryModules() {
            return ImmutableSet.of(MemoryModuleType.NEAREST_REPELLENT);
        }

        @Override
        public void sense(ServerWorld world, LivingEntity entity) {
            Brain<?> brain = entity.getBrain();
            if (entity instanceof ChaosPawn pawn && pawn.isChess())
                brain.remember(MemoryModuleType.NEAREST_REPELLENT, findRepellent(world, entity));
        }

        public static Optional<BlockPos> findRepellent(ServerWorld world, LivingEntity entity) {
            return BlockPos.findClosest(entity.getBlockPos(), 8, 4,
                    pos -> Iridescence.isIridescence(world.getFluidState(pos)));
        }
    }
}
