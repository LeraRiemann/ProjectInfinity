package net.lerariemann.infinity.features;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.lerariemann.infinity.registry.core.ModBlocks;
import net.lerariemann.infinity.block.entity.InfinityPortalBlockEntity;
import net.lerariemann.infinity.util.InfinityMethods;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.NetherPortalBlock;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.FeatureConfig;
import net.minecraft.world.gen.feature.util.FeatureContext;

import java.util.HashSet;
import java.util.Set;

public class RandomPortalSetupper extends Feature<RandomPortalSetupper.Config> {
    public RandomPortalSetupper(Codec<Config> configCodec) {
        super(configCodec);
    }


    public static Set<Integer> tileChunkPositions(int start, int offset) {
        Set<Integer> ls = new HashSet<>();
        int mod = InfinityMethods.properMod(start, offset);
        int d_start = mod == 0 ? 0 : offset - mod;
        for (int d_curr = d_start; d_curr < 16; d_curr += offset) ls.add(start + d_curr);
        return ls;
    }

    @Override
    public boolean generate(FeatureContext<Config> context) {
        StructureWorldAccess structureWorldAccess = context.getWorld();
        BlockPos blockPos = context.getOrigin();
        int y = context.getConfig().y();
        Random random = context.getRandom();
        boolean axis_x = context.getConfig().axis_x();

        int width = context.getConfig().width();
        int sol = context.getConfig().sign_offset_l();
        if (sol == 0) sol = (width + 1) / 2;
        int height = context.getConfig().height();
        int soy = context.getConfig().sign_offset_y();
        if (y == 0) soy = height + 1;

        Set<Integer> ls = tileChunkPositions(axis_x ? blockPos.getX() : blockPos.getZ(), context.getConfig().offset_l());
        Set<Integer> ts = tileChunkPositions(axis_x ? blockPos.getZ() : blockPos.getX(), context.getConfig().offset_t());
        boolean bl = !ls.isEmpty() && !ts.isEmpty();
        for (int i : ls) for (int j : ts) {
            bl = bl && generateOnePortal(structureWorldAccess,
                    axis_x ? new BlockPos(i, y, j) : new BlockPos(j, 0, i),
                    random, axis_x, width, height, sol, soy);

        }
        return bl;
    }

    final static BlockState obs = Blocks.OBSIDIAN.getDefaultState();
    final static BlockState sign = Blocks.OAK_HANGING_SIGN.getDefaultState();

    public static BlockPos bpadd(BlockPos bp, int l, int y, int t, boolean axis_x) {
        return axis_x ? bp.add(l, y, t) : bp.add(t, y, l);
    }

    public boolean generateOnePortal(StructureWorldAccess structureWorldAccess, BlockPos blockPos, Random random,
                                     boolean axis_x, int width, int height, int sol, int soy) {
        long dim = InfinityMethods.getRandomSeed(random);
        for (int y = 0; y < height+2; y++) {
            if (y == 0 || y == height+1) for (int l = 0; l < width+2; l++) {
                setBlockState(structureWorldAccess, bpadd(blockPos, l, y, 0, axis_x), obs);
            }
            else {
                setBlockState(structureWorldAccess, blockPos.add(0, y, 0), obs);
                setBlockState(structureWorldAccess, bpadd(blockPos, width+1, y, 0, axis_x), obs);
                for (int l = 1; l < width+1; l++) {
                    BlockPos pos = bpadd(blockPos, l, y, 0, axis_x);
                    setBlockState(structureWorldAccess, pos,
                            ModBlocks.PORTAL.get().getDefaultState().with(
                                    NetherPortalBlock.AXIS, axis_x ? Direction.Axis.X : Direction.Axis.Z));
                    if (structureWorldAccess.getBlockEntity(pos) instanceof InfinityPortalBlockEntity be) {
                        be.setDimension(dim);
                        be.setOpen(false);
                        be.markDirty();
                    }
                    else return false;
                }
            }
        }
        for (int i: Set.of(1, -1)) setBlockState(structureWorldAccess,
                bpadd(blockPos, sol, soy, i, axis_x),
                axis_x ? sign : sign.with(Properties.ROTATION, 4));
        return true;
    }

    public record Config(boolean axis_x, int width, int height, int offset_l, int offset_t,
                                              int sign_offset_l, int sign_offset_y, int y) implements FeatureConfig {
        public static final Codec<Config> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                (Codec.BOOL.fieldOf("axis_x")).orElse(Boolean.TRUE).forGetter(a -> a.axis_x),
                (Codec.INT.fieldOf("width")).orElse(2).forGetter(a -> a.width),
                (Codec.INT.fieldOf("height")).orElse(3).forGetter(a -> a.height),
                (Codec.INT.fieldOf("offset_l")).orElse(8).forGetter(a -> a.offset_l),
                (Codec.INT.fieldOf("offset_t")).orElse(8).forGetter(a -> a.offset_t),
                (Codec.INT.fieldOf("sign_offset_l")).orElse(0).forGetter(a -> a.sign_offset_l),
                (Codec.INT.fieldOf("sign_offset_y")).orElse(0).forGetter(a -> a.sign_offset_y),
                (Codec.INT.fieldOf("y")).orElse(16).forGetter(a -> a.y)).apply(
                instance, Config::new));
    }
}
