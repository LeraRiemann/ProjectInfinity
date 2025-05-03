package net.lerariemann.infinity.item.function;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.lerariemann.infinity.block.custom.AltarBlock;
import net.lerariemann.infinity.registry.core.ModItemFunctions;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.function.ConditionalLootFunction;
import net.minecraft.loot.function.LootFunctionType;

import java.util.List;

public class SetAltarStateLootFunction extends ConditionalLootFunction {
    public static final MapCodec<SetAltarStateLootFunction> CODEC = RecordCodecBuilder.mapCodec(
            instance -> addConditionsField(instance).apply(instance, SetAltarStateLootFunction::new)
    );

    private SetAltarStateLootFunction(List<LootCondition> conditions) {
        super(conditions);
    }

    @Override
    public LootFunctionType<? extends ConditionalLootFunction> getType() {
        return ModItemFunctions.SET_ALTAR_STATE.get();
    }

    @Override
    protected ItemStack process(ItemStack stack, LootContext context) {
        BlockState st = context.get(LootContextParameters.BLOCK_STATE);
        if (st != null) stack.applyChanges(AltarBlock.toComponentChanges(st));
        return stack;
    }
}

