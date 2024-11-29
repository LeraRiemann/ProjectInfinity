package net.lerariemann.infinity.item;

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
import net.minecraft.nbt.NbtCompound;

import java.util.List;
import java.util.Map;

public class SetAltarStateLootFunction extends ConditionalLootFunction {
//    public static final MapCodec<SetAltarStateLootFunction> CODEC = RecordCodecBuilder.mapCodec(
//            instance -> addConditionsField(instance).apply(instance, SetAltarStateLootFunction::new)
//    );

    private SetAltarStateLootFunction(List<LootCondition> conditions) {
        super(conditions.toArray(new LootCondition[0]));
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

