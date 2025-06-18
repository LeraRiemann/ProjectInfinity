package net.lerariemann.infinity.util.config;

import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.access.MinecraftServerAccess;
import net.lerariemann.infinity.registry.core.ModBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ConfigGenInvocation {
    protected Map<String, BlockState> map;
    public static int[] offsets = new int[]{-1, 0, 1};
    public static int[] offsets_y = new int[]{1, 2, 3};
    public ServerWorld world;
    public BlockPos pos;

    public static void invokeOn(ServerPlayerEntity player) {
        ServerWorld w = player.getServerWorld();
        int y = w.getTopY() - 10;
        BlockPos pos = new BlockPos(player.getBlockX(), y, player.getBlockZ());
        (new ConfigGenInvocation(w, pos)).run();
    }

    public ConfigGenInvocation(ServerWorld w, BlockPos p) {
        world = w;
        pos = p;
        map = new HashMap<>();
    }

    public void run() {
        InfinityMod.LOGGER.info("Invoking the name of the Cosmic Altar...");
        //setting up space
        map.put("0,0,0", world.getBlockState(pos));
        world.setBlockState(pos, ModBlocks.COSMIC_ALTAR.get().getDefaultState());
        for (int i : offsets) for (int j : offsets_y) for (int k : offsets) {
            map.put(i + "," + j + "," + k, world.getBlockState(pos.add(i, j, k)));
            world.setBlockState(pos.add(i, j, k), Blocks.AIR.getDefaultState());
        }
        //invocation
        ConfigGenerator.generateAll(world.getServer());
        Set<String> fluidBlockNames = ConfigGenerator.generateFluids();
        ConfigGenerator.generateBlocks(world, pos.up(2), pos.up(), fluidBlockNames);
        //putting everything back
        for (int i : offsets) for (int j : offsets_y) for (int k : offsets) {
            world.setBlockState(pos.add(i, j, k), fromMap(i, j, k));
        }
        world.setBlockState(pos, fromMap(0, 0, 0));

        ((MinecraftServerAccess)world.getServer()).infinity$onInvocation();
    }

    BlockState fromMap(int i, int j, int k) {
        BlockState s = map.get(i + "," + j + "," + k);
        if (s==null) s = Blocks.AIR.getDefaultState();
        return s;
    }
}
