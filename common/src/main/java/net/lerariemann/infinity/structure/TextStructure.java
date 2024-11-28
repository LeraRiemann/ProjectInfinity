package net.lerariemann.infinity.structure;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.lerariemann.infinity.util.TextData;
import net.minecraft.structure.StructurePiecesCollector;
import net.minecraft.util.math.BlockPos;
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
            (Codec.INT.fieldOf("orientation")).orElse(1).forGetter(a -> a.ori),
            (Codec.INT.fieldOf("char_spacing")).orElse(1).forGetter(a -> a.char_spacing),
            (Codec.INT.fieldOf("line_spacing")).orElse(1).forGetter(a -> a.line_spacing)).apply(instance, TextStructure::new));
    String text;
    BlockStateProvider block;
    int ori;
    int line_spacing;
    int char_spacing;
    TextData data;

    TextStructure(Structure.Config config, String text, BlockStateProvider block, int ori, int line_spacing, int char_spacing) {
        super(config);
        this.text = text;
        this.block = block;
        this.ori = ori;
        this.line_spacing = line_spacing;
        this.char_spacing = char_spacing;
        this.data = TextData.genData(char_spacing, 1000, text);
    }

    @Override
    public Optional<StructurePosition> getStructurePosition(Context context) {
        return PyramidStructure.getStructurePosition(context, Heightmap.Type.WORLD_SURFACE_WG, collector -> addPieces(collector, context));
    }

    @Override
    public StructureType<?> getType() {
        return ModStructureTypes.TEXT.get();
    }

    private void addPieces(StructurePiecesCollector collector, Structure.Context context) {
        BlockPos p = context.chunkPos().getStartPos().up(90);
        int linediff = (8 + line_spacing);
        int maxsize = 192;
        int len = -maxsize;
        int line = -maxsize;
        int i;
        for (i = 0; i < text.length(); i++) {
            Character c = text.charAt(i);
            List<Integer> lst = TextData.storage.get(text.charAt(i));
            if (lst == null) {
                continue;
            }
            if (c.equals('$') && i+1 < text.length() && ((Character) text.charAt(i+1)).equals('n')) {
                i+=1;
                line+=linediff;
                len = -maxsize;
                continue;
            }
            BlockPos letterOrigin = TextData.mutate(p, ori, line, len);
            collector.addPiece(LetterPiece.of(letterOrigin, ori, c, block));
            len += lst.size() + char_spacing;
            if (len > maxsize) {
                len = -maxsize;
                line+=linediff;
            }
            if (line > maxsize) {
                break;
            }
        }
    }
}
