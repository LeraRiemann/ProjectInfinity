package net.lerariemann.infinity.entity.custom;

import net.lerariemann.infinity.entity.ModEntities;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class DimensionalSkeleton extends SkeletonEntity implements TintableEntity {
    private static final TrackedData<Integer> effect = DataTracker.registerData(DimensionalSkeleton.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> duration = DataTracker.registerData(DimensionalSkeleton.class, TrackedDataHandlerRegistry.INTEGER);
    public static final Map<Integer, Integer> effect_lookup = Map.ofEntries(Map.entry(27, 26), Map.entry(31, 32), Map.entry(15, 16),
            Map.entry(33, 16), Map.entry(24, 14), Map.entry(17, 23), Map.entry(25, 28), Map.entry(4, 3),
            Map.entry(19, 10), Map.entry(2, 1), Map.entry(18, 5), Map.entry(20, 10));
    public DimensionalSkeleton(EntityType<? extends SkeletonEntity> entityType, World world) {
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
    @Nullable
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData, @Nullable NbtCompound entityNbt) {
        Random r = new Random();
        List<StatusEffect> a = new ArrayList<>();
        Registries.STATUS_EFFECT.stream().forEach(e -> {
            if (e.getCategory().equals(StatusEffectCategory.HARMFUL)) a.add(e);
        });
        this.setEffect(a.get(r.nextInt(a.size())));
        this.setDuration(r.nextInt(200));
        return super.initialize(world, difficulty, spawnReason, entityData, entityNbt);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(effect, StatusEffect.getRawId(StatusEffects.POISON));
        this.dataTracker.startTracking(duration, 200);
    }

    public boolean isFriendly() {
        return !(getEffect().getCategory().equals(StatusEffectCategory.HARMFUL));
    }
    @Override
    public boolean cannotDespawn() {
        return super.cannotDespawn() || isFriendly();
    }
    @Override
    public boolean isAffectedByDaylight() {
        return super.isAffectedByDaylight() && !isFriendly();
    }

    public void copySkeleton(SkeletonEntity newSkeleton) {
        newSkeleton.refreshPositionAndAngles(this.getX(), this.getY(), this.getZ(), this.getYaw(), this.getPitch());
        newSkeleton.setHealth(this.getHealth());
        newSkeleton.bodyYaw = this.bodyYaw;
        if (this.hasCustomName()) {
            newSkeleton.setCustomName(this.getCustomName());
            newSkeleton.setCustomNameVisible(this.isCustomNameVisible());
        }
        if (this.isPersistent()) {
            newSkeleton.setPersistent();
        }
        newSkeleton.setInvulnerable(this.isInvulnerable());
        newSkeleton.setStackInHand(Hand.MAIN_HAND, this.getStackInHand(Hand.MAIN_HAND));
        newSkeleton.setStackInHand(Hand.OFF_HAND, this.getStackInHand(Hand.OFF_HAND));
    }

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        ItemStack itemStack = player.getStackInHand(hand);
        if (itemStack.isOf(Items.FERMENTED_SPIDER_EYE) && effect_lookup.containsKey(this.getEffectRaw())) {
            if (!player.getAbilities().creativeMode) {
                itemStack.decrement(1);
            }
            Random r = new Random();
            if (r.nextFloat() < 0.5) {
                DimensionalSkeleton newSkeleton;
                if (!this.getWorld().isClient() && (newSkeleton = ModEntities.DIMENSIONAL_SKELETON.create(this.getWorld())) != null) {
                    ((ServerWorld)this.getWorld()).spawnParticles(ParticleTypes.HEART, this.getX(), this.getBodyY(0.5), this.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
                    this.discard();
                    this.copySkeleton(newSkeleton);
                    newSkeleton.setDuration(this.getDuration());
                    newSkeleton.setEffectRaw(effect_lookup.get(this.getEffectRaw()));
                    this.getWorld().spawnEntity(newSkeleton);
                    return ActionResult.SUCCESS;
                }
            }
        }
        if (itemStack.isOf(Items.GLASS_BOTTLE)) {
            ItemStack itemStack2 = setPotion(Items.LINGERING_POTION.getDefaultStack(), this.getEffectRaw(), this.getDuration() * 60);
            ItemStack itemStack3 = ItemUsage.exchangeStack(itemStack, player, itemStack2, false);
            player.setStackInHand(hand, itemStack3);
            this.playSound(SoundEvents.ENTITY_COW_MILK, 1.0f, 1.0f);
            SkeletonEntity newSkeleton;
            if (!this.getWorld().isClient() && (newSkeleton = EntityType.SKELETON.create(this.getWorld())) != null) {
                this.discard();
                this.copySkeleton(newSkeleton);
                this.getWorld().spawnEntity(newSkeleton);
                return ActionResult.SUCCESS;
            }
        }
        return super.interactMob(player, hand);
    }


    public void setEffectRaw(int c) {
        this.dataTracker.set(effect, c);
    }
    public int getEffectRaw() {
        return this.dataTracker.get(effect);
    }
    public void setEffect(StatusEffect i) {
        setEffectRaw(StatusEffect.getRawId(i));
    }
    public StatusEffect getEffect() {
        return StatusEffect.byRawId(getEffectRaw());
    }
    public void setDuration(int i) {
        this.dataTracker.set(duration, i);
    }
    public int getDuration() {
        return this.dataTracker.get(duration);
    }
    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putInt("effect", this.getEffectRaw());
        nbt.putInt("duration", this.getDuration());
    }
    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.setEffectRaw(nbt.getInt("effect"));
        this.setDuration(nbt.getInt("duration"));
    }

    public static ItemStack setPotion(ItemStack stack, int effect, int duration) {
        NbtList effects = new NbtList();
        NbtCompound compound = new NbtCompound();
        compound.putInt("Id", effect);
        compound.putInt("Duration", duration);
        effects.add(compound);
        PotionUtil.setPotion(stack, Potions.WATER);
        stack.getOrCreateNbt().put("CustomPotionEffects", effects);
        return stack;
    }

    @Override
    public ItemStack getProjectileType(ItemStack stack) {
        return setPotion(Items.TIPPED_ARROW.getDefaultStack(), this.getEffectRaw(), this.getDuration());
    }

    @Override
    public int getColorRaw() {
        return this.getEffect().getColor();
    }
}
