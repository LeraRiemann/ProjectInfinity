package net.lerariemann.infinity.fluids.neoforge;

import net.lerariemann.infinity.iridescence.IridescenceLiquidBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.neoforged.neoforge.common.extensions.IBlockExtension;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class IridescenceLiquidBlockNeoforge extends IridescenceLiquidBlock implements IBlockExtension {
    public IridescenceLiquidBlockNeoforge(Supplier<? extends FlowableFluid> fluid, Settings properties) {
        super(fluid, properties);
    }

    @Override
    public PathNodeType getBlockPathType(@NotNull BlockState state, @NotNull BlockView level, @NotNull BlockPos pos, @Nullable MobEntity mob) {
        return PathNodeType.WATER;
    }
}
