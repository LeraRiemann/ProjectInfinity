package net.lerariemann.infinity.block;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.PlatformMethods;
import net.lerariemann.infinity.block.custom.*;
import net.lerariemann.infinity.iridescence.IridescenceLiquidBlock;
import net.minecraft.block.*;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;

import static net.lerariemann.infinity.InfinityMod.MOD_ID;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(MOD_ID, RegistryKeys.BLOCK);
    public static RegistryKey<Block> registryKey(String s) {
        Identifier id = InfinityMod.getId(s);
        return RegistryKey.of(RegistryKeys.BLOCK, id);
    }

    public static AbstractBlock.Settings create(String id) {
        return AbstractBlock.Settings.create().registryKey(registryKey(id));
    }

    public static AbstractBlock.Settings copy(String id, Block block) {
        return AbstractBlock.Settings.copy(block).registryKey(registryKey(id));
    }

    public static final RegistrySupplier<Block> ALTAR_COSMIC = BLOCKS.register("altar_cosmic", () ->
            new CosmicAltar(copy("altar_cosmic", Blocks.BEDROCK).nonOpaque()));
    public static final RegistrySupplier<Block> ALTAR_LIT = BLOCKS.register("altar_lit", () ->
            new TransfiniteAltar(copy("altar_lit", Blocks.BEDROCK).nonOpaque().luminance(state -> state.get(TransfiniteAltar.FLOWER) ? 15 : 0)));
    public static final RegistrySupplier<Block> ALTAR = BLOCKS.register("altar", () ->
            new TransfiniteAltarBase(copy("altar", Blocks.STONE).nonOpaque().luminance(state -> state.get(TransfiniteAltarBase.FLOWER) ? 15 : 0)));
    public static final RegistrySupplier<Block> ANT = BLOCKS.register("ant", () ->
            new AntBlock(create("ant").strength(-1f, 3600000.0f).mapColor(MapColor.WHITE).sounds(BlockSoundGroup.METAL).dropsNothing()));
    public static final RegistrySupplier<Block> BOOK_BOX = BLOCKS.register("book_box", () ->
            new BookBoxBlock(copy("book_box", Blocks.BOOKSHELF)));
    public static final RegistrySupplier<Block> CURSOR = BLOCKS.register("cursor", () ->
            new Block(create("cursor").strength(1.8f).mapColor(MapColor.GREEN).sounds(BlockSoundGroup.STONE)));
    public static final RegistrySupplier<Block> NEITHER_PORTAL = BLOCKS.register("neither_portal", () ->
            new NeitherPortalBlock(copy("neither_portal", Blocks.NETHER_PORTAL)));
    public static final RegistrySupplier<Block> NETHERITE_STAIRS = BLOCKS.register("netherite_stairs", () ->
            new ModStairs(Blocks.NETHERITE_BLOCK.getDefaultState(), copy("netherite_stairs", Blocks.NETHERITE_BLOCK)));
    public static final RegistrySupplier<Block> NETHERITE_SLAB = BLOCKS.register("netherite_slab", () ->
            new SlabBlock(copy("netherite_slab", Blocks.NETHERITE_BLOCK)));
    public static final RegistrySupplier<Block> TIME_BOMB = BLOCKS.register("timebomb", () ->
            new TimeBombBlock(copy("timebomb", Blocks.BEDROCK).nonOpaque().luminance(state -> 15)));
    public static final RegistrySupplier<FluidBlock> IRIDESCENCE = BLOCKS.register("iridescence", () ->
            new IridescenceLiquidBlock(PlatformMethods.getIridescenceStill(), copy("iridescence", Blocks.WATER)));

    public static void registerModBlocks() {
        BLOCKS.register();
    }
}