package net.lerariemann.infinity.entity.custom;

import net.minecraft.entity.ai.pathing.LandPathNodeMaker;
import net.minecraft.entity.ai.pathing.PathNode;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

public class BishopNodeMaker extends LandPathNodeMaker {
    @Override
    public int getSuccessors(PathNode[] successors, PathNode node) {
        int i = 0;
        int j = 0;
        PathNodeType pathNodeType = this.getNodeType(node.x, node.y + 1, node.z);
        PathNodeType pathNodeType2 = this.getNodeType(node.x, node.y, node.z);
        if (this.entity.getPathfindingPenalty(pathNodeType) >= 0.0F && pathNodeType2 != PathNodeType.STICKY_HONEY) {
            j = MathHelper.floor(Math.max(1.0F, this.entity.getStepHeight()));
        }

        double d = this.getFeetY(new BlockPos(node.x, node.y, node.z));

        PathNode[] succ = new PathNode[Direction.Type.HORIZONTAL.getFacingCount()];
        for (Direction direction : Direction.Type.HORIZONTAL) {
            PathNode pathNode = this.getPathNode(node.x + direction.getOffsetX(), node.y, node.z + direction.getOffsetZ(), j, d, direction, pathNodeType2);
            succ[direction.getHorizontal()] = pathNode;
        }
        for (Direction directionx : Direction.Type.HORIZONTAL) {
            Direction direction2 = directionx.rotateYClockwise();
            if (this.isValidDiagonalSuccessor(node,
                    succ[directionx.getHorizontal()],
                    succ[direction2.getHorizontal()])) {
                PathNode pathNode2 = this.getPathNode(
                        node.x + directionx.getOffsetX() + direction2.getOffsetX(),
                        node.y,
                        node.z + directionx.getOffsetZ() + direction2.getOffsetZ(),
                        j,
                        d,
                        directionx,
                        pathNodeType2
                );
                if (this.isValidDiagonalSuccessor(pathNode2)) {
                    successors[i++] = pathNode2;
                }
            }
        }

        return i;
    }

    @Override
    protected boolean isValidDiagonalSuccessor(PathNode xNode, @Nullable PathNode zNode, @Nullable PathNode xDiagNode) {
        if (xDiagNode == null || zNode == null || (xDiagNode.y > xNode.y && zNode.y > xNode.y)) {
            return false;
        } else if (zNode.type != PathNodeType.WALKABLE_DOOR && xDiagNode.type != PathNodeType.WALKABLE_DOOR) {
            boolean bl = xDiagNode.type == PathNodeType.FENCE && zNode.type == PathNodeType.FENCE && (double)this.entity.getWidth() < 0.5;
            return (xDiagNode.y < xNode.y || xDiagNode.penalty >= 0.0F || bl) && (zNode.y < xNode.y || zNode.penalty >= 0.0F || bl);
        } else {
            return false;
        }
    }
}
