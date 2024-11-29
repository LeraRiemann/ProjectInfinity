package net.lerariemann.infinity.item;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.lerariemann.infinity.block.custom.BiomeBottle;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.function.ConditionalLootFunction;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.JsonSerializer;
import org.apache.commons.lang3.ArrayUtils;

import java.util.List;

public class SetLevelLootFunction extends ConditionalLootFunction {

    private SetLevelLootFunction(List<LootCondition> conditions) {
        super(conditions.toArray(conditions.toArray(new LootCondition[0])));
    }

    @Override
    public LootFunctionType getType() {
        return ModItemFunctions.SET_BIOME_BOTTLE_LEVEL.get();
    }

    @Override
    protected ItemStack process(ItemStack stack, LootContext context) {
        BiomeBottle.updateCharge(stack);
        return stack;
    }
}
