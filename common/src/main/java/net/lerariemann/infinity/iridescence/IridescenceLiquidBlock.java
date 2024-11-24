package net.lerariemann.infinity.iridescence;

import dev.architectury.core.block.ArchitecturyLiquidBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.function.Supplier;

public class IridescenceLiquidBlock extends ArchitecturyLiquidBlock {
    public IridescenceLiquidBlock(Supplier<? extends FlowableFluid> fluid, Settings properties) {
        super(fluid, properties);
    }

    @Override
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        super.onEntityCollision(state, world, pos, entity);
        if (world.getFluidState(pos).getLevel() > 3) {
            if (entity instanceof PlayerEntity player) {
                Iridescence.tryBeginJourney(player, 4);
            }
            else if (entity instanceof MobEntity ent) {
                Iridescence.tryBeginConversion(ent);
            }
        }
    }
}
