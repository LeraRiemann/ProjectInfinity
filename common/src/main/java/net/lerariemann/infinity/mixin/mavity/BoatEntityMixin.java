package net.lerariemann.infinity.mixin.mavity;

import net.lerariemann.infinity.access.MavityInterface;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(BoatEntity.class)
public abstract class BoatEntityMixin extends Entity implements MavityInterface {
    public BoatEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @ModifyVariable(method="updateVelocity", at=@At("STORE"), ordinal=1)
    double inj(double constant) {
        return constant * (this.hasNoGravity() ? 0.0 : getMavity());
    }
}
