package net.lerariemann.infinity.util.neoforge;

import static net.lerariemann.infinity.registry.core.ModItems.*;
import net.lerariemann.infinity.registry.core.ModItems;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import static net.minecraft.item.Items.*;

public class NeoItems {
    
    public static void addAfter(BuildCreativeModeTabContentsEvent event, Item itemBefore, Item itemAfter) {
        event.insertAfter(itemBefore.getDefaultStack(), itemAfter.getDefaultStack(), ItemGroup.StackVisibility.PARENT_AND_SEARCH_TABS);
    }
    
    //MyItemsClass.MY_ITEM is a Supplier<? extends Item>, MyBlocksClass.MY_BLOCK is a Supplier<? extends Block>
    @SubscribeEvent
    public static void buildContents(BuildCreativeModeTabContentsEvent event) {
        // Is this the tab we want to add to?
        if (event.getTabKey() == ItemGroups.BUILDING_BLOCKS) {
            addAfter(event, NETHERITE_BLOCK, ModItems.NETHERITE_SLAB_ITEM.get());
            addAfter(event, NETHERITE_BLOCK, ModItems.NETHERITE_STAIRS_ITEM.get());
        } else if (event.getTabKey() == ItemGroups.FUNCTIONAL) {
            addAfter(event, LECTERN, ModItems.COSMIC_ALTAR_ITEM.get());
            addAfter(event, LECTERN,ModItems.ALTAR_ITEM.get());
            addAfter(event, LODESTONE,ModItems.ANT_ITEM.get());
            addAfter(event, CHISELED_BOOKSHELF,ModItems.BOOK_BOX_ITEM.get());
            addAfter(event, NOTE_BLOCK,ModItems.NOTES_BLOCK_ITEM.get());
            addAfter(event, VAULT,ModItems.TIME_BOMB_ITEM.get());
        } else if (event.getTabKey() == ItemGroups.INGREDIENTS) {
            addAfter(event, DISC_FRAGMENT_5,ModItems.FOOTPRINT.get());
            addAfter(event, OMINOUS_TRIAL_KEY, TRANSFINITE_KEY.get());
            addAfter(event, NETHER_STAR,ModItems.IRIDESCENT_STAR.get());
            addAfter(event, NETHER_STAR,ModItems.STAR_OF_LANG.get());
            addAfter(event, DISC_FRAGMENT_5,ModItems.CHROMATIC_MATTER.get());
            addAfter(event, DISC_FRAGMENT_5,ModItems.WHITE_MATTER.get());
            addAfter(event, DISC_FRAGMENT_5,ModItems.BLACK_MATTER.get());
            addAfter(event, EXPERIENCE_BOTTLE,ModItems.BIOME_BOTTLE_ITEM.get());
        } else if (event.getTabKey() == ItemGroups.FOOD_AND_DRINK) {
            addAfter(event, MILK_BUCKET,ModItems.HOME_ITEM.get());
            addAfter(event, HONEY_BOTTLE,ModItems.IRIDESCENT_POTION.get());
            addAfter(event, HONEY_BOTTLE,ModItems.CHROMATIC_POTION.get());
        } else if (event.getTabKey() == ItemGroups.TOOLS) {
            addAfter(event, WRITABLE_BOOK,ModItems.F4.get());
            addAfter(event, MUSIC_DISC_PIGSTEP,ModItems.DISC.get());
            addAfter(event, MILK_BUCKET,ModItems.IRIDESCENCE_BUCKET.get());
        } else if (event.getTabKey() == ItemGroups.COLORED_BLOCKS) {
            addAfter(event, PINK_WOOL,ModItems.IRIDESCENT_WOOL.get());
            addAfter(event, PINK_CARPET,ModItems.IRIDESCENT_CARPET.get());
            addAfter(event, PINK_WOOL,ModItems.CHROMATIC_WOOL.get());
            addAfter(event, PINK_CARPET,ModItems.CHROMATIC_CARPET.get());
            addAfter(event, PINK_TERRACOTTA,ModItems.CURSOR_ITEM.get());
        }
    }
}
