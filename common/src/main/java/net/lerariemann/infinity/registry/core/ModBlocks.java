package net.lerariemann.infinity.registry.core;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.lerariemann.infinity.util.PlatformMethods;
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
    public static final RegistrySupplier<Block> PORTAL = BLOCKS.register("neither_portal", () ->
            new InfinityPortalBlock(AbstractBlock.Settings.copy(Blocks.NETHER_PORTAL)));
    public static final RegistrySupplier<Block> NETHERITE_STAIRS = BLOCKS.register("netherite_stairs", () ->
            new ModStairs(Blocks.NETHERITE_BLOCK.getDefaultState(), AbstractBlock.Settings.copy(Blocks.NETHERITE_BLOCK)));
    public static final RegistrySupplier<Block> NETHERITE_SLAB = BLOCKS.register("netherite_slab", () ->
            new SlabBlock(AbstractBlock.Settings.copy(Blocks.NETHERITE_BLOCK)));
    public static final RegistrySupplier<Block> TIME_BOMB = BLOCKS.register("timebomb", () ->
            new TimeBombBlock(AbstractBlock.Settings.copy(Blocks.BEDROCK).nonOpaque().luminance(state -> 15)));
    public static final RegistrySupplier<FluidBlock> IRIDESCENCE = PlatformMethods.getIridBlockForReg();
    public static final RegistrySupplier<Block> BIOME_BOTTLE = BLOCKS.register("biome_bottle", () ->
            new BiomeBottle(AbstractBlock.Settings.copy(Blocks.BEACON).luminance(state -> state.get(BiomeBottle.LEVEL))
                    .sounds(BlockSoundGroup.GLASS)));
    public static final RegistrySupplier<Block> IRIDESCENT_WOOL = BLOCKS.register("iridescent_wool", () ->
            new IridescentBlock(AbstractBlock.Settings.copy(Blocks.MAGENTA_WOOL)));
    public static final RegistrySupplier<Block> IRIDESCENT_CARPET = BLOCKS.register("iridescent_carpet", () ->
            new IridescentBlock.Carpet(AbstractBlock.Settings.copy(Blocks.MAGENTA_CARPET)));
    public static final RegistrySupplier<IridescentKelp> IRIDESCENT_KELP = BLOCKS.register("iridescent_kelp", () ->
            new IridescentKelp(AbstractBlock.Settings.copy(Blocks.KELP).mapColor(MapColor.MAGENTA)));
    public static final RegistrySupplier<IridescentKelp.Plant> IRIDESCENT_KELP_PLANT = BLOCKS.register("iridescent_kelp_plant", () ->
            new IridescentKelp.Plant(AbstractBlock.Settings.copy(Blocks.KELP).mapColor(MapColor.MAGENTA)));


    public static void registerModBlocks() {
        BLOCKS.register();
    }

    public static void registerFlammableBlocks() {
        PlatformMethods.registerFlammableBlock(ModBlocks.IRIDESCENT_WOOL, 60, 30);
        PlatformMethods.registerFlammableBlock(ModBlocks.IRIDESCENT_CARPET, 20, 60);
    }
}