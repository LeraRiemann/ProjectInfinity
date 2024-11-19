package net.lerariemann.infinity.block;

import dev.architectury.core.block.ArchitecturyLiquidBlock;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.lerariemann.infinity.PlatformMethods;
import net.lerariemann.infinity.block.custom.*;
import net.minecraft.block.*;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.BlockSoundGroup;

import static net.lerariemann.infinity.InfinityMod.MOD_ID;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(MOD_ID, RegistryKeys.BLOCK);

    public static final RegistrySupplier<Block> ALTAR_COSMIC = BLOCKS.register("altar_cosmic", () ->
            new CosmicAltar(AbstractBlock.Settings.copy(Blocks.BEDROCK).nonOpaque()));
    public static final RegistrySupplier<Block> ALTAR_LIT = BLOCKS.register("altar_lit", () ->
            new TransfiniteAltar(AbstractBlock.Settings.copy(Blocks.BEDROCK).nonOpaque().luminance(state -> state.get(TransfiniteAltar.FLOWER) ? 15 : 0)));
    public static final RegistrySupplier<Block> ALTAR = BLOCKS.register("altar", () ->
            new TransfiniteAltarBase(AbstractBlock.Settings.copy(Blocks.STONE).nonOpaque().luminance(state -> state.get(TransfiniteAltarBase.FLOWER) ? 15 : 0)));
    public static final RegistrySupplier<Block> ANT = BLOCKS.register("ant", () ->
            new AntBlock(AbstractBlock.Settings.create().strength(-1f, 3600000.0f).mapColor(MapColor.WHITE).sounds(BlockSoundGroup.METAL).dropsNothing()));
    public static final RegistrySupplier<Block> BOOK_BOX = BLOCKS.register("book_box", () ->
            new BookBoxBlock(AbstractBlock.Settings.copy(Blocks.BOOKSHELF)));
    public static final RegistrySupplier<Block> CURSOR = BLOCKS.register("cursor", () ->
            new Block(AbstractBlock.Settings.create().strength(1.8f).mapColor(MapColor.GREEN).sounds(BlockSoundGroup.STONE)));
    public static final RegistrySupplier<Block> NEITHER_PORTAL = BLOCKS.register("neither_portal", () ->
            new NeitherPortalBlock(AbstractBlock.Settings.copy(Blocks.NETHER_PORTAL)));
    public static final RegistrySupplier<Block> NETHERITE_STAIRS = BLOCKS.register("netherite_stairs", () ->
            new ModStairs(Blocks.NETHERITE_BLOCK.getDefaultState(), AbstractBlock.Settings.copy(Blocks.NETHERITE_BLOCK)));
    public static final RegistrySupplier<Block> NETHERITE_SLAB = BLOCKS.register("netherite_slab", () ->
            new SlabBlock(AbstractBlock.Settings.copy(Blocks.NETHERITE_BLOCK)));
    public static final RegistrySupplier<Block> TIME_BOMB = BLOCKS.register("timebomb", () ->
            new TimeBombBlock(AbstractBlock.Settings.copy(Blocks.BEDROCK).nonOpaque().luminance(state -> 15)));
    public static final RegistrySupplier<FluidBlock> IRIDESCENCE = BLOCKS.register("iridescence", () ->
            new ArchitecturyLiquidBlock(PlatformMethods.getIridescenceStill(), AbstractBlock.Settings.copy(Blocks.WATER)));

    public static void registerModBlocks() {
        BLOCKS.register();
    }
}