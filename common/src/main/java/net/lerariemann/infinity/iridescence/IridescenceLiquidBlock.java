package net.lerariemann.infinity.iridescence;

import dev.architectury.core.block.ArchitecturyLiquidBlock;
import net.lerariemann.infinity.registry.core.ModItemFunctions;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

public class IridescenceLiquidBlock extends ArchitecturyLiquidBlock {
    public IridescenceLiquidBlock(Supplier<? extends FlowableFluid> fluid, Settings properties) {
        super(fluid, properties);
    }

    @Override
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        super.onEntityCollision(state, world, pos, entity);
        if (world.getFluidState(pos).getLevel() > 3 && world instanceof ServerWorld w) {
            if (Objects.requireNonNull(entity) instanceof PlayerEntity player) {
                Iridescence.tryBeginJourney(player, 0, false);
            } else if (entity instanceof MobEntity ent) {
                Iridescence.tryApplyEffect(ent);
            } else if (entity instanceof ItemEntity item) {
                if (!Iridescence.isIridescentItem(item.getStack()) && item.getOwner() instanceof LivingEntity le &&
                        !Iridescence.getPhase(le).equals(Iridescence.Phase.INITIAL))
                    ModItemFunctions.checkCollisionRecipes(w, item, ModItemFunctions.IRIDESCENCE_CRAFTING_TYPE.get(),
                            new NbtCompound());
            }
        }
    }
}