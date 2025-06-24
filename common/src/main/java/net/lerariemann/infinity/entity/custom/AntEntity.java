package net.lerariemann.infinity.entity.custom;

import net.lerariemann.infinity.block.custom.AntBlock;
import net.lerariemann.infinity.util.core.NbtUtils;
import net.lerariemann.infinity.util.var.BishopBattle;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class AntEntity extends AbstractChessFigure {
    @Nullable
    protected BlockPos lastChangedPos;
    private Direction direction;
    private boolean dropsLoot;

    public AntEntity(EntityType<? extends AbstractChessFigure> entityType, World world) {
        super(entityType, world);
        direction = Direction.EAST;
        dropsLoot = true;
    }

    @Override
    public boolean isBlackOrWhite() {
        return true;
    }
    @Override
    public boolean shouldDropLoot() {
        return dropsLoot;
    }
    public void addToBattle(BishopBattle battle) {
        battle.addEntity(this);
        dropsLoot = false;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }
    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_SILVERFISH_HURT;
    }
    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_SILVERFISH_DEATH;
    }

    @Override
    protected void initGoals() {
        targetSelector.add(2, new AntBattleGoal<>(this, PlayerEntity.class, true));
        goalSelector.add(3, new AntBlockRecolorGoal(this));
        super.initGoals();
    }
    @Override
    protected void initRegularGoals() {
        goalSelector.add(0, new SwimGoal(this));
        goalSelector.add(5, new WanderConditionalGoal(this, 1.0));
        goalSelector.add(6, new LookAtEntityConditionalGoal(this, PlayerEntity.class, 6.0F));
        goalSelector.add(7, new LookAroundConditionalGoal(this));
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.MOVEMENT_SPEED, 0.1f)
                .add(EntityAttributes.MAX_HEALTH, 6);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.dropsLoot = NbtUtils.getBoolean(nbt, "dropsLoot", true);
        this.direction = switch(NbtUtils.getString(nbt, "direction")) {
            case "N" -> Direction.NORTH;
            case "W" -> Direction.WEST;
            case "S" -> Direction.SOUTH;
            default -> Direction.EAST;
        };
        if (nbt.contains("last_changed_pos")) {
            NbtCompound pos = NbtUtils.getCompound(nbt, "last_changed_pos");
            this.lastChangedPos = new BlockPos(NbtUtils.getInt(pos, "x"), NbtUtils.getInt(nbt,"y"), NbtUtils.getInt(nbt, "z"));
        }
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putBoolean("dropsLoot", dropsLoot);
        nbt.putString("direction", switch(this.direction) {
                    case Direction.NORTH -> "N";
                    case Direction.WEST -> "W";
                    case Direction.SOUTH -> "S";
                    default -> "E";
                });
        if (lastChangedPos != null) {
            NbtCompound pos = new NbtCompound();
            pos.putInt("x", lastChangedPos.getX());
            pos.putInt("y", lastChangedPos.getY());
            pos.putInt("z", lastChangedPos.getZ());
            nbt.put("last_changed_pos", pos);
        }
    }

    @Override
    public boolean shouldPursueRegularGoals() {
        World w = getWorld();
        Optional<BlockPos> bp = supportingBlockPos;
        return (bp.isEmpty()
                || isInBattle()
                || !AntBlock.isSafeToRecolor(w, bp.get()))
                && super.shouldPursueRegularGoals();
    }

    @Override
    public boolean canBeLeashed() {
        return !isInBattle();
    }
    @Override
    public boolean canWalkOnFluid(FluidState state) {
        return state.isIn(FluidTags.WATER);
    }

    public static VoxelShape getWaterCollisionShape(int level) {
        return Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, level, 16.0);
    }

    @Override
    public void tickMovement() {
        super.tickMovement();
        if (!this.firstUpdate && this.fluidHeight.getDouble(FluidTags.WATER) > 0.0) {
            ShapeContext shapeContext = ShapeContext.of(this);
            if (shapeContext.isAbove(getWaterCollisionShape(15),
                    this.getBlockPos(), true)
                    && !this.getWorld().getFluidState(this.getBlockPos().up()).isIn(FluidTags.WATER)) {
                this.setOnGround(true);
            } else {
                this.setVelocity(this.getVelocity().multiply(0.5).add(0.0, 0.05, 0.0));
            }
        }
    }

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        if (!this.hasPassengers()
                && this.getAttributeValue(EntityAttributes.SCALE) > 2
                && player.getStackInHand(hand).isEmpty()) {
            this.putPlayerOnBack(player);
            return ActionResult.SUCCESS;
        }
        return super.interactMob(player, hand);
    }
    protected void putPlayerOnBack(PlayerEntity player) {
        if (!this.getWorld().isClient) {
            player.setYaw(this.getYaw());
            player.setPitch(this.getPitch());
            player.startRiding(this);
        }
    }
    @Override
    protected Vec3d getControlledMovementInput(PlayerEntity controllingPlayer, Vec3d movementInput) {
        float f = controllingPlayer.forwardSpeed;
        return new Vec3d(controllingPlayer.sidewaysSpeed * 0.5f, 0, f < 0 ? f*0.25f : f);
    }
    @Override
    protected float getSaddledSpeed(PlayerEntity controllingPlayer) {
        return (float)this.getAttributeValue(EntityAttributes.MOVEMENT_SPEED) * 0.8f;
    }
    @Nullable
    @Override
    public LivingEntity getControllingPassenger() {
        if (this.getFirstPassenger() instanceof PlayerEntity player) {
            return player;
        }
        return super.getControllingPassenger();
    }
    @Override
    protected void updatePassengerPosition(Entity passenger, Entity.PositionUpdater positionUpdater) {
        super.updatePassengerPosition(passenger, positionUpdater);
        if (passenger instanceof LivingEntity) {
            ((LivingEntity)passenger).bodyYaw = this.bodyYaw;
        }
    }
    @Override
    protected void tickControlled(PlayerEntity controllingPlayer, Vec3d movementInput) {
        super.tickControlled(controllingPlayer, movementInput);
        this.setRotation(controllingPlayer.getYaw(),controllingPlayer.getPitch() * 0.5F);
        this.prevYaw = this.bodyYaw = this.headYaw = this.getYaw();
    }

    public static class AntBattleGoal<T extends LivingEntity> extends ActiveTargetGoal<T> {
        public AntBattleGoal(MobEntity mob, Class<T> targetClass, boolean checkVisibility) {
            super(mob, targetClass, checkVisibility);
        }

        @Override
        public boolean canStart() {
            if (mob instanceof AbstractChessFigure e && !e.isInBattle()) return false;
            return super.canStart();
        }
    }

    public static class AntBlockRecolorGoal extends Goal {
        private final AntEntity mob;
        @Nullable
        private BlockPos targetPos;

        public AntBlockRecolorGoal(AntEntity mob) {
            this.mob = mob;
        }

        @Override
        public boolean canStart() {
            if (mob.shouldPursueRegularGoals()) return false;
            Optional<BlockPos> bp = mob.supportingBlockPos;
            if (bp.isEmpty() || bp.get().equals(mob.lastChangedPos)) return false;
            mob.lastChangedPos = bp.get();

            BlockState down = mob.getWorld().getBlockState(bp.get());
            AntBlock.Clockwiseness cw = AntBlock.getCW(down.getBlock());
            if (cw == null) return false;

            Direction direction = mob.direction;
            Direction direction2 = cw.equals(AntBlock.Clockwiseness.CW)
                    ? direction.rotateYClockwise()
                    : direction.rotateYCounterclockwise();

            Block newBlock = AntBlock.recolor(down.getBlock(), cw.equals(AntBlock.Clockwiseness.CCW));
            if (newBlock == null) return false;

            targetPos = mob.getBlockPos().offset(direction2);
            mob.getWorld().setBlockState(bp.get(), newBlock.getStateWithProperties(down), 19);
            mob.direction = direction2;
            return true;
        }

        @Override
        public boolean shouldContinue() {
            return false;
        }

        @Override
        public void start() {
            if (targetPos == null) return;
            Vec3d v = targetPos.toBottomCenterPos();
            mob.teleport(v.x, v.y, v.z, false);
            mob.setYaw(mob.direction.asRotation());
        }
    }

    public static class WanderConditionalGoal extends WanderAroundFarGoal {
        private final AntEntity mob1;
        public WanderConditionalGoal(AntEntity pathAwareEntity, double d) {
            super(pathAwareEntity, d);
            mob1 = pathAwareEntity;
        }

        @Override
        public boolean canStart() {
            return super.canStart() && mob1.shouldPursueRegularGoals();
        }
    }

    public static class LookAroundConditionalGoal extends LookAroundGoal {
        private final AntEntity mob1;
        public LookAroundConditionalGoal(AntEntity mob) {
            super(mob);
            mob1 = mob;
        }

        @Override
        public boolean canStart() {
            return super.canStart() && mob1.shouldPursueRegularGoals();
        }
    }

    public static class LookAtEntityConditionalGoal extends LookAtEntityGoal {
        private final AntEntity mob1;

        public LookAtEntityConditionalGoal(AntEntity mob, Class<? extends LivingEntity> targetType, float range) {
            super(mob, targetType, range);
            mob1 = mob;
        }

        @Override
        public boolean canStart() {
            return super.canStart() && mob1.shouldPursueRegularGoals();
        }
    }
}
