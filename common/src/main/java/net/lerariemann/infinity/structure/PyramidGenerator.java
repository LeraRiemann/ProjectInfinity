package net.lerariemann.infinity.structure;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.structure.ShiftableStructurePiece;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;
import net.minecraft.world.gen.structure.Structure;
import net.minecraft.util.math.random.Random;

public class PyramidGenerator extends ShiftableStructurePiece {
    int y, y0;
    BlockStateProvider b;

    PyramidGenerator(Structure.Context context, int top_y, int bottom_y, BlockStateProvider blockState) {
        super(ModStructureType.PYRAMID_PIECE.get(), context.chunkPos().getStartX(), bottom_y, context.chunkPos().getStartZ(),
                2*(top_y-bottom_y)+1, top_y-bottom_y, 2*(top_y-bottom_y)+1, Direction.EAST);
        y = top_y;
        y0 = bottom_y;
        b = blockState;
    }

    public PyramidGenerator(NbtCompound nbt) {
        super(ModStructureType.PYRAMID_PIECE.get(), nbt);
    }

    @Override
    public void generate(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox box, ChunkPos chunkPos, BlockPos pivot) {
        int size = y-y0;
        for(int j = 0; j <= size; j++) {
            for (int i = j; i <= 2*size - j; ++i) {
                for (int k = j; k <= 2*size - j; ++k) {
                    if (this.getBlockAt(world, i, j, k, box).isAir() || this.getBlockAt(world, i, j, k, box).isOf(Blocks.WATER)) {
                        BlockState state;
                        if (b == null) state = Blocks.BRICKS.getDefaultState();
                        else state = b.get(random, new BlockPos(i, j, k));
                        this.addBlock(world, state, i, j, k, box);
                    }
                }
            }
        }
    }
}
