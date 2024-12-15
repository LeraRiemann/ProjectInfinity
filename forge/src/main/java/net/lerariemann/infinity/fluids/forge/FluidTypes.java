package net.lerariemann.infinity.fluids.forge;

import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.iridescence.Iridescence;
import net.minecraft.block.Blocks;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.SoundActions;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fluids.FluidInteractionRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class FluidTypes {
    private static final DeferredRegister<FluidType> FLUID_TYPES = DeferredRegister.create(ForgeRegistries.Keys.FLUID_TYPES, InfinityMod.MOD_ID);

    public static final RegistryObject<FluidType> IRIDESCENCE_TYPE = FLUID_TYPES.register("iridescence",
            () -> new IridescentFluidType(FluidType.Properties.create()
            .descriptionId("fluid.infinity.iridescence")
            .fallDistanceModifier(0F)
            .canExtinguish(true)
            .canConvertToSource(true)
            .supportsBoating(true)
            .canSwim(true)
            .canHydrate(true)
            .sound(SoundActions.BUCKET_FILL, SoundEvents.ITEM_BUCKET_FILL)
            .sound(SoundActions.BUCKET_EMPTY, SoundEvents.ITEM_BUCKET_EMPTY)
            .sound(SoundActions.FLUID_VAPORIZE, SoundEvents.BLOCK_FIRE_EXTINGUISH)
            .pathType(PathNodeType.WATER)
            .adjacentPathType(PathNodeType.WATER)) {

        @Override
        public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
            consumer.accept(new IClientFluidTypeExtensions() {
                private static final Identifier IRIDESCENCE = InfinityMod.getId("block/iridescence");

                @Override
                public @NotNull Identifier getStillTexture() {
                    return IRIDESCENCE;
                }

                @Override
                public @NotNull Identifier getFlowingTexture() {
                    return IRIDESCENCE;
                }

                @Override
                public int getTintColor(@NotNull FluidState state, @NotNull BlockRenderView getter, @NotNull BlockPos pos) {
                    return Iridescence.color(pos);
                }
            });
        }
    });

    public static FluidInteractionRegistry.InteractionInformation getIridescentInteraction(FluidType type) {
        return new FluidInteractionRegistry.InteractionInformation(
                (level, currentPos, relativePos, currentState) -> level.getFluidState(relativePos).getFluidType() == type,
                (level, currentPos, relativePos, currentState) -> {
            level.setBlockState(currentPos, ForgeEventFactory.fireFluidPlaceBlockEvent(level, currentPos, currentPos,
                    currentState.isStill() ? Blocks.OBSIDIAN.getDefaultState() :
                            Iridescence.getRandomColorBlock(level,"glazed_terracotta").getDefaultState()));
            level.syncWorldEvent(1501, currentPos, 0);
        });
    }

    public static void registerFluidTypes(IEventBus bus) {
        FLUID_TYPES.register(bus);
    }
    public static void registerFluidInteractions(FMLCommonSetupEvent event) {
        FluidInteractionRegistry.addInteraction(ForgeMod.LAVA_TYPE.get(), getIridescentInteraction(IRIDESCENCE_TYPE.get()));
    }

    public static class IridescentFluidType extends FluidType {
        public IridescentFluidType(Properties properties) {
            super(properties);
        }
        @Override
        public PathNodeType getBlockPathType(@NotNull FluidState state, @NotNull BlockView level, @NotNull BlockPos pos,
                                             @Nullable MobEntity mob, boolean canFluidLog) {
            return canFluidLog ? super.getBlockPathType(state, level, pos, mob, true) : null;
        }
        @Override
        public boolean canConvertToSource(@NotNull FluidState state, @NotNull WorldView reader, @NotNull BlockPos pos) {
            if (reader instanceof World level) {
                return Iridescence.isInfinite(level);
            }
            //Best guess fallback to default (true)
            return super.canConvertToSource(state, reader, pos);
        }
        @Override
        public boolean isVaporizedOnPlacement(@NotNull World w, @NotNull BlockPos pos, @NotNull FluidStack stack) {
            return false;
        }
    }
}
