package net.lerariemann.infinity.registry.var;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.registry.tag.TagKey;

import static net.lerariemann.infinity.util.PlatformMethods.createItemTag;
import static net.lerariemann.infinity.util.PlatformMethods.createBlockTag;

public class ModTags {
    public static TagKey<Block> IRIDESCENT_BLOCKS = createBlockTag("iridescent");
    public static TagKey<Item> IRIDESCENT_ITEMS = createItemTag("iridescent");
    public static TagKey<Item> MATTER = createItemTag("matter");
}
