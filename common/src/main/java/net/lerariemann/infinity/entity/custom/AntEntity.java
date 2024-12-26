package net.lerariemann.infinity.entity.custom;

import net.lerariemann.infinity.block.custom.AntBlock;
import net.lerariemann.infinity.registry.core.ModEntities;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.Angerable;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public class AntEntity extends AnimalEntity implements Angerable {
    private int angerTime;
    @Nullable
    private UUID angryAt;
    @Nullable
    protected BlockPos lastChangedPos;
    private Direction direction;

    public AntEntity(EntityType<? extends AnimalEntity> entityType, World world) {
        super(entityType, world);
        direction = Direction.EAST;
    }

    @Override
    protected void initGoals() {
        super.initGoals();
        this.goalSelector.add(0, new SwimGoal(this));
        this.goalSelector.add(1, new MeleeAttackGoal(this, 1.25, true));
        this.targetSelector.add(1, new RevengeGoal(this));
        this.targetSelector.add(2, new AntBattleGoal<>(this, PlayerEntity.class, true));
        this.goalSelector.add(3, new AntBlockRecolorGoal(this));
        this.goalSelector.add(4, new FollowParentConditionalGoal(this, 1.25));
        this.targetSelector.add(5, new UniversalAngerGoal<>(this, true));
        this.goalSelector.add(5, new WanderConditionalGoal(this, 1.0));
        this.goalSelector.add(6, new LookAtEntityConditionalGoal(this, PlayerEntity.class, 6.0F));
        this.goalSelector.add(7, new LookAroundConditionalGoal(this));
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.1f)
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 6);
    }

    public boolean isInBattle() {
        Team t = getScoreboardTeam();
        return (t != null && t.getName().equals("ant_battle"));
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.readAngerFromNbt(this.getWorld(), nbt);
        this.direction = switch(nbt.getString("direction")) {
            case "N" -> Direction.NORTH;
            case "W" -> Direction.WEST;
            case "S" -> Direction.SOUTH;
            default -> Direction.EAST;
        };
        if (nbt.contains("last_changed_pos")) {
            NbtCompound pos = nbt.getCompound("last_changed_pos");
            this.lastChangedPos = new BlockPos(pos.getInt("x"), pos.getInt("y"), pos.getInt("z"));
        }
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        this.writeAngerToNbt(nbt);
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
    public boolean isBreedingItem(ItemStack stack) {
        return false;
    }

    @Override
    public @Nullable PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
        return ModEntities.ANT.get().create(world);
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
    public void chooseRandomAngerTime() {
        this.setAngerTime(200);
    }

    @Nullable
    @Override
    public UUID getAngryAt() {
        return this.angryAt;
    }

    public boolean shouldPursueRegularGoals() {
        World w = getWorld();
        Optional<BlockPos> bp = supportingBlockPos;
        return (w == null
                || bp.isEmpty()
                || isInBattle()
                || !AntBlock.inverseExists(w.getBlockState(bp.get()).getBlock()));
    }

    public static class AntBattleGoal<T extends LivingEntity> extends ActiveTargetGoal<T> {
        public AntBattleGoal(MobEntity mob, Class<T> targetClass, boolean checkVisibility) {
            super(mob, targetClass, checkVisibility);
        }

        @Override
        public boolean canStart() {
            if (mob instanceof AntEntity e && !e.isInBattle()) return false;
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

    public static class FollowParentConditionalGoal extends FollowParentGoal {
        private final AntEntity mob;

        public FollowParentConditionalGoal(AntEntity animal, double speed) {
            super(animal, speed);
            this.mob = animal;
        }

        @Override
        public boolean canStart() {
            return super.canStart() && mob.shouldPursueRegularGoals();
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
