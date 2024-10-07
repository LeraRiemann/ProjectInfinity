package net.lerariemann.infinity.var;

import com.mojang.serialization.MapCodec;
import dev.architectury.injectables.annotations.ExpectPlatform;
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
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.gen.surfacebuilder.MaterialRules;

import static net.lerariemann.infinity.util.ConfigManager.getConfigDir;

public class ModMaterialRules {
    public record RandomBlockStateRule(WeighedStructure<NbtElement> w) implements MaterialRules.BlockStateRule
    {
        @Override
        public BlockState tryApply(int i, int j, int k) {
            long seed = MathHelper.hashCode(i, j, k);
            double d = (seed & 0xFFFL) / (double)0xFFFL;
            d = d - Math.floor(d);
            BlockState st = Registries.BLOCK.get(Identifier.of(RandomProvider.blockElementToName(w.getElement(d)))).getDefaultState();
            if(st.contains(Properties.PERSISTENT)) st = st.with(Properties.PERSISTENT, Boolean.TRUE);
            return st;
        }
    }

    enum RandomBlockMaterialRule implements MaterialRules.MaterialRule
    {
        INSTANCE;
        static final CodecHolder<RandomBlockMaterialRule> CODEC = CodecHolder.of(MapCodec.unit(INSTANCE));
        static final RandomProvider PROVIDER = new RandomProvider(getConfigDir() + "/");

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
        static final BlockState floor = Blocks.OAK_SLAB.getDefaultState();
        static final BlockState wall = ModBlocks.BOOK_BOX.get().getDefaultState();
        static final BlockState decor = Blocks.GLOWSTONE.getDefaultState();
        static final BlockState glass = Blocks.OAK_TRAPDOOR.getDefaultState();
        static final BlockState column = Blocks.OAK_PLANKS.getDefaultState();
        static final BlockState air = Blocks.AIR.getDefaultState();
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
                if (min_xz < 2) {
                    if (y == 0) return floor;
                    if (y < 4) return air; //corridors
                }
                return wall; //walls
            }
            if (max_xz < 2) {
                return ((x + z) % 2 == 1) ? wall : column; //central column
            }
            if (max_xz == 2 && min_xz == 1) {
                if (j == 0) return floor;
                Direction d = (x == 5 ? Direction.WEST : x == 9 ? Direction.EAST : z == 5 ? Direction.NORTH : Direction.SOUTH);
                return Blocks.LADDER.getDefaultState().with(LadderBlock.FACING, d); //ladders
            }
            if (y == 0) {
                if (max_xz == 4) {
                    if (min_xz == 4) return decor;
                    if (min_xz == 0 && j > 0) return glass; //lighting
                }
                return floor; //floor
            }
            return air;
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

    public static class Backrooms implements MaterialRules.BlockStateRule {
        static final BlockState floor = Blocks.MUSHROOM_STEM.getDefaultState();
        static final BlockState wall = Blocks.SMOOTH_SANDSTONE.getDefaultState();
        static final BlockState light = Blocks.OCHRE_FROGLIGHT.getDefaultState();
        static final BlockState ceiling = Blocks.SMOOTH_SANDSTONE.getDefaultState();
        static final BlockState air = Blocks.AIR.getDefaultState();
        static final BlockState filler = Blocks.OBSIDIAN.getDefaultState();
        int normalize(int x, int size) {
            int a = Math.abs(x < 0 ? x+1 : x) % size;
            return (x < 0) ? size - 1 - a : a;
        }
        int anti_normalize(int x, int size) {
            return Math.abs(x < 0 ? x+1 : x) / size;
        }
        @Override
        public BlockState tryApply(int i, int j, int k) {
            int size_xz = 15;
            int halfsize_xz = 7;
            int x = normalize(i, size_xz);
            int y = normalize(j-1, 16);
            int z = normalize(k, size_xz);
            int xrel = Math.abs(halfsize_xz - x);
            int zrel = Math.abs(halfsize_xz - z);
            int max_xz = Math.max(xrel, zrel);
            int min_xz = Math.min(xrel, zrel);
            boolean isXMax = max_xz == xrel;
            boolean isOpen = (max_xz >= 3) && (min_xz <= 3) && (y <= 6) && ((double) (MathHelper.hashCode(
                    2*anti_normalize(i, size_xz) - MathHelper.sign(i)*MathHelper.sign(halfsize_xz - x)*(isXMax ? 1 : 0),
                    anti_normalize(j-1, 16),
                    2*anti_normalize(k, size_xz) - MathHelper.sign(k)*MathHelper.sign(halfsize_xz - z)*(isXMax ? 0 : 1))
                    & 0xFL) / 15.0 > 0.3);
            if (isOpen || (min_xz <=3 && y <= 6 && (i==0 || k==0))) {
                if (min_xz == 3) return wall;
                if (y == 0) return floor;
                if (y == 6) {
                    if (min_xz == 0 && max_xz%3 == 0) return light;
                    return ceiling;
                }
                return air;
            }
            else if (max_xz <= 3 && y <= 6) {
                if (max_xz == 3) return wall;
                if (y == 0) return floor;
                if (y == 6) {
                    if (max_xz == 0) return light;
                    return ceiling;
                }
                return air;
            }
            return filler;
        }

    }
    enum BackroomsRule implements MaterialRules.MaterialRule {
        INSTANCE;
        static final CodecHolder<BackroomsRule> CODEC = CodecHolder.of(MapCodec.unit(INSTANCE));
        @Override
        public CodecHolder<? extends MaterialRules.MaterialRule> codec() {
            return CODEC;
        }
        @Override
        public MaterialRules.BlockStateRule apply(MaterialRules.MaterialRuleContext materialRuleContext) {
            return new Backrooms();
        }
    }

    @ExpectPlatform
    public static <T extends CodecHolder<? extends MaterialRules.MaterialRule>> void register(String name, T holder) {
        throw new AssertionError();
    }

    public static void registerRules() {
        register("chaos", RandomBlockMaterialRule.CODEC);
        register("library", LibraryRule.CODEC);
        register("backrooms", BackroomsRule.CODEC);
    }
}
