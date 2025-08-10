package net.lerariemann.infinity.registry.core;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.util.PlatformMethods;
import net.lerariemann.infinity.block.custom.*;
import net.minecraft.block.*;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.BlockSoundGroup;

import static net.lerariemann.infinity.InfinityMod.MOD_ID;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(MOD_ID, RegistryKeys.BLOCK);

    public static final RegistrySupplier<Block> COSMIC_ALTAR = BLOCKS.register("cosmic_altar", () ->
            new CosmicAltarBlock(AbstractBlock.Settings.copy(Blocks.NETHERITE_BLOCK).luminance(state -> 15).nonOpaque()));
    public static final RegistrySupplier<Block> ALTAR = BLOCKS.register("altar", () ->
            new AltarBlock(AbstractBlock.Settings.copy(Blocks.STONE).nonOpaque().luminance(state -> state.get(AltarBlock.FLOWER) ? 15 : 0)));
    public static final RegistrySupplier<Block> ANT = BLOCKS.register("ant", () ->
            new AntBlock(AbstractBlock.Settings.copy(Blocks.WHITE_CONCRETE)));
    public static final RegistrySupplier<Block> BOOK_BOX = BLOCKS.register("book_box", () ->
            new BookBoxBlock(AbstractBlock.Settings.copy(Blocks.BOOKSHELF)));
    public static final RegistrySupplier<Block> CURSOR = BLOCKS.register("cursor", () ->
            new Block(AbstractBlock.Settings.create().strength(1.8f).mapColor(MapColor.GREEN).sounds(BlockSoundGroup.STONE)));
    public static final RegistrySupplier<Block> PORTAL = BLOCKS.register("neither_portal", () ->
            new InfinityPortalBlock(AbstractBlock.Settings.copy(Blocks.NETHER_PORTAL)));
    public static final RegistrySupplier<Block> NETHERITE_STAIRS = BLOCKS.register("netherite_stairs", () ->
            new ModStairsBlock(Blocks.NETHERITE_BLOCK.getDefaultState(), AbstractBlock.Settings.copy(Blocks.NETHERITE_BLOCK)));
    public static final RegistrySupplier<Block> NETHERITE_SLAB = BLOCKS.register("netherite_slab", () ->
            new SlabBlock(AbstractBlock.Settings.copy(Blocks.NETHERITE_BLOCK)));
    public static final RegistrySupplier<Block> TIME_BOMB = BLOCKS.register("timebomb", () ->
            new TimeBombBlock(AbstractBlock.Settings.copy(Blocks.BEDROCK).nonOpaque().luminance(state -> 15)));
    public static final RegistrySupplier<FluidBlock> IRIDESCENCE = PlatformMethods.getIridBlockForReg();
    public static final RegistrySupplier<Block> BIOME_BOTTLE = BLOCKS.register("biome_bottle", () ->
            new BiomeBottleBlock(AbstractBlock.Settings.copy(Blocks.BEACON).luminance(state -> state.get(BiomeBottleBlock.LEVEL))
                    .sounds(BlockSoundGroup.GLASS)));
    public static final RegistrySupplier<Block> IRIDESCENT_WOOL = BLOCKS.register("iridescent_wool", () ->
            new IridescentBlock(AbstractBlock.Settings.copy(Blocks.MAGENTA_WOOL)));
    public static final RegistrySupplier<Block> IRIDESCENT_CARPET = BLOCKS.register("iridescent_carpet", () ->
            new IridescentBlock.Carpet(AbstractBlock.Settings.copy(Blocks.MAGENTA_CARPET)));
    public static final RegistrySupplier<Block> CHROMATIC_WOOL = BLOCKS.register("chromatic_wool", () ->
            new ChromaticBlock(AbstractBlock.Settings.copy(Blocks.MAGENTA_WOOL)));
    public static final RegistrySupplier<Block> CHROMATIC_CARPET = BLOCKS.register("chromatic_carpet", () ->
            new ChromaticBlock.Carpet(AbstractBlock.Settings.copy(Blocks.MAGENTA_CARPET)));
    public static final RegistrySupplier<IridescentKelpBlock> IRIDESCENT_KELP = BLOCKS.register("iridescent_kelp", () ->
            new IridescentKelpBlock(AbstractBlock.Settings.copy(Blocks.KELP).mapColor(MapColor.MAGENTA)));
    public static final RegistrySupplier<IridescentKelpBlock.Plant> IRIDESCENT_KELP_PLANT = BLOCKS.register("iridescent_kelp_plant", () ->
            new IridescentKelpBlock.Plant(AbstractBlock.Settings.copy(Blocks.KELP).mapColor(MapColor.MAGENTA)));
    public static final RegistrySupplier<Block> NOTES_BLOCK = BLOCKS.register("notes_block", () ->
            new NotesBlock(AbstractBlock.Settings.copy(Blocks.NOTE_BLOCK).ticksRandomly()));
    public static final RegistrySupplier<Block> HAUNTED_AIR = BLOCKS.register("haunted_air", () ->
            new HauntedBlock(Blocks.AIR));
    public static final RegistrySupplier<RailHelper> RAIL_HELPER = BLOCKS.register("rail_helper", () ->
            new RailHelper(AbstractBlock.Settings.create().strength(-1.0F, 3600000.8F).mapColor(MapColor.CLEAR).dropsNothing().nonOpaque().allowsSpawning(Blocks::never).noBlockBreakParticles().pistonBehavior(PistonBehavior.BLOCK)));

    public static void registerModBlocks() {
        InfinityMod.LOGGER.debug("Registering blocks for " + MOD_ID);
        BLOCKS.register();
    }

    public static void registerFlammableBlocks() {
        PlatformMethods.registerFlammableBlock(ModBlocks.IRIDESCENT_WOOL, 60, 30);
        PlatformMethods.registerFlammableBlock(ModBlocks.IRIDESCENT_CARPET, 20, 60);
        PlatformMethods.registerFlammableBlock(ModBlocks.CHROMATIC_WOOL, 60, 30);
        PlatformMethods.registerFlammableBlock(ModBlocks.CHROMATIC_CARPET, 20, 60);
    }
}