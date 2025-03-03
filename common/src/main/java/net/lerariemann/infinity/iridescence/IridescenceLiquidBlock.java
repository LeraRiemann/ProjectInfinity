package net.lerariemann.infinity.iridescence;

import dev.architectury.core.block.ArchitecturyLiquidBlock;
import net.lerariemann.infinity.registry.core.ModItemFunctions;
import net.lerariemann.infinity.util.core.RandomProvider;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Optional;
import java.util.function.Supplier;

public class IridescenceLiquidBlock extends ArchitecturyLiquidBlock {
    public IridescenceLiquidBlock(Supplier<? extends FlowableFluid> fluid, Settings properties) {
        super(fluid, properties);
    }

    @Override
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        super.onEntityCollision(state, world, pos, entity);
        if (world.getFluidState(pos).getLevel() > 3 && world instanceof ServerWorld w) switch (entity) {
            case PlayerEntity player -> Iridescence.tryBeginJourney(player, RandomProvider.ruleInt("iridescenceContactLevel"), false);
            case MobEntity ent -> Iridescence.tryApplyEffect(ent);
            case ItemEntity item -> {
                if (!Iridescence.isIridescentItem(item.getStack()) && item.getOwner() instanceof LivingEntity le &&
                !Iridescence.getPhase(le).equals(Iridescence.Phase.INITIAL))
                    ModItemFunctions.checkCollisionRecipes(w, item, ModItemFunctions.IRIDESCENCE_CRAFTING_TYPE.get(),
                        i -> Optional.empty());
            }
            default -> {}
        }
    }
}
