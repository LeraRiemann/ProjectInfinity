package net.lerariemann.infinity.entity.custom;

import net.lerariemann.access.MobEntityAccess;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.world.*;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Random;

public class ChaosPawn extends HostileEntity {
    public static final TrackedData<NbtCompound> colors = DataTracker.registerData(ChaosPawn.class, TrackedDataHandlerRegistry.NBT_COMPOUND);
    public static final TrackedData<Integer> special_case = DataTracker.registerData(ChaosPawn.class, TrackedDataHandlerRegistry.INTEGER);

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
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(colors, new NbtCompound());
        this.dataTracker.startTracking(special_case, -1);
    }
    @Override
    protected void initGoals() {
        this.goalSelector.add(0, new SwimGoal(this));
        this.goalSelector.add(2, new MeleeAttackGoal(this, 1.0, false));
        this.targetSelector.add(3, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
        this.goalSelector.add(5, new EatGrassGoal(this));
        this.goalSelector.add(7, new WanderAroundFarGoal(this, 1.0));
        this.goalSelector.add(8, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));
        this.goalSelector.add(8, new LookAroundGoal(this));
    }

    @Override
    public void onEatingGrass() {
        super.onEatingGrass();
        this.setAllColors(new Random(), this.getWorld().getBiome(this.getBlockPos()).value().getGrassColorAt(this.getX(), this.getZ()));
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
    public Identifier getLootTableId() {
        return switch (this.dataTracker.get(special_case)) {
            case 0 -> new Identifier("infinity:entities/chaos_pawn_black.json");
            case 1 -> new Identifier("infinity:entities/chaos_pawn_white.json");
            default -> new Identifier("");
        };
    }

    public void setAllColors(Random r, int color) {
        NbtCompound c = new NbtCompound();
        Arrays.stream((new String[]{"head", "body", "left_arm", "right_arm", "left_leg", "right_leg"})).forEach(s -> c.putInt(s, color));
        c.putInt("hat", r.nextInt(16777216));
        this.setColors(c);
    }

    public void setAllColors(Random r, BlockState state) {
        if (state.isOf(Blocks.WHITE_WOOL)) {
            this.dataTracker.set(special_case, 1);
            setAllColors(r, 16777215);
            return;
        }
        if (state.isOf(Blocks.BLACK_WOOL)) {
            this.dataTracker.set(special_case, 0);
            setAllColors(r, 0);
            return;
        }
        NbtCompound c = new NbtCompound();
        Arrays.stream((new String[]{"head", "body", "hat", "left_arm", "right_arm", "left_leg", "right_leg"})).forEach(s -> c.putInt(s, r.nextInt(16777216)));
        this.setColors(c);
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
        setAllColors(r, world.getBlockState(this.getBlockPos().down(2)));
        if (this.dataTracker.get(special_case) < 0) {
            this.equipLootStack(EquipmentSlot.FEET, Registries.ITEM.get(r.nextInt(Registries.ITEM.size())).getDefaultStack());
            ((MobEntityAccess)this).setPersistent(false);
        }
        return super.initialize(world, difficulty, spawnReason, entityData, entityNbt);
    }
}
