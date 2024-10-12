package net.lerariemann.infinity.neoforge.client;

import net.lerariemann.infinity.PlatformMethods;
import net.lerariemann.infinity.block.ModBlocks;
import net.lerariemann.infinity.block.entity.NeitherPortalBlockEntity;
import net.lerariemann.infinity.config.neoforge.ModConfigFactory;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

import static net.lerariemann.infinity.InfinityModClient.sampler;

public class InfinityModNeoForgeClient {

    static double sample(int x, int y, int z) {
        return sampler.sample(x, y, z);
    }

    static int posToColor(BlockPos pos) {
        double r = sample(pos.getX(), pos.getY() - 10000, pos.getZ());
        double g = sample(pos.getX(), pos.getY(), pos.getZ());
        double b = sample(pos.getX(), pos.getY() + 10000, pos.getZ());
        return (int)(256 * ((r + 1)/2)) + 256*((int)(256 * ((g + 1)/2)) + 256*(int)(256 * ((b + 1)/2)));
    }

    //Integrate Cloth Config screen (if mod present) with NeoForge mod menu.
    public static void registerModsPage() {
        if (clothConfigInstalled()) ModLoadingContext.get().registerExtensionPoint(IConfigScreenFactory.class, ModConfigFactory::new);
    }

    // Client-side mod bus event handler
    @SubscribeEvent
    public static void registerBlockColorHandlers(RegisterColorHandlersEvent.Block event) {
        event.register((state, world, pos, tintIndex) -> {
            if (world != null && pos != null) {
                BlockEntity blockEntity = world.getBlockEntity(pos);
                if (blockEntity instanceof NeitherPortalBlockEntity) {
                    Object j = ((NeitherPortalBlockEntity) blockEntity).getRenderData();
                    if (j == null) return 0;
                    return (int)j & 0xFFFFFF;
                }
            }
            return 16777215;
                },    ModBlocks.NEITHER_PORTAL.get());
        event.register((state, world, pos, tintIndex) -> {
            if (pos != null) {
                return posToColor(pos);
            }
            return 16777215;
        }, ModBlocks.BOOK_BOX.get());
    }

    private static boolean clothConfigInstalled() {
        return PlatformMethods.isModLoaded("cloth_config");
    }
}