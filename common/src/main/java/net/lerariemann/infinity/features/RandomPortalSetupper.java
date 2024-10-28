package net.lerariemann.infinity.features;

import com.mojang.serialization.Codec;
import net.lerariemann.infinity.block.ModBlocks;
import net.lerariemann.infinity.block.entity.NeitherPortalBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.NetherPortalBlock;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.util.FeatureContext;

import java.util.Set;

public class RandomPortalSetupper extends Feature<RandomPortalSetupperConfig> {
    public RandomPortalSetupper(Codec<RandomPortalSetupperConfig> configCodec) {
        super(configCodec);
    }

    @Override
    public boolean generate(FeatureContext<RandomPortalSetupperConfig> context) {
        StructureWorldAccess structureWorldAccess = context.getWorld();
        BlockPos blockPos = context.getOrigin();
        BlockPos bp2 = new BlockPos(blockPos.getX(), context.getConfig().y(), blockPos.getZ());
        Random random = context.getRandom();
        boolean axis_x = context.getConfig().axis_x();
        boolean bl = true;
        for (int i: Set.of(0, context.getConfig().offset_l())) for (int j: Set.of(0, context.getConfig().offset_t())) {
            bl = bl && generateOnePortal(structureWorldAccess,
                    axis_x ? bp2.add(i, 0, j) : bp2.add(j, 0, i),
                    random, axis_x);
        }
        return bl;
    }

    final static BlockState obs = Blocks.OBSIDIAN.getDefaultState();
    final static BlockState sign = Blocks.OAK_HANGING_SIGN.getDefaultState();

    public static BlockPos bpadd(BlockPos bp, int l, int y, int t, boolean axisx) {
        return axisx ? bp.add(l, y, t) : bp.add(t, y, l);
    }

    public boolean generateOnePortal(StructureWorldAccess structureWorldAccess, BlockPos blockPos, Random random, boolean axis_x) {
        int dim = random.nextInt();
        for (int y = 0; y < 5; y++) {
            if (y == 0 || y == 4) for (int l = 0; l < 5; l++) {
                setBlockState(structureWorldAccess, bpadd(blockPos, l, y, 0, axis_x), obs);
            }
            else {
                setBlockState(structureWorldAccess, blockPos.add(0, y, 0), obs);
                setBlockState(structureWorldAccess, bpadd(blockPos, 4, y, 0, axis_x), obs);
                for (int l = 1; l < 4; l++) {
                    BlockPos pos = bpadd(blockPos, l, y, 0, axis_x);
                    setBlockState(structureWorldAccess, pos,
                            ModBlocks.NEITHER_PORTAL.get().getDefaultState().with(
                                    NetherPortalBlock.AXIS, axis_x ? Direction.Axis.X : Direction.Axis.Z));
                    if (structureWorldAccess.getBlockEntity(pos) instanceof NeitherPortalBlockEntity be) {
                        be.setDimension(dim);
                        be.setOpen(false);
                    }
                    else return false;
                }
            }
        }
        for (int i: Set.of(1, -1)) setBlockState(structureWorldAccess,
                bpadd(blockPos, 2, 4, i, axis_x),
                axis_x ? sign : sign.with(Properties.ROTATION, 4));
        return true;
    }
}
