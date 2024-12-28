package net.lerariemann.infinity.item.function;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.lerariemann.infinity.block.custom.AltarBlock;
import net.lerariemann.infinity.registry.core.ModItemFunctions;
import net.minecraft.block.BlockState;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BlockStateComponent;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.function.ConditionalLootFunction;
import net.minecraft.loot.function.LootFunctionType;

import java.util.List;
import java.util.Map;

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
        if (st == null) return stack;
        int color = st.get(AltarBlock.COLOR);
        boolean flower = st.get(AltarBlock.FLOWER);
        if (color > 0 || flower) stack.applyComponentsFrom(ComponentMap.builder().add(DataComponentTypes.BLOCK_STATE,
                new BlockStateComponent(Map.of())
                        .with(AltarBlock.COLOR, color)
                        .with(AltarBlock.FLOWER, flower))
                .add(DataComponentTypes.CUSTOM_MODEL_DATA, new CustomModelDataComponent(color +
                        (flower ? AltarBlock.numColors : 0)))
                .build());
        return stack;
    }
}

