package net.lerariemann.infinity.block;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.block.custom.NeitherPortalBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registry;

public class ModBlocks {
    public static final Block NEITHER_PORTAL = registerBlockWithoutItem("neither_portal", new NeitherPortalBlock(FabricBlockSettings.copy(Blocks.NETHER_PORTAL)));
    private static Block registerBlockWithoutItem(String name, Block block) {
        return Registry.register(Registries.BLOCK, new Identifier(InfinityMod.MOD_ID, name), block);
    }
    public static void registerModBlocks() {
        InfinityMod.LOGGER.debug("Registering ModBlocks for " + InfinityMod.MOD_ID);
    }
}
