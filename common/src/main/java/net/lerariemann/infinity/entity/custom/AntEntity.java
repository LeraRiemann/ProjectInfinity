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
import net.minecraft.entity.mob.PathAwareEntity;
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
import org.apache.logging.log4j.LogManager;
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
        this.goalSelector.add(1, new MeleeAttackGoal(this, 2, true));
        this.targetSelector.add(1, new RevengeGoal(this));
        this.targetSelector.add(2, new ActiveTargetGoal<>(this, PlayerEntity.class, 10,
                true, false, this::shouldAttackFirst));
        this.goalSelector.add(3, new AntGoal(this));
        this.goalSelector.add(4, new FollowParentConditionalGoal(this, 1.25));
        this.targetSelector.add(5, new UniversalAngerGoal<>(this, false));
        this.goalSelector.add(5, new WanderConditionalGoal(this, 1.0));
        this.goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 6.0F));
        this.goalSelector.add(7, new LookAroundGoal(this));
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.1f)
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 6);
    }

    public boolean isInBattle() {
        Team t = getScoreboardTeam();
        return (t != null && t.getName().equals("battle"));
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

    public boolean shouldAttackFirst(LivingEntity entity) {
        return isInBattle() && shouldAngerAt(entity);
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

    public static boolean isOnRecolorable(LivingEntity mob) {
        World w = mob.getWorld();
        Optional<BlockPos> bp = mob.supportingBlockPos;
        return (w != null && bp.isPresent() && AntBlock.inverseExists(w.getBlockState(bp.get()).getBlock()));
    }

    public static class AntGoal extends Goal {
        private final AntEntity mob;
        @Nullable
        private BlockPos targetPos;

        public AntGoal(AntEntity mob) {
            this.mob = mob;
        }

        @Override
        public boolean canStart() {
            if (mob.isInBattle()) return false;
            Optional<BlockPos> bp = mob.supportingBlockPos;
            if (bp.isEmpty() || bp.get().equals(mob.lastChangedPos)) return false;
            LogManager.getLogger().info("boop 1");
            mob.lastChangedPos = bp.get();

            BlockState down = mob.getWorld().getBlockState(bp.get());
            AntBlock.Clockwiseness cw = AntBlock.getCW(down.getBlock());
            if (cw == null) return false;
            LogManager.getLogger().info("boop 2");

            Direction direction = mob.direction;
            Direction direction2 = cw.equals(AntBlock.Clockwiseness.CW)
                    ? direction.rotateYClockwise()
                    : direction.rotateYCounterclockwise();

            Block newBlock = AntBlock.recolor(down.getBlock(), cw.equals(AntBlock.Clockwiseness.CCW));
            if (newBlock == null) return false;

            targetPos = mob.getBlockPos().offset(direction2);
            LogManager.getLogger().info("boop 4");
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
        private final AnimalEntity mob;

        public FollowParentConditionalGoal(AnimalEntity animal, double speed) {
            super(animal, speed);
            this.mob = animal;
        }

        @Override
        public boolean canStart() {
            return super.canStart() && !isOnRecolorable(mob);
        }
    }

    public static class WanderConditionalGoal extends WanderAroundFarGoal {
        public WanderConditionalGoal(PathAwareEntity pathAwareEntity, double d) {
            super(pathAwareEntity, d);
        }

        @Override
        public boolean canStart() {
            return super.canStart() && !isOnRecolorable(mob);
        }
    }
}
