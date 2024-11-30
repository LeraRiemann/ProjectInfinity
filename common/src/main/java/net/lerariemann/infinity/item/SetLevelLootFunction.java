package net.lerariemann.infinity.item;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSyntaxException;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.lerariemann.infinity.block.custom.BiomeBottle;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.function.ConditionalLootFunction;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.JsonSerializer;
import org.apache.commons.lang3.ArrayUtils;

import java.util.List;

public class SetLevelLootFunction extends ConditionalLootFunction {
    public static class Serializer extends ConditionalLootFunction.Serializer<SetLevelLootFunction> {
        public Serializer() {
        }

        public void toJson(JsonObject jsonObject, SetLevelLootFunction setLevelLootFunction, JsonSerializationContext jsonSerializationContext) {
            super.toJson(jsonObject, setLevelLootFunction, jsonSerializationContext);
//            jsonObject.addProperty("tag", setLevelLootFunction.nbt.toString());
        }

        public SetLevelLootFunction fromJson(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootCondition[] lootConditions) {
            try {
                NbtCompound nbtCompound = StringNbtReader.parse(JsonHelper.getString(jsonObject, "tag"));
                return new SetLevelLootFunction(lootConditions);
            } catch (CommandSyntaxException commandSyntaxException) {
                throw new JsonSyntaxException(commandSyntaxException.getMessage());
            }
        }
    }

    private SetLevelLootFunction(LootCondition[] conditions) {
        super(conditions);
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
