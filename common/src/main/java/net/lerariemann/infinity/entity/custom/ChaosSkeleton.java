package net.lerariemann.infinity.entity.custom;

import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.registry.core.ModEntities;
import net.lerariemann.infinity.util.core.ConfigType;
import net.lerariemann.infinity.util.core.NbtUtils;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
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
import java.util.List;

public class ChaosSkeleton extends SkeletonEntity implements TintableEntity {
    private static final TrackedData<String> effect = DataTracker.registerData(ChaosSkeleton.class, TrackedDataHandlerRegistry.STRING);
    private static final TrackedData<Integer> color = DataTracker.registerData(ChaosSkeleton.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> duration = DataTracker.registerData(ChaosSkeleton.class, TrackedDataHandlerRegistry.INTEGER);
    public static Map<String, String> effect_lookup = Map.ofEntries(Map.entry("minecraft:unluck", "minecraft:luck"),
            Map.entry("minecraft:bad_omen", "minecraft:hero_of_the_village"),
            Map.entry("minecraft:darkness", "minecraft:night_vision"),
            Map.entry("minecraft:blindness", "minecraft:night_vision"),
            Map.entry("minecraft:glowing", "minecraft:invisibility"),
            Map.entry("minecraft:hunger", "minecraft:saturation"),
            Map.entry("minecraft:levitation", "minecraft:slow_falling"),
            Map.entry("minecraft:mining_fatigue", "minecraft:haste"),
            Map.entry("minecraft:poison", "minecraft:regeneration"),
            Map.entry("minecraft:slowness", "minecraft:speed"),
            Map.entry("minecraft:weakness", "minecraft:strength"),
            Map.entry("minecraft:wither", "minecraft:regeneration"),
            Map.entry("minecraft:instant_damage", "minecraft:instant_health"),
            Map.entry("minecraft:instant_health", "minecraft:instant_damage"),
            Map.entry("minecraft:luck", "minecraft:unluck"),
            Map.entry("minecraft:hero_of_the_village", "minecraft:bad_omen"),
            Map.entry("minecraft:night_vision", "minecraft:darkness"),
            Map.entry("minecraft:invisibility", "minecraft:glowing"),
            Map.entry("minecraft:saturation", "minecraft:hunger"),
            Map.entry("minecraft:slow_falling", "minecraft:levitation"),
            Map.entry("minecraft:haste", "minecraft:mining_fatigue"),
            Map.entry("minecraft:regeneration", "minecraft:poison"),
            Map.entry("minecraft:speed", "minecraft:slowness"),
            Map.entry("minecraft:strength", "minecraft:weakness"));

    public ChaosSkeleton(EntityType<? extends SkeletonEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    public int getColorNamed() {
        return hasCustomName() ? TintableEntity.getColorNamed(getName().getString(), age, getId()) : -1;
    }

    @Override
    @Nullable
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData) {
        Random r = new Random();
        NbtCompound effect = InfinityMod.provider.randomElement(r, ConfigType.EFFECTS);
        this.setEffect(effect);
        this.setDuration(r.nextInt(600));
        return super.initialize(world, difficulty, spawnReason, entityData);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(effect, "luck");
        builder.add(duration, 200);
        builder.add(color, 0x00FF00);
    }
    @Override
    public boolean isAffectedByDaylight() {
        return false;
    }

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        ItemStack itemStack = player.getStackInHand(hand);
        if (itemStack.isOf(Items.FERMENTED_SPIDER_EYE) && effect_lookup.containsKey(this.getEffect())) {
            if (!player.getAbilities().creativeMode) {
                itemStack.decrement(1);
            }
            if (player.getWorld().getRandom().nextFloat() < 0.5) {
                String i = effect_lookup.get(this.getEffect());
                StatusEffect newEffect = Registries.STATUS_EFFECT.get(Identifier.of(i));
                if (newEffect != null) {
                    ChaosSkeleton newSkeleton;
                    if (!this.getWorld().isClient() && (newSkeleton = ModEntities.CHAOS_SKELETON.get().create(this.getWorld())) != null) {
                        ((ServerWorld) this.getWorld()).spawnParticles(ParticleTypes.HEART, this.getX(), this.getBodyY(0.5), this.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
                        this.discard();
                        ModEntities.copy(this, newSkeleton);
                        newSkeleton.setDuration(this.getDuration());
                        newSkeleton.setEffect(i, newEffect.getColor());
                        this.getWorld().spawnEntity(newSkeleton);
                        return ActionResult.SUCCESS;
                    }
                }
            }
        }
        if (itemStack.isOf(Items.GLASS_BOTTLE)) {
            ItemStack itemStack2 = setPotion(Items.POTION.getDefaultStack(), this.getColorForRender(), this.getEffect(), this.getDuration() * 20);
            ItemStack itemStack3 = ItemUsage.exchangeStack(itemStack, player, itemStack2, false);
            player.setStackInHand(hand, itemStack3);
            this.playSound(SoundEvents.ITEM_BOTTLE_FILL, 1.0f, 1.0f);
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

    public void setEffect(NbtCompound eff) {
        setEffect(NbtUtils.getString(eff, "Name"), NbtUtils.getInt(eff, "Color"));
    }
    public void setEffect(String eff, int c) {
        if (eff.isBlank()) {
            NbtCompound newEffect = InfinityMod.provider.randomElement(random, ConfigType.EFFECTS);
            eff = NbtUtils.getString(newEffect, "Name");
            c = NbtUtils.getInt(newEffect, "Color");
        }
        this.dataTracker.set(effect, eff);
        this.dataTracker.set(color, c);
    }
    public String getEffect() {
        return this.dataTracker.get(effect);
    }
    @Override
    public int getColor() {
        return this.dataTracker.get(color);
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
        nbt.putString("effect", this.getEffect());
        nbt.putInt("duration", this.getDuration());
        nbt.putInt("color", this.getColor());
    }
    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.setEffect(NbtUtils.getString(nbt, "effect"), NbtUtils.getInt(nbt, "color"));
        this.setDuration(NbtUtils.getInt(nbt,"duration"));
    }

    public static ItemStack setPotion(ItemStack stack, int color, String effect, int duration) {
        NbtCompound potionEffect = new NbtCompound();
        potionEffect.putString("id", effect);
        potionEffect.putInt("duration", duration);
        List<StatusEffectInstance> customEffects = new ArrayList<>();
        customEffects.add(StatusEffectInstance.fromNbt(potionEffect));
        stack.set(DataComponentTypes.POTION_CONTENTS, new PotionContentsComponent(Optional.empty(), Optional.of(color), customEffects));
        stack.set(DataComponentTypes.ITEM_NAME, Text.translatable("potion.infinity.skeleton"));
        return stack;
    }

    @Override
    public ItemStack getProjectileType(ItemStack stack) {
        return getProjectileType();
    }

    public ItemStack getProjectileType() {
        return setPotion(Items.TIPPED_ARROW.getDefaultStack(), this.getColor(), this.getEffect(), this.getDuration());
    }

    @Override
    protected void dropEquipment(ServerWorld world, DamageSource source, boolean causedByPlayer) {
        super.dropEquipment(world, source, causedByPlayer);
        ItemStack weapon = source.getWeaponStack();
        RegistryEntry<Enchantment> looting = world.getServer().getRegistryManager().get(RegistryKeys.ENCHANTMENT).entryOf(Enchantments.LOOTING);
        int lootingLevel = weapon == null ? 0 : weapon.getEnchantments().getLevel(looting);
        int count = world.random.nextBetween(0, 2 + lootingLevel);
        this.dropStack(getProjectileType().copyWithCount(count));
    }
}
