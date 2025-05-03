package net.lerariemann.infinity.registry.core;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.util.InfinityMethods;
import net.lerariemann.infinity.util.PlatformMethods;
import net.lerariemann.infinity.block.custom.*;
import net.minecraft.block.*;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;

import static net.lerariemann.infinity.InfinityMod.MOD_ID;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(MOD_ID, RegistryKeys.BLOCK);

    public static RegistryKey<Block> registryKey(String s) {
        Identifier id = InfinityMethods.getId(s);
        return RegistryKey.of(RegistryKeys.BLOCK, id);
    }

    public static AbstractBlock.Settings create(String id) {
        return AbstractBlock.Settings.create().registryKey(registryKey(id));
    }

    public static AbstractBlock.Settings copy(String id, Block block) {
        return AbstractBlock.Settings.copy(block).registryKey(registryKey(id));
    }

    public static final RegistrySupplier<Block> COSMIC_ALTAR = BLOCKS.register("cosmic_altar", () ->
            new CosmicAltarBlock(copy("cosmic_altar",Blocks.NETHERITE_BLOCK).luminance(state -> 15).nonOpaque()));
    public static final RegistrySupplier<Block> ALTAR = BLOCKS.register("altar", () ->
            new AltarBlock(copy("altar",Blocks.STONE).nonOpaque().luminance(state -> state.get(AltarBlock.FLOWER) ? 15 : 0)));
    public static final RegistrySupplier<Block> ANT = BLOCKS.register("ant", () ->
            new AntBlock(copy("ant",Blocks.WHITE_CONCRETE)));
    public static final RegistrySupplier<Block> BOOK_BOX = BLOCKS.register("book_box", () ->
            new BookBoxBlock(copy("book_box",Blocks.BOOKSHELF)));
    public static final RegistrySupplier<Block> CURSOR = BLOCKS.register("cursor", () ->
            new Block(create("cursor").strength(1.8f).mapColor(MapColor.GREEN).sounds(BlockSoundGroup.STONE)));
    public static final RegistrySupplier<Block> PORTAL = BLOCKS.register("neither_portal", () ->
            new InfinityPortalBlock(copy("neither_portal",Blocks.NETHER_PORTAL)));
    public static final RegistrySupplier<Block> NETHERITE_STAIRS = BLOCKS.register("netherite_stairs", () ->
            new ModStairsBlock(Blocks.NETHERITE_BLOCK.getDefaultState(), copy("netherite_stairs",Blocks.NETHERITE_BLOCK)));
    public static final RegistrySupplier<Block> NETHERITE_SLAB = BLOCKS.register("netherite_slab", () ->
            new SlabBlock(copy("netherite_slab",Blocks.NETHERITE_BLOCK)));
    public static final RegistrySupplier<Block> TIME_BOMB = BLOCKS.register("timebomb", () ->
            new TimeBombBlock(copy("timebomb",Blocks.BEDROCK).nonOpaque().luminance(state -> 15)));
    public static final RegistrySupplier<FluidBlock> IRIDESCENCE = PlatformMethods.getIridBlockForReg();
    public static final RegistrySupplier<Block> BIOME_BOTTLE = BLOCKS.register("biome_bottle", () ->
            new BiomeBottleBlock(copy("biome_bottle",Blocks.BEACON).luminance(state -> state.get(BiomeBottleBlock.LEVEL))
                    .sounds(BlockSoundGroup.GLASS)));
    public static final RegistrySupplier<Block> IRIDESCENT_WOOL = BLOCKS.register("iridescent_wool", () ->
            new IridescentBlock(copy("iridescent_wool",Blocks.MAGENTA_WOOL)));
    public static final RegistrySupplier<Block> IRIDESCENT_CARPET = BLOCKS.register("iridescent_carpet", () ->
            new IridescentBlock.Carpet(copy("iridescent_carpet",Blocks.MAGENTA_CARPET)));
    public static final RegistrySupplier<Block> CHROMATIC_WOOL = BLOCKS.register("chromatic_wool", () ->
            new ChromaticBlock(copy("chromatic_wool",Blocks.MAGENTA_WOOL)));
    public static final RegistrySupplier<Block> CHROMATIC_CARPET = BLOCKS.register("chromatic_carpet", () ->
            new ChromaticBlock.Carpet(copy("chromatic_carpet",Blocks.MAGENTA_CARPET)));
    public static final RegistrySupplier<IridescentKelpBlock> IRIDESCENT_KELP = BLOCKS.register("iridescent_kelp", () ->
            new IridescentKelpBlock(copy("iridescent_kelp",Blocks.KELP).mapColor(MapColor.MAGENTA)));
    public static final RegistrySupplier<IridescentKelpBlock.Plant> IRIDESCENT_KELP_PLANT = BLOCKS.register("iridescent_kelp_plant", () ->
            new IridescentKelpBlock.Plant(copy("iridescent_kelp_plant",Blocks.KELP).mapColor(MapColor.MAGENTA)));
    public static final RegistrySupplier<Block> NOTES_BLOCK = BLOCKS.register("notes_block", () ->
            new NotesBlock(copy("notes_block",Blocks.NOTE_BLOCK).ticksRandomly()));
    public static final RegistrySupplier<Block> HAUNTED_AIR = BLOCKS.register("haunted_air", () ->
            new HauntedBlock(Blocks.AIR));

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