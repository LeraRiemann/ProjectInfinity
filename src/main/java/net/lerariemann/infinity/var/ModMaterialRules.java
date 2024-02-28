package net.lerariemann.infinity.var;

import com.mojang.serialization.MapCodec;
import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.dimensions.RandomProvider;
import net.lerariemann.infinity.util.WeighedStructure;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.CodecHolder;
import net.minecraft.world.gen.surfacebuilder.MaterialRules;

public class ModMaterialRules {
    public record RandomBlockStateRule(WeighedStructure<NbtElement> w) implements MaterialRules.BlockStateRule
    {
        @Override
        public BlockState tryApply(int i, int j, int k) {
            long size = w.keys.size();
            long seed = size * size * i + j + size * k;
            double d = seed / Math.PI;
            d = d - Math.floor(d);
            BlockState st = Registries.BLOCK.get(new Identifier(RandomProvider.blockElementToName(w.getElement(d)))).getDefaultState();
            if(st.contains(Properties.PERSISTENT)) st = st.with(Properties.PERSISTENT, Boolean.TRUE);
            return st;
        }
    }

    enum RandomBlockMaterialRule implements MaterialRules.MaterialRule
    {
        INSTANCE;
        static final CodecHolder<RandomBlockMaterialRule> CODEC;
        static final RandomProvider PROVIDER;
        static {
            CODEC = CodecHolder.of(MapCodec.unit(INSTANCE));
            PROVIDER = new RandomProvider("config/" + InfinityMod.MOD_ID + "/");
        }
        @Override
        public CodecHolder<? extends MaterialRules.MaterialRule> codec() {
            return CODEC;
        }

        @Override
        public MaterialRules.BlockStateRule apply(MaterialRules.MaterialRuleContext materialRuleContext) {
            return new RandomBlockStateRule(PROVIDER.blockRegistry.get("full_blocks_worldgen"));
        }
    }

    public static void registerRules() {
        Registry.register(Registries.MATERIAL_RULE, "infinity:chaos", RandomBlockMaterialRule.CODEC.codec());
    }
}
