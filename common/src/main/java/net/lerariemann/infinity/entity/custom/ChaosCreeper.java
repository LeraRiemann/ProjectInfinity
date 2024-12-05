package net.lerariemann.infinity.entity.custom;

import net.lerariemann.infinity.block.custom.BiomeBottle;
import net.lerariemann.infinity.entity.ModEntities;
import net.lerariemann.infinity.item.ModItems;
import net.lerariemann.infinity.util.RandomProvider;
import net.lerariemann.infinity.util.WeighedStructure;
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
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import org.jetbrains.annotations.Nullable;

import java.awt.Color;

public class ChaosCreeper extends CreeperEntity implements TintableEntity {
    public static TrackedData<Integer> color = DataTracker.registerData(ChaosCreeper.class, TrackedDataHandlerRegistry.INTEGER);
    public static TrackedData<Float> range = DataTracker.registerData(ChaosCreeper.class, TrackedDataHandlerRegistry.FLOAT);
    public static TrackedData<String> biome = DataTracker.registerData(ChaosCreeper.class, TrackedDataHandlerRegistry.STRING);
    public Registry<Biome> reg;

    public ChaosCreeper(EntityType<? extends CreeperEntity> entityType, World world) {
        super(entityType, world);
    }

    public void setRandomCharge() {
        setRange((float)(10*(1 + random.nextFloat()*(Math.sqrt(10) - 1))));
    }

    @Override
    @Nullable
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData) {
        MinecraftServer s = world.toServerWorld().getServer();
        reg = s.getRegistryManager().getOrThrow(RegistryKeys.BIOME);
        WeighedStructure<String> biomes = RandomProvider.getProvider(s).registry.get("biomes");
        String biomename = biomes != null ? biomes.getElement(world.getRandom().nextDouble()) : "minecraft:plains";
        Biome b = reg.get(Identifier.of(biomename));
        this.setColor(b != null ? b.getFoliageColor() : 7842607);
        this.setRandomCharge();
        this.setBiome(biomename);
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

    public Biome getBiome() {
        return reg.get(Identifier.of(getBiomeId()));
    }

    public String getBiomeId() {
        return this.dataTracker.get(biome);
    }

    public void setColor(int c) {
        this.dataTracker.set(color, c);
    }

    @Override
    public int getColorNamed() {
        if (hasCustomName()) {
            String s = getName().getString();
            if ("jeb_".equals(s)) {
                return TintableEntity.getColorJeb(age, getId());
            }
            if ("hue".equals(s)) {
                int n = age + 400 * getId();
                float hue = n / 400.f;
                hue = hue - (int) hue;
                return Color.getHSBColor(hue, 1.0f, 1.0f).getRGB();
            }
        }
        return -1;
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
        nbt.putString("biome", this.getBiomeId());
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
                BiomeBottle.spread(serverWorld, getBlockPos(), Identifier.of(getBiomeId()), getCharge());
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
        if (itemStack.isOf(ModItems.BIOME_BOTTLE_ITEM.get()) && BiomeBottle.isEmpty(itemStack)) {
            ItemStack itemStack2 = new ItemStack(itemStack.getItem());
            this.playSound(SoundEvents.ITEM_BOTTLE_FILL, 1.0f, 1.0f);
            CreeperEntity newCreeper;
            if (!this.getWorld().isClient() && (newCreeper = EntityType.CREEPER.create(this.getWorld())) != null) {
                itemStack2.applyComponentsFrom(BiomeBottle.addComponents(ComponentMap.builder(),
                                Identifier.of(getBiomeId()), getColorRaw(), getCharge()).build());
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
