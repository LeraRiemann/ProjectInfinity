package net.lerariemann.infinity.entity.custom;

import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.iridescence.Iridescence;
import net.lerariemann.infinity.util.InfinityMethods;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.Angerable;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.TimeHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
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
        return HostileEntity.createHostileAttributes().add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 5.0)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 35.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.6F);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(colors, new NbtCompound());
        this.dataTracker.startTracking(special_case, -1);
    }
    @Override
    protected void initGoals() {
        this.goalSelector.add(0, new SwimGoal(this));
        this.targetSelector.add(1, new PawnRevengeGoal(this).setGroupRevenge());
        this.goalSelector.add(2, new MeleeAttackGoal(this, 1.0, false));
        this.targetSelector.add(3, new ChaosCleanseGoal<>(this, ChaosSlime.class, true));
        this.targetSelector.add(3, new ChaosCleanseGoal<>(this, ChaosSkeleton.class, true));
        this.targetSelector.add(3, new PawnUniversalAngerGoal(this));
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

    public int getCase() {
        return dataTracker.get(special_case);
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.put("colors", getColors());
        nbt.putInt("case", getCase());
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.setColors(nbt.getCompound("colors"));
        this.dataTracker.set(special_case, nbt.getInt("case"));
    }

    @Override
    public Identifier getLootTableId() {
        return switch (dataTracker.get(special_case)) {
            case 0 -> Identifier.tryParse("infinity:entities/chaos_pawn_black");
            case 1 -> Identifier.tryParse("infinity:entities/chaos_pawn_white");
            default -> Identifier.tryParse("");
        };
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
        return dataTracker.get(special_case) != -1 && !Iridescence.isUnderEffect(this);
    }

    public void setAllColors(Random r, BlockState state) {
        if (state.isOf(Blocks.WHITE_WOOL) || state.isOf(Blocks.WHITE_CONCRETE)) {
            chess(true);
        }
        else if (state.isOf(Blocks.BLACK_WOOL) || state.isOf(Blocks.BLACK_CONCRETE)) {
            chess(false);
        }
        else this.randomizeColors(r);
    }

    public void chess(boolean white) {
        dataTracker.set(special_case, white ? 1 : 0);
        setAllColors(white ? 0xFFFFFF : 0);
    }

    public void unchess() {
        dataTracker.set(special_case, -1);
        randomizeColors(new Random());
        playSound(SoundEvents.ENTITY_ZOMBIE_VILLAGER_CONVERTED, 1.0f, 1.0f);
    }

    public void randomizeColors(Random r) {
        setColors(getColorSetup(() -> r.nextInt(16777216)));
    }

    @Override
    public boolean canPickupItem(ItemStack stack) {
        return true;
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
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData, @Nullable NbtCompound entityNbt) {
        Random r = new Random();
        setAllColors(r, world.getBlockState(this.getBlockPos().down()));
        double i = r.nextDouble() * 40;
        Objects.requireNonNull(this.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH)).setBaseValue(i);
        this.setHealth((float)i);
        int a;
        if ((a = (int)(0.1*i)) > 0) {
            this.equipLootStack(EquipmentSlot.HEAD, Registries.ITEM.get(new Identifier(
                            ((MinecraftServerAccess)Objects.requireNonNull(world.getServer())).infinity$getDimensionProvider().randomName(r, "items")))
                    .getDefaultStack().copyWithCount(a));
            ((MobEntityAccess)this).infinity$setPersistent(false);
        }
        return super.initialize(world, difficulty, spawnReason, entityData, entityNbt);
    }

    @Override
    protected void mobTick() {
        this.tickAngerLogic((ServerWorld)this.getWorld(), false);
        super.mobTick();
    }

    public static boolean canSpawn(EntityType<ChaosPawn> type, ServerWorldAccess world, SpawnReason spawnReason, BlockPos pos, net.minecraft.util.math.random.Random random) {
        return world.getDifficulty() != Difficulty.PEACEFUL && InfinityMethods.chaosMobsEnabled();
    }

    @Override
    protected void dropEquipment(DamageSource source, int lootingMultiplier, boolean allowDrops) {
        super.dropEquipment(source, lootingMultiplier, allowDrops);
        if (!this.isChess()) {
            String s = InfinityMod.provider.registry.get("items").getRandomElement(world.random);
            double i = Objects.requireNonNull(this.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH)).getBaseValue() / 10;
            ItemStack stack = Registries.ITEM.get(Identifier.of(s)).getDefaultStack().copyWithCount((int)(i*i));
            stack.applyComponentsFrom(ComponentMap.builder().add(DataComponentTypes.MAX_STACK_SIZE, 64).build());
            this.dropStack(stack);
        }
    }

    @Override
    public float getPathfindingFavor(BlockPos pos, WorldView world) {
        if (!isChess()) return 0.0f;
        if (Iridescence.isIridescence(world, pos)) return -1.0F;
        return 0.0f;
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

    public static class PawnUniversalAngerGoal extends Goal {
        private final ChaosPawn mob;
        private int lastAttackedTime;

        public PawnUniversalAngerGoal(ChaosPawn mob) {
            this.mob = mob;
        }

        @Override
        public boolean canStart() {
            return this.mob.getWorld().getGameRules().getBoolean(GameRules.UNIVERSAL_ANGER) && this.canStartUniversalAnger();
        }

        private boolean canStartUniversalAnger() {
            return this.mob.getAttacker() != null
                    && this.mob.getAttacker().getType() == EntityType.PLAYER
                    && this.mob.getLastAttackedTime() > this.lastAttackedTime
                    && !Iridescence.isUnderEffect(this.mob);
        }

        @Override
        public void start() {
            this.lastAttackedTime = this.mob.getLastAttackedTime();
            this.mob.universallyAnger();
            this.getOthersInRange().stream().filter(entity -> {
                if (entity == mob) return false;
                if (Iridescence.isUnderEffect(entity)) return false;
                if (entity instanceof ChaosPawn) {
                    return mob.getCase() == entity.getCase();
                }
                return true;
            }).map(entity -> (Angerable)entity).forEach(Angerable::universallyAnger);
            super.start();
        }

        private List<? extends ChaosPawn> getOthersInRange() {
            double d = this.mob.getAttributeValue(EntityAttributes.GENERIC_FOLLOW_RANGE);
            Box box = Box.from(this.mob.getPos()).expand(d, 10.0, d);
            return this.mob.getWorld().getEntitiesByClass(this.mob.getClass(), box, EntityPredicates.EXCEPT_SPECTATOR);
        }
    }

    public static class PawnRevengeGoal extends RevengeGoal {
        public PawnRevengeGoal(PathAwareEntity mob, Class<?>... noRevengeTypes) {
            super(mob, noRevengeTypes);
        }

        @Override
        protected void callSameTypeForRevenge() {
            if (mob instanceof ChaosPawn pawn && !Iridescence.isUnderEffect(pawn)) {
                double d = this.getFollowRange();
                Box box = Box.from(pawn.getPos()).expand(d, 10.0, d);
                List<ChaosPawn> list = pawn.getWorld().getEntitiesByClass(ChaosPawn.class, box, EntityPredicates.EXCEPT_SPECTATOR);
                for (ChaosPawn pawn2 : list) {
                    if (pawn != pawn2
                            && pawn2.getTarget() == null
                            && !pawn2.isTeammate(pawn.getAttacker())
                            && !Iridescence.isUnderEffect(pawn2)
                            && pawn2.getCase() == pawn.getCase())
                        this.setMobEntityTarget(pawn2, pawn.getAttacker());
                }
            }
        }
    }
}