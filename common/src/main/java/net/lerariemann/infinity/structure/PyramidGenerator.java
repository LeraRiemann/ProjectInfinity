package net.lerariemann.infinity.structure;

import net.lerariemann.infinity.registry.core.ModStructureTypes;
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

    public PyramidGenerator(int x, int y, int z, int width, int height, int depth, BlockStateProvider provider) {
        super(ModStructureTypes.PYRAMID_PIECE.get(), x, y, z, width, height, depth, Direction.EAST);
        b = provider;
    }

    public static PyramidGenerator of(Structure.Context context, int top_y, int bottom_y, BlockStateProvider provider) {
        int deltaY = top_y-bottom_y;
        int x = context.chunkPos().getStartX();
        int z = context.chunkPos().getStartZ();
        return new PyramidGenerator(x - deltaY, bottom_y, z - deltaY, 2*deltaY + 1, deltaY, 2*deltaY + 1, provider);
    }

    public PyramidGenerator(NbtCompound nbt) {
        super(ModStructureTypes.PYRAMID_PIECE.get(), nbt);
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
