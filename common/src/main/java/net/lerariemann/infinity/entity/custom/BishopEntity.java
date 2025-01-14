package net.lerariemann.infinity.entity.custom;

import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.util.var.BishopBattle;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.RangedAttackMob;
import net.minecraft.entity.ai.goal.BowAttackGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.MobNavigation;
import net.minecraft.entity.ai.pathing.PathNodeNavigator;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class BishopEntity extends AbstractChessFigure implements RangedAttackMob {
    @Nullable
    public BishopBattle battle;

    private BowAttackGoal<BishopEntity> bowAttackGoal;
    private MeleeAttackGoal meleeAttackGoal;

    public BishopEntity(EntityType<? extends BishopEntity> entityType, World world) {
        super(entityType, world);
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.35)
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 150)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 5.0);
    }
    @Override
    protected void initGoals() {
        targetSelector.add(2, new AntEntity.AntBattleGoal<>(this, PlayerEntity.class, true));
        super.initGoals();
    }
    @Override
    protected void initAttackType() {
        bowAttackGoal = new BowAttackGoal<>(this, 1.0, 20, 15.0F) {
            @Override
            public boolean isHoldingBow() {
                return true;
            }
        };
        meleeAttackGoal = new MeleeAttackGoal(this, 1.2, false) {
            @Override
            public void stop() {
                super.stop();
                mob.setAttacking(false);
            }

            @Override
            public void start() {
                super.start();
                mob.setAttacking(true);
            }
        };
        updateAttackType();
    }
    public void updateAttackType() {
        if (this.getWorld() != null && !this.getWorld().isClient) {
            goalSelector.remove(meleeAttackGoal);
            goalSelector.remove(bowAttackGoal);
            if (random.nextBoolean()) {
                goalSelector.add(2, meleeAttackGoal);
                equipStack(EquipmentSlot.MAINHAND, (random.nextBoolean() ? Items.IRON_SWORD : Items.IRON_AXE).getDefaultStack());
            }
            else {
                goalSelector.add(2, bowAttackGoal);
                if (!isHolding(Items.BOW)) equipStack(EquipmentSlot.MAINHAND, Items.BOW.getDefaultStack());
            }
        }
    }

    @Override
    public void tickMovement() {
        if (age % 200 == 0) updateAttackType();
        super.tickMovement();
    }
    @Override
    public void tickRiding() {
        super.tickRiding();
        if (this.getControllingVehicle() instanceof PathAwareEntity pathAwareEntity) {
            this.bodyYaw = pathAwareEntity.bodyYaw;
        }
    }

    @Override
    protected EntityNavigation createNavigation(World world) {
        return new MobNavigation(this, world) {
            @Override
            protected PathNodeNavigator createPathNodeNavigator(int range) {
                this.nodeMaker = new BishopNodeMaker();
                this.nodeMaker.setCanEnterOpenDoors(true);
                return new PathNodeNavigator(this.nodeMaker, range);
            }
        };
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        if (battle != null) nbt.putString("battle", battle.teamName);
    }
    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        if (nbt.contains("battle") && getWorld() instanceof ServerWorld w)
            battle = new BishopBattle(w, nbt.getString("battle"));
    }
    public void addToBattle(BishopBattle battle) {
        this.battle = battle;
        battle.addEntity(this);
    }
    @Override
    protected void onRemoval(Entity.RemovalReason reason) {
        super.onRemoval(reason);
        if (battle != null) battle.stop();
    }

    @Override
    public boolean isInBattle(String battleName) {
        return battle != null && battle.teamName.contains(battleName);
    }
    @Override
    public boolean isInBattle() {
        return battle != null;
    }
    @Override
    public boolean isBlackOrWhite() {
        return true;
    }

    @Override
    public void shootAt(LivingEntity target, float pullProgress) {
        ItemStack bow = Items.BOW.getDefaultStack();
        PersistentProjectileEntity persistentProjectileEntity =
                ProjectileUtil.createArrowProjectile(this, getProjectileType(), pullProgress, bow);
        double d = target.getX() - this.getX();
        double e = target.getBodyY(0.333) - persistentProjectileEntity.getY();
        double f = target.getZ() - this.getZ();
        double g = Math.sqrt(d * d + f * f);
        persistentProjectileEntity.setVelocity(d, e + g * 0.2F, f, 1.6F, (float)(14 - this.getWorld().getDifficulty().getId() * 4));
        this.playSound(SoundEvents.ENTITY_SKELETON_SHOOT, 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
        this.getWorld().spawnEntity(persistentProjectileEntity);
    }

    @Override
    public ItemStack getProjectileType(ItemStack stack) {
        return getProjectileType();
    }

    public ItemStack getProjectileType() {
        NbtCompound effect = InfinityMod.provider.randomElement(random, "effects");
        if (!effect.getString("Category").equals("harmful")) return Items.ARROW.getDefaultStack();
        return ChaosSkeleton.setPotion(Items.TIPPED_ARROW.getDefaultStack(),
                effect.getInt("Color"),
                effect.getString("Name"),
                200);
    }
}
