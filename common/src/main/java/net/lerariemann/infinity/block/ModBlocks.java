package net.lerariemann.infinity.block;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.lerariemann.infinity.block.custom.*;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.registry.RegistryKeys;

import static net.lerariemann.infinity.InfinityMod.MOD_ID;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(MOD_ID, RegistryKeys.BLOCK);
    public static final RegistrySupplier<Block> NEITHER_PORTAL = BLOCKS.register("neither_portal", () ->
            new NeitherPortalBlock(AbstractBlock.Settings.copy(Blocks.NETHER_PORTAL)));
    public static final RegistrySupplier<Block> BOOK_BOX = BLOCKS.register("book_box", () ->
            new BookBoxBlock(AbstractBlock.Settings.copy(Blocks.BOOKSHELF)));

    public static final RegistrySupplier<Block> ALTAR_COSMIC = BLOCKS.register("altar_cosmic", () -> new CosmicAltar(AbstractBlock.Settings.copy(Blocks.BEDROCK).nonOpaque()));
    public static final RegistrySupplier<Block> ALTAR_LIT = BLOCKS.register("altar_lit", () -> new TransfiniteAltar(AbstractBlock.Settings.copy(Blocks.BEDROCK).nonOpaque().luminance(state -> state.get(TransfiniteAltar.FLOWER) ? 15 : 0)));
    public static final RegistrySupplier<Block> ALTAR = BLOCKS.register("altar", () -> new TransfiniteAltar(AbstractBlock.Settings.copy(Blocks.STONE).nonOpaque().luminance(state -> state.get(TransfiniteAltar.FLOWER) ? 15 : 0)));

    public static final RegistrySupplier<Block> TIME_BOMB = BLOCKS.register("timebomb", () -> new TimeBombBlock(AbstractBlock.Settings.copy(Blocks.BEDROCK).nonOpaque().luminance(state -> 15)));


    public static void registerModBlocks() {
        BLOCKS.register();
    }
}