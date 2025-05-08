package net.lerariemann.infinity.mixin.qol;

import net.lerariemann.infinity.access.BlockEntityTypeAccess;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Set;

@Mixin(BlockEntityType.class)
public class BlockEntityTypeMixin<T extends BlockEntity> implements BlockEntityTypeAccess<T> {
    @Shadow @Final private BlockEntityType.BlockEntityFactory<? extends T> factory;

    @Shadow @Final private Set<Block> blocks;

    @Override
    public BlockEntityType.BlockEntityFactory<? extends T> infinity$getFactory() {
        return factory;
    }

    @Override
    public Set<Block> infinity$getBlocks() {
        return blocks;
    }
}
