package net.lerariemann.infinity.entity.custom;

import net.lerariemann.infinity.util.BishopBattle;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class BishopEntity extends AbstractChessFigure {
    @Nullable
    public BishopBattle battle;

    public BishopEntity(EntityType<? extends BishopEntity> entityType, World world) {
        super(entityType, world);
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.25)
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 150);
    }
    @Override
    protected void initGoals() {
        targetSelector.add(2, new AntEntity.AntBattleGoal<>(this, PlayerEntity.class, true));
        super.initGoals();
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
    public void onRemoved() {
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
}
