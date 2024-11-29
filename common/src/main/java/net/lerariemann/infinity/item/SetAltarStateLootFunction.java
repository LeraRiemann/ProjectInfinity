package net.lerariemann.infinity.item;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSyntaxException;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.lerariemann.infinity.block.custom.TransfiniteAltar;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.function.ConditionalLootFunction;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.loot.function.SetNbtLootFunction;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.util.JsonHelper;

import java.util.List;
import java.util.Map;

public class SetAltarStateLootFunction extends ConditionalLootFunction {
    public static class Serializer extends ConditionalLootFunction.Serializer<SetAltarStateLootFunction> {
        public Serializer() {
        }

        public void toJson(JsonObject jsonObject, SetAltarStateLootFunction setAltarStateLootFunction, JsonSerializationContext jsonSerializationContext) {
            super.toJson(jsonObject, setAltarStateLootFunction, jsonSerializationContext);
            jsonObject.addProperty("tag", setAltarStateLootFunction.nbt.toString());
        }

        public SetAltarStateLootFunction fromJson(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootCondition[] lootConditions) {
            try {
                NbtCompound nbtCompound = StringNbtReader.parse(JsonHelper.getString(jsonObject, "tag"));
                return new SetAltarStateLootFunction(lootConditions);
            } catch (CommandSyntaxException commandSyntaxException) {
                throw new JsonSyntaxException(commandSyntaxException.getMessage());
            }
        }
    }

    private SetAltarStateLootFunction(LootCondition[] conditions) {
        super(conditions);
    }

    @Override
    public LootFunctionType getType() {
        return ModItemFunctions.SET_ALTAR_STATE.get();
    }

    @Override
    protected ItemStack process(ItemStack stack, LootContext context) {
        BlockState st = context.get(LootContextParameters.BLOCK_STATE);
        if (st == null) return stack;
        int color = st.get(TransfiniteAltar.COLOR);
        boolean flower = st.get(TransfiniteAltar.FLOWER);
        if (color > 0 || flower) {
            NbtCompound compound = new NbtCompound();
            compound.putInt("custom_model_data", color + (flower ? 7 : 0));
            stack.setNbt(compound);
        }
        return stack;
    }
}

