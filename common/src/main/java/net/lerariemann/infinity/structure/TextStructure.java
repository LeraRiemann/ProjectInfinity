package net.lerariemann.infinity.structure;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.lerariemann.infinity.util.TextData;
import net.minecraft.structure.StructurePiecesCollector;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.Heightmap;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;
import net.minecraft.world.gen.structure.Structure;
import net.minecraft.world.gen.structure.StructureType;

import java.util.List;
import java.util.Optional;

public class TextStructure extends Structure {
    public static final MapCodec<TextStructure> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            TextStructure.configCodecBuilder(instance),
            (Codec.STRING.fieldOf("text")).forGetter(a -> a.text),
            (BlockStateProvider.TYPE_CODEC.fieldOf("block")).forGetter(a -> a.block),
            (Codec.INT.fieldOf("polarization")).orElse(0).forGetter(a -> a.pol.id),
            (Codec.STRING.fieldOf("direction")).orElse("E").forGetter(a -> a.dir),
            (Codec.INT.fieldOf("char_spacing")).orElse(1).forGetter(a -> a.char_spacing),
            (Codec.INT.fieldOf("line_spacing")).orElse(1).forGetter(a -> a.line_spacing)).apply(instance, TextStructure::new));
    String text;
    BlockStateProvider block;
    String dir;
    int line_spacing;
    int char_spacing;
    TextData.Polarization pol;

    TextStructure(Structure.Config config, String text, BlockStateProvider block, int ori, String dir, int line_spacing, int char_spacing) {
        super(config);
        this.text = text;
        this.block = block;
        this.dir = dir;
        this.pol = TextData.Polarization.of(ori);
        this.line_spacing = line_spacing;
        this.char_spacing = char_spacing;
    }

    public Direction getDir(Structure.Context context) {
        return switch (dir) {
            case "N" -> Direction.NORTH;
            case "S" -> Direction.SOUTH;
            case "W" -> Direction.WEST;
            case "E" -> Direction.EAST;
            default -> Direction.Type.HORIZONTAL.random(context.random());
        };
    }

    @Override
    public Optional<StructurePosition> getStructurePosition(Context context) {
        return getStructurePosition(context, Heightmap.Type.WORLD_SURFACE_WG, collector -> addPieces(collector, context));
    }

    @Override
    public StructureType<?> getType() {
        return ModStructureTypes.TEXT.get();
    }

    private void addPieces(StructurePiecesCollector collector, Structure.Context context) {
        BlockPos p = context.chunkPos().getStartPos();
        int y = context.chunkGenerator().getHeightInGround(p.getX(), p.getZ(),
                Heightmap.Type.WORLD_SURFACE_WG, context.world(), context.noiseConfig());
        if (context.world().getTopY() - y < 16) y = context.world().getBottomY() + context.world().getHeight()/2;
        p = p.up(y - p.getY());
        int maxsize = 196;
        TextData data = TextData.genData(char_spacing, maxsize, text);
        for (int i = 0; i < data.getLines(); i++) {
            for (int j = 0; j < data.getLineLen(i); j++) {
                int line = i*(8+line_spacing);
                if (line > maxsize) {
                    break;
                }
                int len = data.offsetMap().get(i).get(j);
                List<Integer> lst = TextData.storage.get(data.charMap().get(i).get(j));
                if (lst == null) {
                    continue;
                }
                Direction dir = getDir(context);
                BlockPos letterOrigin = p.add(TextData.offset(line, len, pol, dir));
                collector.addPiece(LetterPiece.of(letterOrigin, dir, pol, lst, block));
            }
        }
    }
}
