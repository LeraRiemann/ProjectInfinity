package net.lerariemann.infinity.entity.custom;

import net.lerariemann.infinity.iridescence.Iridescence;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.Angerable;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.TimeHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public abstract class AbstractChessFigure extends HostileEntity implements Angerable {
    protected int angerTime;
    @Nullable
    protected UUID angryAt;

    protected AbstractChessFigure(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
    }

    public abstract boolean isChess();

    @Override
    protected void initGoals() {
        initRegularGoals();
        initChessGoals();
    }
    protected void initChessGoals() {
        this.targetSelector.add(1, new ChessRevengeGoal(this).setGroupRevenge());
        this.goalSelector.add(2, new MeleeAttackGoal(this, 1.0, false));
        this.targetSelector.add(3, new ChaosCleanseGoal<>(this, ChaosSlime.class, true));
        this.targetSelector.add(3, new ChaosCleanseGoal<>(this, ChaosSkeleton.class, true));
        this.targetSelector.add(3, new ChessUniversalAngerGoal(this));
    }
    protected void initRegularGoals() {
        this.goalSelector.add(0, new SwimGoal(this));
        this.goalSelector.add(7, new WanderAroundFarGoal(this, 1.0));
        this.goalSelector.add(8, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));
        this.goalSelector.add(8, new LookAroundGoal(this));
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

    public boolean isInBattle() {
        return isInBattle("battle");
    }
    public boolean isInBattle(String battleName) {
        Team t = getScoreboardTeam();
        return (t != null && t.getName().contains(battleName));
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        this.writeAngerToNbt(nbt);
    }
    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.readAngerFromNbt(this.getWorld(), nbt);
    }

    @Override
    protected void mobTick() {
        this.tickAngerLogic((ServerWorld)this.getWorld(), false);
        super.mobTick();
    }

    @Override
    public float getPathfindingFavor(BlockPos pos, WorldView world) {
        if (!isChess()) return 0.0f;
        if (Iridescence.isIridescence(world, pos)) return -1.0F;
        return 0.0f;
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

    public boolean shouldPursueRegularGoals() {
        return (!Iridescence.isUnderEffect(this));
    }
    public boolean shouldPursueChessGoals() {
        return shouldPursueRegularGoals() && isChess();
    }

    public static boolean isAngerCompatible(AbstractChessFigure fig1, AbstractChessFigure fig2) {
        if (fig1 instanceof ChaosPawn p1 && fig2 instanceof ChaosPawn p2) return p1.getCase() == p2.getCase();
        return fig1.isChess() ^ !fig2.isChess();
    }

    public static class ChaosCleanseGoal<T extends LivingEntity> extends ActiveTargetGoal<T> {
        public ChaosCleanseGoal(MobEntity mob, Class<T> targetClass, boolean checkVisibility) {
            super(mob, targetClass, checkVisibility);
        }

        @Override
        public boolean canStart() {
            if (mob instanceof AbstractChessFigure e && !e.shouldPursueChessGoals()) return false;
            return super.canStart();
        }
    }

    public static class ChessUniversalAngerGoal extends Goal {
        private final AbstractChessFigure mob;
        private int lastAttackedTime;

        public ChessUniversalAngerGoal(AbstractChessFigure mob) {
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
                    && this.mob.shouldPursueRegularGoals();
        }

        @Override
        public void start() {
            this.lastAttackedTime = this.mob.getLastAttackedTime();
            this.mob.universallyAnger();
            this.getOthersInRange().stream().filter(entity -> {
                if (entity == mob) return false;
                if (Iridescence.isUnderEffect(entity)) return false;
                if (entity instanceof AbstractChessFigure pawn) {
                    return isAngerCompatible(mob, pawn);
                }
                return true;
            }).map(entity -> (Angerable)entity).forEach(Angerable::universallyAnger);
            super.start();
        }

        private List<? extends AbstractChessFigure> getOthersInRange() {
            double d = this.mob.getAttributeValue(EntityAttributes.GENERIC_FOLLOW_RANGE);
            Box box = Box.from(this.mob.getPos()).expand(d, 10.0, d);
            return this.mob.getWorld().getEntitiesByClass(this.mob.getClass(), box, EntityPredicates.EXCEPT_SPECTATOR);
        }
    }

    public static class ChessRevengeGoal extends RevengeGoal {
        public ChessRevengeGoal(PathAwareEntity mob, Class<?>... noRevengeTypes) {
            super(mob, noRevengeTypes);
        }

        @Override
        protected void callSameTypeForRevenge() {
            if (mob instanceof AbstractChessFigure figure && figure.shouldPursueRegularGoals()) {
                double d = this.getFollowRange();
                Box box = Box.from(figure.getPos()).expand(d, 10.0, d);
                List<AbstractChessFigure> list = figure.getWorld().getEntitiesByClass(AbstractChessFigure.class, box, EntityPredicates.EXCEPT_SPECTATOR);
                for (AbstractChessFigure pawn2 : list) {
                    if (figure != pawn2
                            && pawn2.getTarget() == null
                            && !pawn2.isTeammate(figure.getAttacker())
                            && !Iridescence.isUnderEffect(pawn2)
                            && isAngerCompatible(pawn2, figure))
                        this.setMobEntityTarget(pawn2, figure.getAttacker());
                }
            }
        }
    }
}
