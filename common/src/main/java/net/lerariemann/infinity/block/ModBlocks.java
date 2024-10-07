package net.lerariemann.infinity.block;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import dev.architectury.registry.registries.Registrar;
import dev.architectury.registry.registries.RegistrarManager;
import dev.architectury.registry.registries.RegistrySupplier;
import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.block.custom.*;
import net.lerariemann.infinity.entity.ModEntities;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

import static net.lerariemann.infinity.InfinityMod.MOD_ID;
import static net.lerariemann.infinity.PlatformMethods.unfreeze;

public class ModBlocks {
    public static final Supplier<RegistrarManager> MANAGER = Suppliers.memoize(() -> RegistrarManager.get(MOD_ID));
    static Registrar<Block> blocks = MANAGER.get().get(RegistryKeys.BLOCK);
    static Registrar<Item> items = MANAGER.get().get(RegistryKeys.ITEM);

    //portal
    public static RegistrySupplier<Block> NEITHER_PORTAL = blocks.register(Identifier.of(MOD_ID, "example_item"), () -> new NeitherPortalBlock(AbstractBlock.Settings.copy(Blocks.NETHER_PORTAL)));
    //book box
    public static RegistrySupplier<Block> BOOK_BOX = blocks.register(Identifier.of(MOD_ID, "book_box"), () -> new NeitherPortalBlock(AbstractBlock.Settings.copy(Blocks.BOOKSHELF)));
    public static RegistrySupplier<Item> BOOK_BOX_ITEM = items.register(Identifier.of(MOD_ID, "book_box"), () -> new BlockItem(BOOK_BOX.get(), new Item.Settings()));
    //altar
    public static RegistrySupplier<Block> ALTAR_COSMIC = blocks.register(Identifier.of(MOD_ID, "altar_cosmic"), () -> new NeitherPortalBlock(AbstractBlock.Settings.copy(Blocks.BEDROCK).nonOpaque()));
    public static RegistrySupplier<Block> ALTAR_LIT = blocks.register(Identifier.of(MOD_ID, "altar_lit"), () -> new NeitherPortalBlock(AbstractBlock.Settings.copy(Blocks.BEDROCK).nonOpaque().luminance(state -> state.get(TransfiniteAltar.FLOWER) ? 15 : 0)));
    public static RegistrySupplier<Block> ALTAR = blocks.register(Identifier.of(MOD_ID, "altar"), () -> new NeitherPortalBlock(AbstractBlock.Settings.copy(Blocks.BEDROCK).nonOpaque().
            luminance(state -> state.get(TransfiniteAltarBase.FLOWER) ? 15 : 0)));
    public static RegistrySupplier<Item> ALTAR_ITEM = items.register(Identifier.of(MOD_ID, "altar"), () -> new BlockItem(ALTAR.get(), new Item.Settings()));


    //reset charge
    public static RegistrySupplier<Block> TIME_BOMB = blocks.register(Identifier.of(MOD_ID, "book_box"), () -> new NeitherPortalBlock(AbstractBlock.Settings.copy(Blocks.BEDROCK)));
    public static RegistrySupplier<Item> TIME_BOMB_ITEM = items.register(Identifier.of(MOD_ID, "book_box"), () -> new BlockItem(BOOK_BOX.get(), new Item.Settings()));

    //spawn eggs
//    public static final Item CHAOS_SKELETON_SPAWN_EGG = new SpawnEggItem(ModEntities.CHAOS_SKELETON, 4012102, 9519532, new Item.Settings());
//    public static final Item CHAOS_SLIME_SPAWN_EGG = new SpawnEggItem(ModEntities.CHAOS_SLIME, 16753145, 12607947, new Item.Settings());
//    public static final Item CHAOS_CREEPER_SPAWN_EGG = new SpawnEggItem(ModEntities.CHAOS_CREEPER, 4259648, 16753480, new Item.Settings());
//    public static final Item CHAOS_PAWN_SPAWN_EGG = new SpawnEggItem(ModEntities.CHAOS_PAWN, 0, 16777215, new Item.Settings());


    public static void registerModBlocks() {


//        registerItem("chaos_skeleton_spawn_egg", CHAOS_SKELETON_SPAWN_EGG);
//        registerItem("chaos_creeper_spawn_egg", CHAOS_CREEPER_SPAWN_EGG);
//        registerItem("chaos_pawn_spawn_egg", CHAOS_PAWN_SPAWN_EGG);
//        registerItem("chaos_slime_spawn_egg", CHAOS_SLIME_SPAWN_EGG);

//        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(content -> content.addAfter(Items.CHISELED_BOOKSHELF, ALTAR_ITEM));
//        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(content -> content.addAfter(Items.CHISELED_BOOKSHELF, BOOK_BOX_ITEM));
//        ItemGroupEvents.modifyEntriesEvent(ItemGroups.OPERATOR).register(content -> content.add(TIME_BOMB_ITEM));
//        ItemGroupEvents.modifyEntriesEvent(ItemGroups.SPAWN_EGGS).register(content -> {
//            content.add(CHAOS_SKELETON_SPAWN_EGG);
//            content.add(CHAOS_CREEPER_SPAWN_EGG);
//            content.add(CHAOS_SLIME_SPAWN_EGG);
//            content.add(CHAOS_PAWN_SPAWN_EGG);
//        });
    }
}