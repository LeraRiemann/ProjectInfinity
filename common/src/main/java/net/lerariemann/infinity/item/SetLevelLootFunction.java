package net.lerariemann.infinity.item;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.lerariemann.infinity.block.custom.BiomeBottle;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BlockStateComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.function.ConditionalLootFunction;
import net.minecraft.loot.function.LootFunctionType;

import java.util.List;
import java.util.Map;

public class SetLevelLootFunction extends ConditionalLootFunction {
    public static final MapCodec<SetLevelLootFunction> CODEC = RecordCodecBuilder.mapCodec(
            instance -> addConditionsField(instance).apply(instance, SetLevelLootFunction::new)
    );

    private SetLevelLootFunction(List<LootCondition> conditions) {
        super(conditions);
    }

    @Override
    public LootFunctionType<? extends ConditionalLootFunction> getType() {
        return ModComponentTypes.SET_BIOME_BOTTLE_LEVEL.get();
    }

    @Override
    protected ItemStack process(ItemStack stack, LootContext context) {
        int charge = BiomeBottle.getCharge(stack);
        if (charge > 0) stack.applyComponentsFrom(ComponentMap.builder().add(DataComponentTypes.BLOCK_STATE,
                (new BlockStateComponent(Map.of())).with(BiomeBottle.LEVEL, BiomeBottle.getLevel(charge))).build());
        return stack;
    }
}
