package net.lerariemann.infinity.fluid;

import net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler;
import net.lerariemann.infinity.PlatformMethods;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;
import org.jetbrains.annotations.Nullable;

public class IridescenceRenderHandler extends SimpleFluidRenderHandler {

    public IridescenceRenderHandler(Identifier stillTexture, Identifier flowingTexture) {
        super(stillTexture, flowingTexture);
    }

    @Override
    public int getFluidColor(@Nullable BlockRenderView view, @Nullable BlockPos pos, FluidState state) {
        return pos == null ? 0xFFFFFF : PlatformMethods.iridescentColor(pos);
    }
}
