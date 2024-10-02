package net.lerariemann.infinity.entity.custom;

import net.lerariemann.infinity.entity.ModEntities;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class DimensionalSkeleton extends SkeletonEntity implements TintableEntity {
    static Registry<StatusEffect> reg = Registries.STATUS_EFFECT;
    private static final TrackedData<String> effect = DataTracker.registerData(DimensionalSkeleton.class, TrackedDataHandlerRegistry.STRING);
    private static final TrackedData<Integer> color = DataTracker.registerData(DimensionalSkeleton.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> duration = DataTracker.registerData(DimensionalSkeleton.class, TrackedDataHandlerRegistry.INTEGER);
    public static final Map<String, String> effect_lookup = Map.ofEntries(Map.entry("unluck", "luck"), Map.entry("bad_omen", "hero_of_the_village"),
            Map.entry("blindness", "night_vision"), Map.entry("darkness", "night_vision"), Map.entry("glowing", "invisibility"),
            Map.entry("hunger", "saturation"), Map.entry("levitation", "slow_falling"), Map.entry("mining_fatigue", "haste"),
            Map.entry("poison", "regeneration"), Map.entry("slowness", "speed"), Map.entry("weakness", "strength"),
            Map.entry("wither", "regeneration"));
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
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData) {
        Random r = new Random();
        List<Identifier> a = new ArrayList<>();
        reg.getIds().forEach(i -> {
            if (Objects.requireNonNull(reg.get(i)).getCategory().equals(StatusEffectCategory.HARMFUL)) a.add(i);
        });
        Identifier e = a.get(r.nextInt(a.size()));
        this.setEffect(e);
        this.setColorRaw(Objects.requireNonNull(reg.get(e)).getColor());
        this.setDuration(r.nextInt(200));
        return super.initialize(world, difficulty, spawnReason, entityData);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(effect, "luck");
        builder.add(duration, 200);
        builder.add(color, 255);
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
                if (!this.getWorld().isClient() && (newSkeleton = ModEntities.CHAOS_SKELETON.create(this.getWorld())) != null) {
                    ((ServerWorld)this.getWorld()).spawnParticles(ParticleTypes.HEART, this.getX(), this.getBodyY(0.5), this.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
                    this.discard();
                    ModEntities.copy(this, newSkeleton);
                    newSkeleton.setDuration(this.getDuration());
                    String i = effect_lookup.get(this.getEffectRaw());
                    StatusEffect e = reg.get(Identifier.of(i));
                    newSkeleton.setEffectRaw(i);
                    if (e!= null) newSkeleton.setColorRaw(e.getColor());
                    this.getWorld().spawnEntity(newSkeleton);
                    return ActionResult.SUCCESS;
                }
            }
        }
        if (itemStack.isOf(Items.GLASS_BOTTLE)) {
            ItemStack itemStack2 = setPotion(Items.LINGERING_POTION.getDefaultStack(), this.getColorForRender(), this.getEffectRaw(), this.getDuration() * 60);
            ItemStack itemStack3 = ItemUsage.exchangeStack(itemStack, player, itemStack2, false);
            player.setStackInHand(hand, itemStack3);
            this.playSound(SoundEvents.ENTITY_COW_MILK, 1.0f, 1.0f);
            SkeletonEntity newSkeleton;
            if (!this.getWorld().isClient() && (newSkeleton = EntityType.SKELETON.create(this.getWorld())) != null) {
                this.discard();
                ModEntities.copy(this, newSkeleton);
                this.getWorld().spawnEntity(newSkeleton);
                return ActionResult.SUCCESS;
            }
        }
        return super.interactMob(player, hand);
    }

    public void setEffect(Identifier i) {
        setEffectRaw(i.toString());
    }
    public void setEffectRaw(String c) {
        this.dataTracker.set(effect, c);
    }
    public String getEffectRaw() {
        return this.dataTracker.get(effect);
    }
    public StatusEffect getEffect() {
        return reg.get(Identifier.of(getEffectRaw()));
    }
    public void setColorRaw(int c) {
        this.dataTracker.set(color, c);
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
        nbt.putString("effect", this.getEffectRaw());
        nbt.putInt("duration", this.getDuration());
        nbt.putInt("color", this.getColorRaw());
    }
    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.setEffectRaw(nbt.getString("effect"));
        this.setDuration(nbt.getInt("duration"));
        this.setColorRaw(nbt.getInt("color"));
    }

    public static ItemStack setPotion(ItemStack stack, int color, String effect, int duration) {
        NbtCompound poteffect = new NbtCompound();
        poteffect.putString("id", effect);
        poteffect.putInt("duration", duration);
        List<StatusEffectInstance> customEffects = new ArrayList<>();
        customEffects.add(StatusEffectInstance.fromNbt(poteffect));
        stack.set(DataComponentTypes.POTION_CONTENTS, new PotionContentsComponent(Optional.empty(), Optional.of(color), customEffects));
        return stack;
    }

    @Override
    public ItemStack getProjectileType(ItemStack stack) {
        return setPotion(Items.TIPPED_ARROW.getDefaultStack(), this.getColorRaw(), this.getEffectRaw(), this.getDuration());
    }

    @Override
    public int getColorRaw() {
        return this.dataTracker.get(color);
    }
}
