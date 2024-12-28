package net.lerariemann.infinity.entity.custom;

import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.block.custom.BiomeBottleBlock;
import net.lerariemann.infinity.registry.core.ModEntities;
import net.lerariemann.infinity.registry.core.ModItems;
import net.minecraft.component.ComponentMap;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class ChaosCreeper extends CreeperEntity implements TintableEntity {
    public static TrackedData<Integer> color = DataTracker.registerData(ChaosCreeper.class, TrackedDataHandlerRegistry.INTEGER);
    public static TrackedData<Float> range = DataTracker.registerData(ChaosCreeper.class, TrackedDataHandlerRegistry.FLOAT);
    public static TrackedData<String> biome = DataTracker.registerData(ChaosCreeper.class, TrackedDataHandlerRegistry.STRING);

    public ChaosCreeper(EntityType<? extends CreeperEntity> entityType, World world) {
        super(entityType, world);
    }

    public void setRandomCharge() {
        setRange((float)(10*(1 + random.nextFloat()*(Math.sqrt(10) - 1))));
    }

    @Override
    @Nullable
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData) {
        NbtCompound biome = InfinityMod.provider.randomElement(world.getRandom(), "biomes");
        this.setColor(biome.contains("Color") ? biome.getInt("Color") : 7842607);
        this.setRandomCharge();
        this.setBiome(biome.getString("Name"));
        return super.initialize(world, difficulty, spawnReason, entityData);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(color, 7842607);
        builder.add(range, 16.0f);
        builder.add(biome, "minecraft:plains");
    }

    public void setBiome(String s) {
        this.dataTracker.set(biome, s);
    }
    public String getBiome() {
        return this.dataTracker.get(biome);
    }

    public void setColor(int c) {
        this.dataTracker.set(color, c);
    }

    @Override
    public int getColorNamed() {
        return hasCustomName() ? TintableEntity.getColorNamed(getName().getString(), age, getId()) : -1;
    }

    @Override
    public int getColorRaw() {
        return this.dataTracker.get(color);
    }

    public void setRange(float s) {
        this.dataTracker.set(range, s);
    }

    public float getRange() {
        return this.dataTracker.get(range);
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putFloat("range", this.getRange());
        nbt.putInt("color", this.getColorRaw());
        nbt.putString("biome", this.getBiome());
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.setRange(nbt.getFloat("range"));
        this.setColor(nbt.getInt("color"));
        this.setBiome(nbt.getString("biome"));
    }

    public void blow_up() {
        float f = 3 * this.getRange() / 16;
        this.dead = true;
        MinecraftServer s = this.getServer();
        if (s != null) {
            ServerWorld serverWorld = s.getWorld(this.getWorld().getRegistryKey());
            if (serverWorld != null) {
                BiomeBottleBlock.spreadCircle(serverWorld, getBlockPos(), Identifier.of(getBiome()), getCharge());
            }
        }
        this.getWorld().createExplosion(this, this.getX(), this.getY(), this.getZ(), f, World.ExplosionSourceType.NONE);
        this.discard();
    }

    public int getCharge() {
        return (int)(this.getRange()*this.getRange());
    }

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        ItemStack itemStack = player.getStackInHand(hand);
        if (itemStack.isOf(ModItems.BIOME_BOTTLE_ITEM.get()) && BiomeBottleBlock.isEmpty(itemStack)) {
            ItemStack itemStack2 = new ItemStack(itemStack.getItem());
            this.playSound(SoundEvents.ITEM_BOTTLE_FILL, 1.0f, 1.0f);
            CreeperEntity newCreeper;
            if (!this.getWorld().isClient() && (newCreeper = EntityType.CREEPER.create(this.getWorld())) != null) {
                itemStack2.applyComponentsFrom(BiomeBottleBlock.addComponents(ComponentMap.builder(),
                                Identifier.of(getBiome()), getColorRaw(), getCharge()).build());
                ItemStack itemStack3 = ItemUsage.exchangeStack(itemStack, player, itemStack2, false);
                player.setStackInHand(hand, itemStack3);
                this.discard();
                ModEntities.copy(this, newCreeper);
                this.getWorld().spawnEntity(newCreeper);
                return ActionResult.SUCCESS;
            }
        }
        return super.interactMob(player, hand);
    }
}
