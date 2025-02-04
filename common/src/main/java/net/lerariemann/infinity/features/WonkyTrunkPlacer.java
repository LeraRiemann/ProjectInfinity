package net.lerariemann.infinity.features;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.lerariemann.infinity.registry.core.ModFeatures;
import net.minecraft.block.BlockState;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.TestableWorld;
import net.minecraft.world.gen.feature.TreeFeatureConfig;
import net.minecraft.world.gen.foliage.FoliagePlacer;
import net.minecraft.world.gen.trunk.TrunkPlacer;
import net.minecraft.world.gen.trunk.TrunkPlacerType;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class WonkyTrunkPlacer extends TrunkPlacer {
    public static final MapCodec<WonkyTrunkPlacer> CODEC = RecordCodecBuilder.mapCodec(
            instance -> fillTrunkPlacerFields(instance)
                    .and(
                            instance.group(
                                    Codecs.POSITIVE_FLOAT.optionalFieldOf("weight_up", 1f).forGetter(a -> a.weightUp),
                                    Codecs.POSITIVE_FLOAT.optionalFieldOf("weight_down", 0.1f).forGetter(a -> a.weightDown),
                                    Codecs.POSITIVE_FLOAT.optionalFieldOf("weight_side", 0.5f).forGetter(a -> a.weightSide)
                            )
                    )
                    .apply(instance, WonkyTrunkPlacer::new));
    final float weightUp;
    final float weightDown;
    final float weightSide;
    Direction currentDir;

    public WonkyTrunkPlacer(int baseHeight, int firstRandomHeight, int secondRandomHeight, float weightUp, float weightDown, float weightSide) {
        super(baseHeight, firstRandomHeight, secondRandomHeight);
        this.weightUp = Math.max(weightUp, 1f);
        this.weightDown = Math.max(weightDown, 1f);
        this.weightSide = Math.max(weightDown, 1f);
        currentDir = Direction.UP;
    }

    @Override
    protected TrunkPlacerType<?> getType() {
        return ModFeatures.WONKY_TRUNK.get();
    }

    @Override
    public List<FoliagePlacer.TreeNode> generate(TestableWorld world, BiConsumer<BlockPos, BlockState> replacer, Random random, int height, BlockPos startPos, TreeFeatureConfig config) {
        setToDirt(world, replacer, random, startPos.down(), config);
        BlockPos curr = startPos;
        this.getAndSetState(world, replacer, random, startPos, config);
        for (int i = 0; i < height; i++) {
            double d = random.nextDouble() * (weightUp + weightDown + weightSide);
            Direction dir;
            if (d < weightUp) dir = Direction.UP;
            else if (d < weightUp + weightDown) dir = Direction.DOWN;
            else {
                dir = switch (random.nextInt(4)) {
                    case 1 -> Direction.WEST;
                    case 2 -> Direction.EAST;
                    case 3 -> Direction.NORTH;
                    default -> Direction.SOUTH;
                };
            }
            if (!this.canReplace(world, curr.offset(dir))) dir = Direction.UP;
            curr = curr.offset(dir);
            currentDir = dir;
            this.getAndSetState(world, replacer, random, curr, config);
        }
        return ImmutableList.of(new FoliagePlacer.TreeNode(curr, 0, false));
    }

    @Override


    protected boolean getAndSetState(
            TestableWorld world,
            BiConsumer<BlockPos, BlockState> replacer,
            Random random,
            BlockPos pos,
            TreeFeatureConfig config,
            Function<BlockState, BlockState> function
    ) {
        if (this.canReplace(world, pos)) {
            BlockState bs = function.apply(config.trunkProvider.get(random, pos));
            /*Direction.Axis axis = currentDir.getAxis();
            if (bs.contains(Properties.AXIS)) bs = bs.with(Properties.AXIS, axis);
            if (bs.contains(Properties.HORIZONTAL_AXIS) && axis.isHorizontal()) bs = bs.with(Properties.HORIZONTAL_AXIS, axis);
            if (bs.contains(Properties.FACING)) bs = bs.with(Properties.FACING, currentDir);
            if (bs.contains(Properties.HORIZONTAL_FACING) && axis.isHorizontal()) bs = bs.with(Properties.HORIZONTAL_FACING, currentDir);
            if (bs.contains(Properties.HOPPER_FACING) && currentDir != Direction.UP) bs = bs.with(Properties.HOPPER_FACING, currentDir);*/
            replacer.accept(pos, bs);
            return true;
        } else {
            return false;
        }
    }
}
