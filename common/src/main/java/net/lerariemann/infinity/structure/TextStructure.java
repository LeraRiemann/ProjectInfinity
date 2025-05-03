package net.lerariemann.infinity.structure;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.lerariemann.infinity.util.var.TextData;
import net.lerariemann.infinity.registry.core.ModStructureTypes;
import net.minecraft.structure.StructurePiecesCollector;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.Heightmap;
import net.minecraft.world.gen.HeightContext;
import net.minecraft.world.gen.heightprovider.HeightProvider;
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
            (Codec.INT.fieldOf("polarization")).orElse(2).forGetter(a -> a.pol.id),
            (Codec.STRING.fieldOf("direction")).orElse("random").forGetter(a -> a.dir),
            (Codec.INT.fieldOf("char_spacing")).orElse(1).forGetter(a -> a.char_spacing),
            (Codec.INT.fieldOf("line_spacing")).orElse(1).forGetter(a -> a.line_spacing),
            (HeightProvider.CODEC.fieldOf("y")).forGetter(a -> a.y_provider)).apply(instance, TextStructure::new));
    String text;
    BlockStateProvider block;
    String dir;
    int line_spacing;
    int char_spacing;
    HeightProvider y_provider;
    TextData.Polarization pol;


    TextStructure(Structure.Config config, String text, BlockStateProvider block, int pol, String dir,
                  int line_spacing, int char_spacing, HeightProvider y_provider) {
        super(config);
        this.text = text;
        this.block = block;
        this.dir = dir;
        this.pol = TextData.Polarization.of(pol);
        this.line_spacing = line_spacing;
        this.char_spacing = char_spacing;
        this.y_provider = y_provider;
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
        BlockPos centerPos = context.chunkPos().getStartPos().add(8, 0, 8);
        int new_y = y_provider.get(context.random(), new HeightContext(context.chunkGenerator(), context.world()));
        centerPos = centerPos.up(new_y - centerPos.getY());
        int maxsize = 119;
        TextData data = TextData.genData(char_spacing, 2*maxsize, text);

        int line_height = 8+line_spacing;
        int length = Math.min(2*maxsize, data.longest_line());
        int height = Math.min(2*maxsize, line_height*data.getLineCount());

        Direction dir = getDir(context);
        int ori = TextData.getOri(pol, dir);
        for (int i = 0; i < data.getLineCount(); i++) {
            for (int j = 0; j < data.getLineLen(i); j++) {
                int line = -height/2 + i*line_height;
                if (line > maxsize) {
                    break;
                }
                int len = -length/2 + data.offsetMap().get(i).get(j);
                List<Integer> lst = TextData.storage.get(data.charMap().get(i).get(j));
                if (lst == null) {
                    continue;
                }
                BlockPos letterOrigin = TextData.mutate(centerPos, ori, line, len);
                collector.addPiece(LetterPiece.of(letterOrigin, ori, lst, block));
            }
        }
    }
}
