package net.lerariemann.infinity.var;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.architectury.registry.registries.DeferredRegister;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Pair;
import net.minecraft.util.dynamic.CodecHolder;
import net.minecraft.world.gen.surfacebuilder.MaterialRules;
import net.lerariemann.infinity.mixin.MaterialRuleContextAccess;

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

    public record TextCondition(int font_size, int char_spacing, int line_spacing, int max_width, String text, Pair<Integer, Pair<List<List<Integer>>, List<List<Character>>>> data) implements MaterialRules.MaterialCondition
    {
        public static final CodecHolder<TextCondition> CODEC = CodecHolder.of(RecordCodecBuilder.mapCodec(instance -> instance.group(
                (Codec.INT.fieldOf("font_size").orElse(1).forGetter(a -> a.font_size)),
                (Codec.INT.fieldOf("char_spacing").orElse(1).forGetter(a -> a.char_spacing)),
                (Codec.INT.fieldOf("line_spacing").orElse(1).forGetter(a -> a.line_spacing)),
                (Codec.INT.fieldOf("max_width").orElse(Integer.MAX_VALUE).forGetter(a -> a.max_width)),
                (Codec.STRING.fieldOf("text").forGetter(a -> a.text))
        ).apply(instance, (font_size, char_spacing, line_spacing, max_width, text) ->
                new TextCondition(font_size, char_spacing, line_spacing, max_width, text,
                        genData(char_spacing, max_width, text)))));

        public static int width(Character c, int char_spacing) {
            return storage.get(c).size() + char_spacing;
        }

        public static Pair<Integer, Pair<List<List<Integer>>, List<List<Character>>>> genData(int char_spacing, int max_width, String text) {
            int x = 0;
            int z = 0;
            int longest_line = 0;
            boolean bl;
            List<List<Integer>> textmap = new ArrayList<>();
            List<List<Character>> charmap = new ArrayList<>();
            textmap.add(new ArrayList<>());
            charmap.add(new ArrayList<>());
            int i = 0;
            while (i < text.length()) {
                bl = false;
                Character c = text.charAt(i);
                if (Objects.equals(c, '$')) {
                    if (i+1 < text.length() && (Objects.equals(text.charAt(i+1), 'n'))) {
                        i+=1;
                        bl = true;
                    }
                }
                if (x == 0 && Objects.equals(c, ' ')) {
                    i+=1;
                    continue;
                }
                if (!bl && storage.containsKey(c)) {
                    textmap.get(z).add(x);
                    charmap.get(z).add(c);
                    x += width(c, char_spacing);
                }
                if (i+1 == text.length()) {
                    longest_line = Math.max(longest_line, x);
                    break;
                }
                if (bl || x >= max_width) {
                    textmap.add(new ArrayList<>());
                    charmap.add(new ArrayList<>());
                    longest_line = Math.max(longest_line, x);
                    x = 0;
                    z += 1;
                }
                i++;
            }
            return new Pair<>(longest_line, new Pair<>(textmap, charmap));
        }

        public static final Map<Character, List<Integer>> storage = Map.ofEntries(
                Map.entry('a', List.of(0b00100000, 0b01010100, 0b01010100, 0b01010100, 0b01111000)),
                Map.entry('b', List.of(0b01111111, 0b01001000, 0b01000100, 0b01000100, 0b00111000)),
                Map.entry('c', List.of(0b00111000, 0b01000100, 0b01000100, 0b01000100, 0b00101000)),
                Map.entry('d', List.of(0b00111000, 0b01000100, 0b01000100, 0b01001000, 0b01111111)),
                Map.entry('e', List.of(0b00111000, 0b01010100, 0b01010100, 0b01010100, 0b01011000)),
                Map.entry('f', List.of(0b00000100, 0b01111110, 0b00000101, 0b00000101)),
                Map.entry('g', List.of(0b10011000, 0b10100100, 0b10100100, 0b10100100, 0b01111100)),
                Map.entry('h', List.of(0b01111111, 0b00001000, 0b00000100, 0b00000100, 0b01111000)),
                Map.entry('i', List.of(0b01111101)),
                Map.entry('j', List.of(0b01000000, 0b10000000, 0b10000000, 0b10000000, 0b01111101)),
                Map.entry('k', List.of(0b01111111, 0b00010000, 0b00101000, 0b01000100)),
                Map.entry('l', List.of(0b00111111, 0b01000000)),
                Map.entry('m', List.of(0b01111100, 0b00000100, 0b00011000, 0b00000100, 0b01111000)),
                Map.entry('n', List.of(0b01111100, 0b00000100, 0b00000100, 0b00000100, 0b01111000)),
                Map.entry('o', List.of(0b00111000, 0b01000100, 0b01000100, 0b01000100, 0b00111000)),
                Map.entry('p', List.of(0b11111100, 0b00101000, 0b00100100, 0b00100100, 0b00011000)),
                Map.entry('q', List.of(0b00011000, 0b00100100, 0b00100100, 0b00101000, 0b11111100)),
                Map.entry('r', List.of(0b01111100, 0b00001000, 0b00000100, 0b00000100, 0b00001000)),
                Map.entry('s', List.of(0b01001000, 0b01010100, 0b01010100, 0b01010100, 0b00100100)),
                Map.entry('t', List.of(0b00000100, 0b00111111, 0b01000100)),
                Map.entry('u', List.of(0b00111100, 0b01000000, 0b01000000, 0b01000000, 0b01111100)),
                Map.entry('v', List.of(0b00011100, 0b00100000, 0b01000000, 0b00100000, 0b00011100)),
                Map.entry('w', List.of(0b00111100, 0b01000000, 0b01110000, 0b01000000, 0b01111100)),
                Map.entry('x', List.of(0b01000100, 0b00101000, 0b00010000, 0b00101000, 0b01000100)),
                Map.entry('y', List.of(0b10011100, 0b10100000, 0b10100000, 0b10100000, 0b01111100)),
                Map.entry('z', List.of(0b01000100, 0b01100100, 0b01010100, 0b01001100, 0b01000100)),
                Map.entry('A', List.of(0b01111110, 0b00000101, 0b00000101, 0b00000101, 0b01111110)),
                Map.entry('B', List.of(0b01111111, 0b01000101, 0b01000101, 0b01000101, 0b00111010)),
                Map.entry('C', List.of(0b00111110, 0b01000001, 0b01000001, 0b01000001, 0b00100010)),
                Map.entry('D', List.of(0b01111111, 0b01000001, 0b01000001, 0b01000001, 0b00111110)),
                Map.entry('E', List.of(0b01111111, 0b01000101, 0b01000101, 0b01000001, 0b01000001)),
                Map.entry('F', List.of(0b01111111, 0b00000101, 0b00000101, 0b00000001, 0b00000001)),
                Map.entry('G', List.of(0b00111110, 0b01000001, 0b01000001, 0b01000101, 0b00111101)),
                Map.entry('H', List.of(0b01111111, 0b00000100, 0b00000100, 0b00000100, 0b01111111)),
                Map.entry('I', List.of(0b01000001, 0b01111111, 0b01000001)),
                Map.entry('J', List.of(0b00100000, 0b01000000, 0b01000000, 0b01000000, 0b00111111)),
                Map.entry('K', List.of(0b01111111, 0b00000100, 0b00000100, 0b00001010, 0b01110001)),
                Map.entry('L', List.of(0b01111111, 0b01000000, 0b01000000, 0b01000000, 0b01000000)),
                Map.entry('M', List.of(0b01111111, 0b00000010, 0b00000100, 0b00000010, 0b01111111)),
                Map.entry('N', List.of(0b01111111, 0b00000010, 0b00000100, 0b00001000, 0b01111111)),
                Map.entry('O', List.of(0b00111110, 0b01000001, 0b01000001, 0b01000001, 0b00111110)),
                Map.entry('P', List.of(0b01111111, 0b00000101, 0b00000101, 0b00000101, 0b00000010)),
                Map.entry('Q', List.of(0b00111110, 0b01000001, 0b01000001, 0b00100001, 0b01011110)),
                Map.entry('R', List.of(0b01111111, 0b00000101, 0b00000101, 0b00000101, 0b01111010)),
                Map.entry('S', List.of(0b00100010, 0b01000101, 0b01000101, 0b01000101, 0b00111001)),
                Map.entry('T', List.of(0b00000001, 0b00000001, 0b01111111, 0b00000001, 0b00000001)),
                Map.entry('U', List.of(0b00111111, 0b01000000, 0b01000000, 0b01000000, 0b00111111)),
                Map.entry('V', List.of(0b00001111, 0b00110000, 0b01000000, 0b00110000, 0b00001111)),
                Map.entry('W', List.of(0b01111111, 0b00100000, 0b00010000, 0b00100000, 0b01111111)),
                Map.entry('X', List.of(0b01110001, 0b00001010, 0b00000100, 0b00001010, 0b01110001)),
                Map.entry('Y', List.of(0b00000001, 0b00000010, 0b00000110, 0b00000010, 0b00000001)),
                Map.entry('Z', List.of(0b01100001, 0b01010001, 0b01001001, 0b01000101, 0b01000011)),
                Map.entry(' ', List.of(0b00000000, 0b00000000, 0b00000000, 0b00000000)),
                Map.entry('.', List.of(0b01000000)),
                Map.entry(',', List.of(0b11000000)),
                Map.entry(':', List.of(0b01000100)),
                Map.entry(';', List.of(0b11000100)),
                Map.entry('\'', List.of(0b00000011)),
                Map.entry('"', List.of(0b00000011, 0b00000000, 0b00000011)),
                Map.entry('`', List.of(0b00000001, 0b00000010)),
                Map.entry('!', List.of(0b01011111)),
                Map.entry('|', List.of(0b01111111)),
                Map.entry('~', List.of(0b00000010, 0b00000001, 0b00000001, 0b00000010, 0b00000010, 0b00000001)),
                Map.entry('@', List.of(0b01111100, 0b10000010, 0b10111010, 0b10101010, 0b10100010, 0b00111100)),
                Map.entry('#', List.of(0b00010100, 0b01111111, 0b00010100, 0b01111111, 0b00010100)),
                Map.entry('$', List.of(0b00100100, 0b00101010, 0b01101011, 0b00101010, 0b00010010)),
                Map.entry('%', List.of(0b01000011, 0b00110000, 0b00001000, 0b00000110, 0b01100001)),
                Map.entry('^', List.of(0b00000100, 0b00000010, 0b00000001, 0b00000010, 0b00000100)),
                Map.entry('*', List.of(0b00000101, 0b0000010, 0b00000101)),
                Map.entry('&', List.of(0b00110000, 0b01001010, 0b01011101, 0b00110010, 0b01001000)),
                Map.entry('(', List.of(0b00011100, 0b00100010, 0b01000001)),
                Map.entry(')', List.of(0b01000001, 0b00100010, 0b00011100)),
                Map.entry('[', List.of(0b01111111, 0b01000001, 0b01000001)),
                Map.entry(']', List.of(0b01000001, 0b01000001, 0b01111111)),
                Map.entry('{', List.of(0b00001000, 0b00110110, 0b01000001)),
                Map.entry('}', List.of(0b01000001, 0b00110110, 0b00001000)),
                Map.entry('+', List.of(0b00001000, 0b00001000, 0b00111110, 0b00001000, 0b00001000)),
                Map.entry('-', List.of(0b00001000, 0b00001000, 0b00001000, 0b00001000, 0b00001000)),
                Map.entry('=', List.of(0b00100100, 0b00100100, 0b00100100, 0b00100100, 0b00100100)),
                Map.entry('_', List.of(0b10000000, 0b10000000, 0b10000000, 0b10000000, 0b10000000)),
                Map.entry('/', List.of(0b01000000, 0b00110000, 0b00001000, 0b00000110, 0b00000001)),
                Map.entry('\\', List.of(0b00000001, 0b00000110, 0b00001000, 0b00110000, 0b01000000)),
                Map.entry('<', List.of(0b00001000, 0b00010100, 0b00100010, 0b01000001)),
                Map.entry('>', List.of(0b01000001, 0b00100010, 0b00010100, 0b00001000)),
                Map.entry('?', List.of(0b00000010, 0b00000001, 0b01010001, 0b00001001, 0b00000110)),
                Map.entry('1', List.of(0b01000000, 0b01000010, 0b01111111, 0b01000000, 0b01000000)),
                Map.entry('2', List.of(0b01100010, 0b01010001, 0b01001001, 0b01001001, 0b01100110)),
                Map.entry('3', List.of(0b00100010, 0b01000001, 0b01001001, 0b01001001, 0b00110110)),
                Map.entry('4', List.of(0b00011000, 0b00010100, 0b00010010, 0b00010001, 0b01111111)),
                Map.entry('5', List.of(0b00100111, 0b01000101, 0b01000101, 0b01000101, 0b00111001)),
                Map.entry('6', List.of(0b00111100, 0b01001010, 0b01001001, 0b01001001, 0b00110000)),
                Map.entry('7', List.of(0b00000011, 0b00000001, 0b01110001, 0b00001001, 0b00000111)),
                Map.entry('8', List.of(0b00110110, 0b01001001, 0b01001001, 0b01001001, 0b00110110)),
                Map.entry('9', List.of(0b00000110, 0b01001001, 0b01001001, 0b00101001, 0b00011110)),
                Map.entry('0', List.of(0b00111110, 0b01010001, 0b01001001, 0b01000101, 0b00111110))
        );

        public static boolean check(int x, int z, Character c) {
            List<Integer> lst = storage.get(c);
            if(x >= lst.size()) return false;
            int column = lst.get(x);
            return ((column >> z)%2) == 1;
        }

        public int find(int x, int line_num) {
            int char_num = Collections.binarySearch(data.getRight().getLeft().get(line_num), x);
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
                    textmap = data.getRight().getLeft();
                    charmap = data.getRight().getRight();
                    longest_line = data.getLeft();
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
                    return check(char_x, char_z, charmap.get(line_num).get(char_num));
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
