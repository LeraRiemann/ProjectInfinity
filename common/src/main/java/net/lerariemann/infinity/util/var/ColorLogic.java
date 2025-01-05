package net.lerariemann.infinity.util.var;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;

import java.util.Map;

public interface ColorLogic {
    String[] vanillaColors = {"white", "light_gray", "gray", "black", "brown", "red", "orange", "yellow", "lime", "green",
            "light_blue", "blue", "cyan", "purple", "magenta", "pink"};
    Map<TagKey<Block>, String> supportedBlockTypes = Map.ofEntries(
            Map.entry(BlockTags.WOOL, "$_wool"),
            Map.entry(BlockTags.WOOL_CARPETS, "$_carpet"));

    static Block getBlockByColor(String color, TagKey<Block> type) {
        return Registries.BLOCK.get(new Identifier(supportedBlockTypes.get(type)
                .replace("$", color)));
    }

    static Block getBlockByColor(DyeColor color, TagKey<Block> type) {
        return getBlockByColor(color.getName(), type);
    }

    static DyeColor getColorByState(BlockState state) {
        for (TagKey<Block> key : supportedBlockTypes.keySet()) if (state.isIn(key)) {
            Identifier id = Registries.BLOCK.getId(state.getBlock());
            return DyeColor.byName(
                    id.getPath().replace(supportedBlockTypes.get(key).replace("$", ""), ""),
                    null);
        }
        return null;
    }
    static boolean isSupported(BlockState state) {
        for (TagKey<Block> key : supportedBlockTypes.keySet()) if (state.isIn(key)) return true;
        return false;
    }

    static Block recolor(String color, BlockState state) {
        for (TagKey<Block> key : supportedBlockTypes.keySet()) if (state.isIn(key))
            return getBlockByColor(color, key);
        return null;
    }
}
