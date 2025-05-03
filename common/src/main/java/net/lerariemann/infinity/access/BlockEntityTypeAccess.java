package net.lerariemann.infinity.access;

import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;

import java.util.Set;

public interface BlockEntityTypeAccess<T extends BlockEntity> {
    BlockEntityType.BlockEntityFactory<? extends T> infinity$getFactory();
    Set<Block> infinity$getBlocks();
}
