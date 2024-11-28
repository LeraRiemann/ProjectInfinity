package net.lerariemann.infinity.structure;

import net.lerariemann.infinity.util.TextData;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.structure.ShiftableStructurePiece;
import net.minecraft.structure.StructurePieceType;
import net.minecraft.util.math.*;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;

import java.util.List;

public class LetterPiece extends ShiftableStructurePiece {
    List<Integer> letter;
    BlockStateProvider block;
    int ori;

    protected LetterPiece(StructurePieceType type, int x, int y, int z, int width, int height, int depth,
                          int ori, List<Integer> letter, BlockStateProvider block) {
        super(type, x, y, z, width, height, depth, Direction.SOUTH);
        this.letter = letter;
        this.block = block;
        this.ori = ori;
    }

    public LetterPiece(NbtCompound nbt) {
        super(ModStructureTypes.LETTER.get(), nbt);
    }

    static LetterPiece of(BlockPos startcorner, int ori, Character letter, BlockStateProvider block) {
        List<Integer> l = TextData.storage.get(letter);
        if (l == null) l = TextData.storage.get(' ');
        BlockPos dims = getDimensions(ori, l.size());
        BlockPos othercoord = startcorner.add(dims);
        return new LetterPiece(ModStructureTypes.LETTER.get(),
                Math.min(startcorner.getX(), othercoord.getX()),
                Math.min(startcorner.getY(), othercoord.getY()),
                Math.min(startcorner.getZ(), othercoord.getZ()),
                Math.abs(dims.getX()), Math.abs(dims.getY()), Math.abs(dims.getZ()), ori, l, block);
    }

    static BlockPos getDimensions(int ori, int letterwidth) {
        return TextData.mutate(BlockPos.ORIGIN, ori, 8, letterwidth);
    }

    @Override
    public void generate(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox chunkBox, ChunkPos chunkPos, BlockPos pivot) {
        int j, k;
        if (letter != null) for (j = 0; j < letter.size(); j++) {
            for (k = 0; k < 8; k++) {
                BlockPos bp = TextData.mutate(BlockPos.ORIGIN, ori, k, j);
                this.addBlock(world,
                        ((letter.get(j) >> k)%2 == 1) ? block.get(random, bp) : Blocks.AIR.getDefaultState(),
                        bp.getX(), bp.getY(), bp.getZ(), chunkBox);
            }
        }
    }
}
