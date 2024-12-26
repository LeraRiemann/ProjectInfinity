package net.lerariemann.infinity.entity.custom;

import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.access.MobEntityAccess;
import net.lerariemann.infinity.iridescence.Iridescence;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.*;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.*;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Supplier;

public class ChaosPawn extends AbstractChessFigure {
    public static final TrackedData<NbtCompound> colors = DataTracker.registerData(ChaosPawn.class, TrackedDataHandlerRegistry.NBT_COMPOUND);
    public static final TrackedData<Integer> special_case = DataTracker.registerData(ChaosPawn.class, TrackedDataHandlerRegistry.INTEGER);

    public ChaosPawn(EntityType<? extends ChaosPawn> entityType, World world) {
        super(entityType, world);
    }
    public void setColors(NbtCompound i) {
        this.dataTracker.set(colors, i);
    }
    public NbtCompound getColors() {
        return this.dataTracker.get(colors);
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return HostileEntity.createHostileAttributes().add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 5.0)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 35.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.6F);
    }
    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(colors, new NbtCompound());
        this.dataTracker.startTracking(special_case, -1);
    }
    @Override
    protected void initGoals() {
        super.initGoals();
        this.goalSelector.add(5, new EatGrassGoal(this));
    }

    @Override
    public void onEatingGrass() {
        super.onEatingGrass();
        this.setAllColors(this.getWorld().getBiome(this.getBlockPos()).value().getGrassColorAt(this.getX(), this.getZ()));
    }

    public int getCase() {
        return dataTracker.get(special_case);
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.put("colors", getColors());
        nbt.putInt("case", getCase());
    }
    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.setColors(nbt.getCompound("colors"));
        this.dataTracker.set(special_case, nbt.getInt("case"));
    }

    @Override
    public Identifier getLootTableId() {
        return switch (dataTracker.get(special_case)) {
            case 0 -> Identifier.tryParse("infinity:entities/chaos_pawn_black");
            case 1 -> Identifier.tryParse("infinity:entities/chaos_pawn_white");
            default -> Identifier.tryParse("");
        };
    }

    public static NbtCompound getColorSetup(Supplier<Integer> colorSupplier) {
        NbtCompound c = new NbtCompound();
        Arrays.stream((new String[]{"body", "left_arm", "right_arm", "left_leg", "right_leg"})).forEach(
                s -> c.putInt(s, colorSupplier.get()));
        int head = colorSupplier.get();
        c.putInt("head", head);
        c.putInt("hat", 0xFFFFFF ^ head);
        return c;
    }

    public void setAllColors(int color) {
        this.setColors(getColorSetup(() -> color));
    }

    @Override
    public boolean isBlackOrWhite() {
        return dataTracker.get(special_case) != -1 && !Iridescence.isUnderEffect(this);
    }

    public void setAllColors(Random r, BlockState state) {
        if (state.isOf(Blocks.WHITE_WOOL) || state.isOf(Blocks.WHITE_CONCRETE)) {
            chess(true);
        }
        else if (state.isOf(Blocks.BLACK_WOOL) || state.isOf(Blocks.BLACK_CONCRETE)) {
            chess(false);
        }
        else this.randomizeColors(r);
    }

    public void chess(boolean white) {
        dataTracker.set(special_case, white ? 1 : 0);
        setAllColors(white ? 0xFFFFFF : 0);
    }

    public void unchess() {
        dataTracker.set(special_case, -1);
        randomizeColors(random);
        playSound(SoundEvents.ENTITY_ZOMBIE_VILLAGER_CONVERTED, 1.0f, 1.0f);
    }

    public void randomizeColors(Random r) {
        setColors(getColorSetup(() -> r.nextInt(16777216)));
    }

    @Override
    @Nullable
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData, @Nullable NbtCompound entityNbt) {
        var r = new java.util.Random();
        setAllColors(random, world.getBlockState(this.getBlockPos().down()));
        double i = r.nextDouble() * 40;
        Objects.requireNonNull(this.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH)).setBaseValue(i);
        this.setHealth((float)i);
        int a;
        if ((a = (int)(0.1*i)) > 0) {
            this.equipLootStack(
                    EquipmentSlot.HEAD,
                    Registries.ITEM.get(new Identifier(InfinityMod.provider.randomName(r, "items"))).getDefaultStack().copyWithCount(a));
            ((MobEntityAccess)this).infinity$setPersistent(false);
        }
        return super.initialize(world, difficulty, spawnReason, entityData, entityNbt);
    }

    @Override
    protected void dropEquipment(DamageSource source, int lootingMultiplier, boolean allowDrops) {
        super.dropEquipment(source, lootingMultiplier, allowDrops);
        if (!this.isBlackOrWhite()) {
            String s = InfinityMod.provider.registry.get("items").getRandomElement(getWorld().random);
            double i = Objects.requireNonNull(this.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH)).getBaseValue() / 10;
            ItemStack stack = Registries.ITEM.get(new Identifier(s)).getDefaultStack().copyWithCount((int)(i*i));
//            stack.applyComponentsFrom(ComponentMap.builder().add(DataComponentTypes.MAX_STACK_SIZE, 64).build());
            this.dropStack(stack);
        }
    }
}
