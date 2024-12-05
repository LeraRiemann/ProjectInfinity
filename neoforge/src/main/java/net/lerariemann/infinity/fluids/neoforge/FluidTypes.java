package net.lerariemann.infinity.fluids.neoforge;

import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.iridescence.Iridescence;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.FluidState;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.fluids.FluidInteractionRegistry;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jetbrains.annotations.NotNull;

public class FluidTypes {
    private static final DeferredRegister<FluidType> FLUID_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, InfinityMod.MOD_ID);

    public static final DeferredHolder<FluidType, FluidType> IRIDESCENCE_TYPE = FLUID_TYPES.register("iridescence",
            () -> new FluidType(FluidType.Properties.create()
            .descriptionId("fluid.infinity.iridescence")
            .fallDistanceModifier(0F)
            .canExtinguish(true)
            .canConvertToSource(true)
            .supportsBoating(true)
            .canSwim(true)
            .sound(SoundActions.BUCKET_FILL, SoundEvents.ITEM_BUCKET_FILL)
            .sound(SoundActions.BUCKET_EMPTY, SoundEvents.ITEM_BUCKET_EMPTY)
            .sound(SoundActions.FLUID_VAPORIZE, SoundEvents.BLOCK_FIRE_EXTINGUISH)
            .canHydrate(true)) {
        @Override
        public boolean canConvertToSource(@NotNull FluidState state, @NotNull WorldView reader, @NotNull BlockPos pos) {
            if (reader instanceof World level) {
                return Iridescence.isInfinite(level);
            }
            //Best guess fallback to default (true)
            return super.canConvertToSource(state, reader, pos);
        }
    });

    public static FluidInteractionRegistry.InteractionInformation getIridescentInteraction(FluidType type) {
        return new FluidInteractionRegistry.InteractionInformation(
                (level, currentPos, relativePos, currentState) -> level.getFluidState(relativePos).getFluidType() == type,
                (level, currentPos, relativePos, currentState) -> {
            level.setBlockState(currentPos, EventHooks.fireFluidPlaceBlockEvent(level, currentPos, currentPos,
                    currentState.isStill() ? Blocks.OBSIDIAN.getDefaultState() :
                            Iridescence.getRandomColorBlock(level,"glazed_terracotta").getDefaultState()));
            level.syncWorldEvent(1501, currentPos, 0);
        });
    }

    public static void registerFluidTypes(IEventBus bus) {
        FLUID_TYPES.register(bus);
    }
    public static void registerFluidInteractions(FMLCommonSetupEvent event) {
        FluidInteractionRegistry.addInteraction(NeoForgeMod.LAVA_TYPE.value(), getIridescentInteraction(IRIDESCENCE_TYPE.value()));
    }
}
