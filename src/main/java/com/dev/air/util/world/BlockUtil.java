package com.dev.air.util.world;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Direction;

import java.util.Arrays;
import java.util.List;

import static com.dev.air.util.MinecraftInstance.*;

public class BlockUtil {

    private static List<Block> blacklist = Arrays.asList(Blocks.air, Blocks.water, Blocks.lava);

    public static BlockData getBlockData(BlockPos input) {
        List<BlockPos> positions = Arrays.asList(
                input.add(0, -1, 0),
                input.add(-1, 0, 0),
                input.add(1, 0, 0),
                input.add(0, 0, -1),
                input.add(0, 0, 1),
                input.add(-1, 0, 1),
                input.add(1, 0, -1),
                input.add(-1, 0, -1),
                input.add(1, 0, 1)
        );
        for (BlockPos pos : positions) {
            if (!blacklist.contains(mc.world.getBlockState(pos).getBlock())) {
                Direction facing = getFacing(input, pos);
                return new BlockData(pos, facing);
            }
        }
        return null;
    }

    private static Direction getFacing(BlockPos input, BlockPos neighbor) {
        if (input.getX() < neighbor.getX()) {
            return Direction.WEST;
        } else if (input.getX() > neighbor.getX()) {
            return Direction.EAST;
        } else if (input.getZ() < neighbor.getZ()) {
            return Direction.NORTH;
        } else if (input.getZ() > neighbor.getZ()) {
            return Direction.SOUTH;
        } else {
            return Direction.UP;
        }
    }

    public static class BlockData {
        public BlockPos position;
        public Direction direction;

        public BlockData(BlockPos position, Direction direction) {
            this.position = position;
            this.direction = direction;
        }
    }

}
