package net.lerariemann.infinity.structure;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.structure.StructurePiecesList;
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.structure.pool.StructurePoolBasedGenerator;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.HeightContext;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.heightprovider.HeightProvider;
import net.minecraft.world.gen.structure.Structure;
import net.minecraft.world.gen.structure.StructureType;

import java.util.Optional;

public class RandomPortalStructure extends Structure {
    public static final Codec<RandomPortalStructure> CODEC = Codecs.validate(
            RecordCodecBuilder.mapCodec(instance -> instance.group(RandomPortalStructure.configCodecBuilder(instance),
                    (StructurePool.REGISTRY_CODEC.fieldOf("start_pool")).forGetter(structure -> structure.startPool),
                    Identifier.CODEC.optionalFieldOf("start_jigsaw_name").forGetter(structure -> structure.startJigsawName),
                    (Codec.intRange(0, 7).fieldOf("size")).forGetter(structure -> structure.size),
                    (HeightProvider.CODEC.fieldOf("start_height")).forGetter(structure -> structure.startHeight),
                    (Codec.BOOL.fieldOf("use_expansion_hack")).forGetter(structure -> structure.useExpansionHack),
                    Heightmap.Type.CODEC.optionalFieldOf("project_start_to_heightmap").forGetter(structure -> structure.projectStartToHeightmap),
                    (Codec.intRange(1, 128).fieldOf("max_distance_from_center")).forGetter(structure -> structure.maxDistanceFromCenter)).apply(
                    instance, RandomPortalStructure::new)), RandomPortalStructure::validate).codec();
    private final RegistryEntry<StructurePool> startPool;
    private final Optional<Identifier> startJigsawName;
    private final int size;
    private final HeightProvider startHeight;
    private final boolean useExpansionHack;
    private final Optional<Heightmap.Type> projectStartToHeightmap;
    private final int maxDistanceFromCenter;
    BlockPos pos;

    private static DataResult<RandomPortalStructure> validate(RandomPortalStructure structure) {
        int i;
        switch (structure.getTerrainAdaptation()) {
            default -> throw new IncompatibleClassChangeError();
            case NONE -> i = 0;
            case BURY, BEARD_THIN, BEARD_BOX -> i = 12;
        }
        if (structure.maxDistanceFromCenter + i > 128) {
            return DataResult.error(() -> "Structure size including terrain adaptation must not exceed 128");
        }
        return DataResult.success(structure);
    }

    public RandomPortalStructure(Structure.Config config, RegistryEntry<StructurePool> startPool, Optional<Identifier> startJigsawName, int size, HeightProvider startHeight, boolean useExpansionHack, Optional<Heightmap.Type> projectStartToHeightmap, int maxDistanceFromCenter) {
        super(config);
        this.startPool = startPool;
        this.startJigsawName = startJigsawName;
        this.size = size;
        this.startHeight = startHeight;
        this.useExpansionHack = useExpansionHack;
        this.projectStartToHeightmap = projectStartToHeightmap;
        this.maxDistanceFromCenter = maxDistanceFromCenter;
    }

    @Override
    public Optional<Structure.StructurePosition> getStructurePosition(Structure.Context context) {
        ChunkPos chunkPos = context.chunkPos();
        int i = this.startHeight.get(context.random(), new HeightContext(context.chunkGenerator(), context.world()));
        BlockPos blockPos = new BlockPos(chunkPos.getStartX(), i, chunkPos.getStartZ());
        Optional<Structure.StructurePosition> s = StructurePoolBasedGenerator.generate(context, this.startPool, this.startJigsawName, this.size, blockPos, this.useExpansionHack, this.projectStartToHeightmap, this.maxDistanceFromCenter);
        pos = s.isPresent() ? s.get().position() : blockPos;
        return s;
    }

    @Override
    public StructureType<?> getType() {
        return ModStructureType.PORTAL;
    }

    @Override
    public void postPlace(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox box, ChunkPos chunkPos, StructurePiecesList pieces) {
        Box b = Box.from(pieces.getBoundingBox());
        try {
            if (!world.isClient() && world.toServerWorld().getEntitiesByType(TypeFilter.instanceOf(ItemEntity.class), b, RandomPortalStructure::isBook).isEmpty()) {
                ItemStack stack = ItemStack.fromNbt(StringNbtReader.parse(
                        "{id:\"minecraft:written_book\", Count:1b, tag:{author:\"LeraRiemann\",filtered_title:\"" + pos + "\",pages:['{\"text\":\"" + pos + "\"}'],title:\"" + pos + "\"}}"));
                Entity e = new ItemEntity(world.toServerWorld(), b.getCenter().x, b.getCenter().y + 1.0, b.getCenter().z, stack, 0.0, 0.0, 0.0);
                world.spawnEntityAndPassengers(e);
            }
        } catch (CommandSyntaxException e) {
            throw new RuntimeException(e);
        }
    }
    public static boolean isBook(ItemEntity entity) {
        return entity.getStack().getItem() == Items.WRITTEN_BOOK || entity.getStack().getItem() == Items.WRITABLE_BOOK;
    }
}
