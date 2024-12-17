package net.lerariemann.infinity.registry.var;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.architectury.registry.registries.DeferredRegister;
import net.lerariemann.infinity.util.TextData;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.dynamic.CodecHolder;
import net.minecraft.world.gen.surfacebuilder.MaterialRules;
import net.lerariemann.infinity.mixin.core.MaterialRuleContextAccess;

import java.util.*;

import static net.lerariemann.infinity.InfinityMod.MOD_ID;

public class ModMaterialConditions {
    public record LinearCondition(double k_x, double k_y, double k_z, double min, double max, int separation) implements MaterialRules.MaterialCondition
    {
        public static final CodecHolder<LinearCondition> CODEC = CodecHolder.of(RecordCodecBuilder.mapCodec(instance -> instance.group(
                (Codec.DOUBLE.fieldOf("k_x").orElse(1.0).forGetter(a -> a.k_x)),
                (Codec.DOUBLE.fieldOf("k_y").orElse(0.0).forGetter(a -> a.k_y)),
                (Codec.DOUBLE.fieldOf("k_z").orElse(1.0).forGetter(a -> a.k_z)),
                (Codec.DOUBLE.fieldOf("min").orElse(0.0).forGetter(a -> a.min)),
                (Codec.DOUBLE.fieldOf("max").orElse(0.5).forGetter(a -> a.max)),
                (Codec.INT.fieldOf("separation").orElse(16).forGetter(a -> a.separation))
        ).apply(instance, LinearCondition::new)));

        @Override
        public CodecHolder<? extends MaterialRules.MaterialCondition> codec() {
            return CODEC;
        }

        @Override
        public MaterialRules.BooleanSupplier apply(MaterialRules.MaterialRuleContext materialRuleContext) {
            class LinearPredicate extends MaterialRules.FullLazyAbstractPredicate {
                LinearPredicate() {
                    super(materialRuleContext);
                }

                double scale() {
                    return Math.sqrt(k_x*k_x + k_y*k_y + k_z*k_z);
                }
                public boolean test() {
                    int x = ((MaterialRuleContextAccess)(Object)(this.context)).getBlockX();
                    int y = ((MaterialRuleContextAccess)(Object)(this.context)).getBlockY();
                    int z = ((MaterialRuleContextAccess)(Object)(this.context)).getBlockZ();
                    double res = (k_x * x + k_y * y + k_z * z)/(separation*scale());
                    res = res - Math.floor(res);
                    return (res > min) && (res < max);
                }
            }
            return new LinearPredicate();
        }
    }

    public record CheckerboardCondition(int size) implements MaterialRules.MaterialCondition
    {
        public static final CodecHolder<CheckerboardCondition> CODEC = CodecHolder.of(RecordCodecBuilder.mapCodec(instance -> instance.group(
                (Codec.INT.fieldOf("size").orElse(1).forGetter(a -> a.size))
        ).apply(instance, CheckerboardCondition::new)));

        @Override
        public CodecHolder<? extends MaterialRules.MaterialCondition> codec() {
            return CODEC;
        }

        @Override
        public MaterialRules.BooleanSupplier apply(MaterialRules.MaterialRuleContext materialRuleContext) {
            class CheckerboardPredicate extends MaterialRules.FullLazyAbstractPredicate {
                CheckerboardPredicate() {
                    super(materialRuleContext);
                }

                public int sign(int coord) {
                    if (coord < 0) return -sign(-1-coord);
                    return ((coord / size) % 2)*2 - 1;
                }
                public boolean test() {
                    int x = sign(((MaterialRuleContextAccess)(Object)(this.context)).getBlockX());
                    int z = sign(((MaterialRuleContextAccess)(Object)(this.context)).getBlockZ());
                    return (x > 0) ^ (z > 0);
                }
            }

            return new CheckerboardPredicate();
        }
    }

    public record TextCondition(int font_size, int char_spacing, int line_spacing, int max_width, String text, TextData data) implements MaterialRules.MaterialCondition
    {
        public static final CodecHolder<TextCondition> CODEC = CodecHolder.of(RecordCodecBuilder.mapCodec(instance -> instance.group(
                (Codec.INT.fieldOf("font_size").orElse(1).forGetter(a -> a.font_size)),
                (Codec.INT.fieldOf("char_spacing").orElse(1).forGetter(a -> a.char_spacing)),
                (Codec.INT.fieldOf("line_spacing").orElse(1).forGetter(a -> a.line_spacing)),
                (Codec.INT.fieldOf("max_width").orElse(Integer.MAX_VALUE).forGetter(a -> a.max_width)),
                (Codec.STRING.fieldOf("text").forGetter(a -> a.text))
        ).apply(instance, (font_size, char_spacing, line_spacing, max_width, text) ->
                new TextCondition(font_size, char_spacing, line_spacing, max_width, text,
                        TextData.genData(char_spacing, max_width, text)))));

        public int find(int x, int line_num) {
            int char_num = Collections.binarySearch(data.offsetMap().get(line_num), x);
            if (char_num < 0) char_num = -char_num - 2;
            return char_num;
        }

        @Override
        public CodecHolder<? extends MaterialRules.MaterialCondition> codec() {
            return CODEC;
        }

        @Override
        public MaterialRules.BooleanSupplier apply(MaterialRules.MaterialRuleContext materialRuleContext) {
            class CheckerboardPredicate extends MaterialRules.FullLazyAbstractPredicate {
                final List<List<Integer>> textmap;
                final List<List<Character>> charmap;
                final int longest_line;

                CheckerboardPredicate() {
                    super(materialRuleContext);
                    textmap = data.offsetMap();
                    charmap = data.charMap();
                    longest_line = data.longest_line();
                }

                public boolean test() {
                    int x = ((MaterialRuleContextAccess)(Object)(this.context)).getBlockX() / font_size;
                    int z = ((MaterialRuleContextAccess)(Object)(this.context)).getBlockZ() / font_size;
                    int factor = 8 + line_spacing;
                    if (x < 0 || z < 0 || x > longest_line || z >= factor * textmap.size()) return false;
                    int line_num = z / factor;
                    int char_num = find(x, line_num);
                    int char_z = z % factor;
                    if (char_z >= 8 || char_num < 0) return false;
                    int char_x = x - textmap.get(line_num).get(char_num);
                    return TextData.check(char_x, char_z, charmap.get(line_num).get(char_num));
                }
            }
            return new CheckerboardPredicate();
        }
    }

    public static final DeferredRegister<Codec<? extends MaterialRules.MaterialCondition>> MATERIAL_CONDITIONS = DeferredRegister.create(MOD_ID, RegistryKeys.MATERIAL_CONDITION);


    public static void registerConditions() {
        MATERIAL_CONDITIONS.register("linear", LinearCondition.CODEC::codec);
        MATERIAL_CONDITIONS.register("checkerboard", CheckerboardCondition.CODEC::codec);
        MATERIAL_CONDITIONS.register("text", TextCondition.CODEC::codec);
        MATERIAL_CONDITIONS.register();
    }
}
