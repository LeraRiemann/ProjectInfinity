package net.lerariemann.infinity.structure;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.lerariemann.infinity.registry.core.ModStructureTypes;
import net.minecraft.structure.StructurePiecesCollector;
import net.minecraft.world.Heightmap;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;
import net.minecraft.world.gen.structure.Structure;
import net.minecraft.world.gen.structure.StructureType;

import java.util.Optional;

public class PyramidStructure extends Structure {
    public static final MapCodec<PyramidStructure> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            PyramidStructure.configCodecBuilder(instance),
            (Codec.INT.fieldOf("top_y")).forGetter(a -> a.top_y),
            (Codec.INT.fieldOf("bottom_y")).forGetter(a -> a.top_y),
            (BlockStateProvider.TYPE_CODEC.fieldOf("block")).forGetter(a -> a.block)).apply(instance, PyramidStructure::new));
    int top_y;
    int bottom_y;
    BlockStateProvider block;

    PyramidStructure(Structure.Config config, int i, int j, BlockStateProvider p) {
        super(config);
        top_y = i;
        bottom_y = j;
        block = p;
    }

    @Override
    public Optional<StructurePosition> getStructurePosition(Structure.Context context) {
        return PyramidStructure.getStructurePosition(context, Heightmap.Type.WORLD_SURFACE_WG, collector -> addPieces(collector, context));
    }

    private void addPieces(StructurePiecesCollector collector, Structure.Context context) {
        collector.addPiece(new PyramidGenerator(context, top_y, bottom_y, block));
    }

    @Override
    public StructureType<?> getType() {
        return ModStructureTypes.PYRAMID.get();
    }
}
