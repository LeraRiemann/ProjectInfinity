package net.lerariemann.infinity.fluids.fabric;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRenderHandler;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributeHandler;
import net.lerariemann.infinity.registry.core.ModBlocks;
import net.lerariemann.infinity.iridescence.Iridescence;
import net.lerariemann.infinity.registry.core.ModItems;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FluidBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.state.StateManager;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.*;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

import static net.lerariemann.infinity.InfinityMod.MOD_ID;

public class ModFluidsFabric {
    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(MOD_ID, RegistryKeys.FLUID);
    public static final RegistrySupplier<IridescenceFlowing> IRIDESCENCE_FLOWING =
            FLUIDS.register("flowing_iridescence", IridescenceFlowing::new);
    public static final RegistrySupplier<IridescenceStill> IRIDESCENCE_STILL =
            FLUIDS.register("iridescence", IridescenceStill::new);

    public static void registerModFluids() {
        FLUIDS.register();
    }

    public static abstract class IridescenceFabric extends FlowableFluid {
        @Override
        public Fluid getFlowing() {
            return ModFluidsFabric.IRIDESCENCE_FLOWING.get();
        }
        @Override
        public Fluid getStill() {
            return ModFluidsFabric.IRIDESCENCE_STILL.get();
        }
        @Override
        public Item getBucketItem() {
            return ModItems.IRIDESCENCE_BUCKET.get();
        }

        @Override
        protected BlockState toBlockState(FluidState state) {
            return ModBlocks.IRIDESCENCE.get().getDefaultState().with(FluidBlock.LEVEL, getBlockStateLevel(state));
        }
        @Override
        protected boolean isInfinite(World world) {
            return Iridescence.isInfinite(world);
        }
        @Override
        protected void beforeBreakingBlock(WorldAccess world, BlockPos pos, BlockState state) {
            BlockEntity blockEntity = state.hasBlockEntity() ? world.getBlockEntity(pos) : null;
            Block.dropStacks(state, world, pos, blockEntity);
        }
        @Override
        protected int getMaxFlowDistance(WorldView world) {
            return 4;
        }
        @Override
        protected int getLevelDecreasePerBlock(WorldView world) {
            return 1;
        }
        @Override
        protected boolean canBeReplacedWith(FluidState state, BlockView world, BlockPos pos, Fluid fluid, Direction direction) {
            return direction == Direction.DOWN && !this.matchesType(fluid);
        }
        @Override
        public int getTickRate(WorldView world) {
            return 5;
        }
        @Override
        protected float getBlastResistance() {
            return 100.0F;
        }
        @Override
        public boolean matchesType(Fluid fluid) {
            return fluid == getStill() || fluid == getFlowing();
        }
    }
    public static class IridescenceStill extends IridescenceFabric {
        @Override
        public boolean isStill(FluidState state) {
            return true;
        }
        @Override
        public int getLevel(FluidState state) {
            return 8;
        }
    }
    public static class IridescenceFlowing extends IridescenceFabric {
        public IridescenceFlowing() {
            super();
            this.setDefaultState(this.getStateManager().getDefaultState().with(LEVEL, 7));
        }
        @Override
        protected void appendProperties(StateManager.Builder<Fluid, FluidState> builder) {
            super.appendProperties(builder);
            builder.add(LEVEL);
        }
        @Override
        public int getLevel(FluidState state) {
            return state.get(LEVEL);
        }
        @Override
        public boolean isStill(FluidState state) {
            return false;
        }
    }

    public static class IridescenceVariantAttributeHandler implements FluidVariantAttributeHandler {
        @Override
        public Text getName(FluidVariant fluidVariant) {
            return Text.translatable("block.infinity.iridescence").withColor(Iridescence.getTimeBasedColor());
        }
    }
    public static class IridescenceVariantRenderHandler implements FluidVariantRenderHandler {
        @Override
        public int getColor(FluidVariant fluidVariant, @Nullable BlockRenderView view, @Nullable BlockPos pos) {
            return (view != null && pos != null) ? Iridescence.getPosBasedColor(pos) : Iridescence.getTimeBasedColor();
        }
    }
    public static class IridescenceRenderHandler implements FluidRenderHandler {
        @Override
        public Sprite[] getFluidSprites(@Nullable BlockRenderView view, @Nullable BlockPos pos, FluidState state) {
            Function<Identifier, Sprite> atlas = MinecraftClient.getInstance().getSpriteAtlas(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE);
            Sprite overlaySprite = atlas.apply(Iridescence.OVERLAY_TEXTURE);
            Sprite[] sprites = new Sprite[overlaySprite == null ? 2 : 3];
            sprites[0] = atlas.apply(Iridescence.TEXTURE);
            sprites[1] = atlas.apply(Iridescence.FLOWING_TEXTURE);
            if (overlaySprite != null) sprites[2] = overlaySprite;
            return sprites;
        }

        @Override
        public int getFluidColor(@Nullable BlockRenderView view, @Nullable BlockPos pos, FluidState state) {
            return (view != null && pos != null) ? Iridescence.getPosBasedColor(pos) : Iridescence.getTimeBasedColor();
        }
    }
}
