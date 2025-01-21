package net.lerariemann.infinity.entity.custom;

import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.registry.core.ModEntities;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffect;
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
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.*;

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
        NbtCompound effect = InfinityMod.provider.randomElement(r, "effects");
        this.setEffect(effect);
        this.setDuration(r.nextInt(600));
        return super.initialize(world, difficulty, spawnReason, entityData, entityNbt);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(effect, "luck");
        this.dataTracker.startTracking(duration, 200);
        this.dataTracker.startTracking(color, 0x00FF00);
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
                StatusEffect newEffect = Registries.STATUS_EFFECT.get(new Identifier(i));
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
            ItemStack itemStack2 = setPotion(Items.POTION.getDefaultStack(), this.getEffectRawId(), this.getDuration() * 20);
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
        setEffect(eff.getString("Name"), eff.getInt("Color"));
    }
    public void setEffect(String eff, int c) {
        if (eff.isBlank()) {
            NbtCompound newEffect = InfinityMod.provider.randomElement(random, "effects");
            eff = newEffect.getString("Name");
            c = newEffect.getInt("Color");
        }
        this.dataTracker.set(effect, eff);
        this.dataTracker.set(color, c);
    }
    public String getEffect() {
        return this.dataTracker.get(effect);
    }

    public int getEffectRawId() {
        return reg.getRawId(getEffect());
    }

    @Override
    public int getColorRaw() {
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
        this.setEffect(nbt.getString("effect"), nbt.getInt("color"));
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
        stack.setCustomName(Text.translatable("potion.infinity.skeleton").setStyle(Style.EMPTY.withItalic(false)));
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
    protected void dropEquipment(DamageSource source, int lootingLevel, boolean allowDrops) {
        if (allowDrops) {
            super.dropEquipment(source, lootingLevel, allowDrops);
            int count = random.nextBetween(0, 2 + lootingLevel);
            this.dropStack(getProjectileType().copyWithCount(count));
        }
    }
}