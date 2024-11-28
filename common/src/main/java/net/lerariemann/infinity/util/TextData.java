package net.lerariemann.infinity.util;

import net.lerariemann.infinity.features.TextFeature;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public record TextData(int longest_line, List<List<Integer>> offsetMap, List<List<Character>> charMap) {
    public int getLines() {
        return offsetMap.size();
    }
    public int getLineLen(int i) {
        return offsetMap.get(i).size();
    }

    public static int width(Character c, int char_spacing) {
        return storage.get(c).size() + char_spacing;
    }

    public static boolean check(int x, int z, Character c) {
        List<Integer> lst = TextData.storage.get(c);
        if(x >= lst.size()) return false;
        return ((lst.get(x) >> z)%2) == 1;
    }

    public static BlockPos mutate(BlockPos blockPos, int ori, int a, int b) { //for features and surface rules
        if (((ori/6)%2) == 1) {
            a*=-1;
        }
        if (((ori/12)%2) == 1) {
            b*=-1;
        }
        List<Integer> lst = switch (ori % 6) {
            case 0 -> List.of(0, a, b);
            case 1 -> List.of(b, 0, a);
            case 2 -> List.of(a, b, 0);
            case 3 -> List.of(0, b, a);
            case 4 -> List.of(a, 0, b);
            default -> List.of(b, a, 0);
        };
        return blockPos.add(lst.get(0), lst.get(1), lst.get(2));
    }

    public static BlockPos offset(int down, int along, Polarization pol) { //for structures as they introduce symmetry
        return switch (pol) { // direction = positive x
            case FLAT -> new BlockPos(along, 0, down);
            case FLAT_REVERSE -> new BlockPos(along, 0, -down);
            case STANDING -> new BlockPos(along, -down, 0);
            case STANDING_REVERSE -> new BlockPos(along, down, 0);
            case UP -> new BlockPos(0, along, -down);
            case DOWN -> new BlockPos(0, -along, down);
        };
    }
    public static BlockPos offset(int down, int along, Polarization pol, Direction dir) {
        BlockPos offset = offset(down, along, pol);
        return switch (dir) {
            case Direction.WEST -> new BlockPos(-offset.getX(), offset.getY(), -offset.getZ());
            case Direction.SOUTH -> new BlockPos(-offset.getZ(), offset.getY(), offset.getX());
            case Direction.NORTH -> new BlockPos(offset.getZ(), offset.getY(), -offset.getX());
            default -> offset;
        };
    }

    public enum Polarization {
        FLAT(0),
        FLAT_REVERSE(1),
        STANDING(2),
        STANDING_REVERSE(3),
        UP(4),
        DOWN(5);
        public final int id;
        Polarization(final int i) {
            id = i;
        }
        public static Polarization of(int i) {
            return switch (i) {
                case 1 -> FLAT_REVERSE;
                case 2 -> STANDING;
                case 3 -> STANDING_REVERSE;
                case 4 -> UP;
                case 5 -> DOWN;
                default -> FLAT;
            };
        }
    }

    public static TextData genData(int char_spacing, int max_width, String text) {
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
            if (c.equals('$')) {
                if (i+1 < text.length() && (Objects.equals(text.charAt(i+1), 'n'))) {
                    i+=1;
                    bl = true;
                }
            }
            if (x == 0 && c.equals(' ')) {
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
        return new TextData(longest_line, textmap, charmap);
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
}
