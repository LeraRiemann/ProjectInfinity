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
    Direction dir;
    TextData.Polarization pol;

    protected LetterPiece(StructurePieceType type, int x, int y, int z, int width, int height, int depth,
                          Direction dir, TextData.Polarization pol, List<Integer> letter, BlockStateProvider block) {
        super(type, x, y, z, width, height, depth, Direction.SOUTH);
        this.letter = letter;
        this.block = block;
        this.dir = dir;
        this.pol = pol;
    }

    public LetterPiece(NbtCompound nbt) {
        super(ModStructureTypes.LETTER.get(), nbt);
    }

    static LetterPiece of(BlockPos startcorner, Direction dir, TextData.Polarization pol, List<Integer> letter, BlockStateProvider block) {
        BlockPos dims = getDimensions(pol, dir, letter.size());
        int min_x, min_y, min_z;
        min_x = Math.min(startcorner.getX(), startcorner.getX() + dims.getX());
        min_y = Math.min(startcorner.getY(), startcorner.getY() + dims.getY());
        min_z = Math.min(startcorner.getZ(), startcorner.getZ() + dims.getZ());
        dims = abs(dims);
        return new LetterPiece(ModStructureTypes.LETTER.get(),
                min_x, min_y, min_z,
                dims.getX(), dims.getY(), dims.getZ(),
                dir, pol, letter, block);
    }

    static BlockPos getDimensions(TextData.Polarization pol, Direction dir, int letterwidth) {
        return TextData.offset(8, letterwidth, pol, dir);
    }

    static BlockPos abs(BlockPos dims) {
        return new BlockPos(Math.abs(dims.getX()), Math.abs(dims.getY()), Math.abs(dims.getZ()));
    }

    static BlockPos getRelativeOffset(BlockPos offset, BlockPos dims) {
        return new BlockPos(
                offset.getX() > 0 ? offset.getX() : offset.getX() + dims.getX(),
                offset.getY() > 0 ? offset.getY() : offset.getY() + dims.getY(),
                offset.getZ() > 0 ? offset.getZ() : offset.getZ() + dims.getZ()
        );
    }

    @Override
    public void generate(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox chunkBox, ChunkPos chunkPos, BlockPos pivot) {
        if (letter == null) return;
        int j, k;
        BlockPos dims = abs(getDimensions(pol, dir, letter.size()));
        for (j = 0; j < letter.size(); j++) {
            for (k = 0; k < 8; k++) {
                BlockPos bp = getRelativeOffset(TextData.offset(k, j, pol, dir), dims);
                this.addBlock(world,
                        ((letter.get(j) >> k)%2 == 1) ? block.get(random, bp) : Blocks.AIR.getDefaultState(),
                        bp.getX(), bp.getY(), bp.getZ(), chunkBox);
            }
        }
    }
}
