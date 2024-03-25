package net.lerariemann.infinity.var;

import com.mojang.serialization.MapCodec;
import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.block.ModBlocks;
import net.lerariemann.infinity.dimensions.RandomProvider;
import net.lerariemann.infinity.util.WeighedStructure;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LadderBlock;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.CodecHolder;
import net.minecraft.util.math.Direction;
import net.minecraft.world.gen.densityfunction.DensityFunction;
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
        static final CodecHolder<RandomBlockMaterialRule> CODEC = CodecHolder.of(MapCodec.unit(INSTANCE));
        static final RandomProvider PROVIDER = new RandomProvider("config/" + InfinityMod.MOD_ID + "/");

        @Override
        public CodecHolder<? extends MaterialRules.MaterialRule> codec() {
            return CODEC;
        }

        @Override
        public MaterialRules.BlockStateRule apply(MaterialRules.MaterialRuleContext materialRuleContext) {
            return new RandomBlockStateRule(PROVIDER.blockRegistry.get("full_blocks_worldgen"));
        }
    }
    public static class Library implements MaterialRules.BlockStateRule
    {
        int normalize(int x, int size) {
            int a = Math.abs(x < 0 ? x+1 : x) % size;
            return (x < 0) ? size - 1 - a : a;
        }
        @Override
        public BlockState tryApply(int i, int j, int k) {
            int x = normalize(i, 15);
            int y = normalize(j, 16);
            int z = normalize(k, 15);
            int max_xz = Math.max(Math.abs(7 - x), Math.abs(7 - z));
            int min_xz = Math.min(Math.abs(7 - x), Math.abs(7 - z));
            if (max_xz == 7) {
                return (y == 0 && min_xz < 2) ? Blocks.OAK_SLAB.getDefaultState() : ModBlocks.BOOK_BOX.getDefaultState(); //walls
            }
            if (max_xz < 2) {
                return ((x + z) % 2 == 1) ? ModBlocks.BOOK_BOX.getDefaultState() : Blocks.OAK_PLANKS.getDefaultState(); //column
            }
            if (max_xz == 2 && min_xz == 1) {
                Direction d = (x == 5 ? Direction.WEST : x == 9 ? Direction.EAST : z == 5 ? Direction.NORTH : Direction.SOUTH);
                return Blocks.LADDER.getDefaultState().with(LadderBlock.FACING, d);
            }
            if (max_xz == 4 && min_xz == 4) return Blocks.GLOWSTONE.getDefaultState();
            return Blocks.OAK_SLAB.getDefaultState();
        }
    }
    enum LibraryRule implements MaterialRules.MaterialRule {
        INSTANCE;
        static final CodecHolder<LibraryRule> CODEC = CodecHolder.of(MapCodec.unit(INSTANCE));
        @Override
        public CodecHolder<? extends MaterialRules.MaterialRule> codec() {
            return CODEC;
        }
        @Override
        public MaterialRules.BlockStateRule apply(MaterialRules.MaterialRuleContext materialRuleContext) {
            return new Library();
        }
    }

    public static <T extends CodecHolder<? extends MaterialRules.MaterialRule>> void register(String name, T holder) {
        Registry.register(Registries.MATERIAL_RULE, InfinityMod.MOD_ID + ":" + name, holder.codec());
    }

    public static void registerRules() {
        register("chaos", RandomBlockMaterialRule.CODEC);
        register("library", LibraryRule.CODEC);
    }
}
