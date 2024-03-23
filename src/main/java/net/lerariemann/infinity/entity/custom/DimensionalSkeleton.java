package net.lerariemann.infinity.entity.custom;

import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.registry.Registries;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.*;

public class DimensionalSkeleton extends SkeletonEntity implements TintableEntity {
    private static final TrackedData<Integer> effect = DataTracker.registerData(DimensionalSkeleton.class, TrackedDataHandlerRegistry.INTEGER);
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
        return super.initialize(world, difficulty, spawnReason, entityData, entityNbt);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(effect, StatusEffect.getRawId(StatusEffects.POISON));
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
    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putInt("effect", this.getEffectRaw());
    }
    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.setEffectRaw(nbt.getInt("effect"));
    }

    @Override
    public ItemStack getProjectileType(ItemStack stack) {
        return PotionUtil.setPotion(Items.TIPPED_ARROW.getDefaultStack(), new Potion(new StatusEffectInstance(this.getEffect(), 1800)));
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
