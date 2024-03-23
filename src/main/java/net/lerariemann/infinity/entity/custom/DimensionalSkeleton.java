package net.lerariemann.infinity.entity.custom;

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
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.registry.Registries;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.*;

public class DimensionalSkeleton extends SkeletonEntity implements TintableEntity {
    private static final TrackedData<Integer> effect = DataTracker.registerData(DimensionalSkeleton.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> duration = DataTracker.registerData(DimensionalSkeleton.class, TrackedDataHandlerRegistry.INTEGER);
    public DimensionalSkeleton(EntityType<? extends SkeletonEntity> entityType, World world) {
        super(entityType, world);
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
    public Vector3f getColor() {
        int i = this.getEffect().getColor();
        float f = (float)(i >> 16 & 0xFF) / 255.0f;
        float g = (float)(i >> 8 & 0xFF) / 255.0f;
        float h = (float)(i & 0xFF) / 255.0f;
        return new Vector3f(f, g, h);
    }

    @Override
    public float getAlpha() {
        return 1.0f;
    }
}
