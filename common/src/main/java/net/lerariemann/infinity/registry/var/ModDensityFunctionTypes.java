package net.lerariemann.infinity.registry.var;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.architectury.registry.registries.DeferredRegister;
import net.lerariemann.infinity.util.InfinityMethods;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.dynamic.CodecHolder;
import net.minecraft.util.math.noise.DoublePerlinNoiseSampler;
import net.minecraft.util.math.random.CheckedRandom;
import net.minecraft.world.gen.densityfunction.DensityFunction;

import static net.lerariemann.infinity.InfinityMod.MOD_ID;

public class ModDensityFunctionTypes {
    interface Nonbinary extends DensityFunction {
        DensityFunction input();

        @Override
        default double sample(DensityFunction.NoisePos pos) {
            return this.apply(this.input().sample(pos));
        }

        @Override
        default void fill(double[] densities, EachApplier applier) {
            this.input().fill(densities, applier);
            for (int i = 0; i < densities.length; ++i) {
                densities[i] = this.apply(densities[i]);
            }
        }

        double apply(double var1);
    }

    record NonbinaryOperation(Type type, DensityFunction input, double minValue, double maxValue) implements Nonbinary
    {
        public static NonbinaryOperation create(Type type, DensityFunction input) {
            double d = input.minValue();
            double e = apply(type, d);
            double f = apply(type, input.maxValue());
            return new NonbinaryOperation(type, input, e, f);
        }

        private static double apply(Type type, double density) {
            return switch (type) {
                case SIN -> Math.sin(density);
                case COS -> Math.cos(density);
                case SQRT -> Math.sqrt(density);
                case INVERT -> (Math.abs(density) < 0.001 ? 1000*Math.signum(density) : 1.0 / density);
                case EXP -> (Math.exp(density));
                case LN -> Math.log(density);
            };
        }

        @Override
        public double apply(double density) {
            return NonbinaryOperation.apply(this.type, density);
        }

        @Override
        public NonbinaryOperation apply(DensityFunction.DensityFunctionVisitor densityFunctionVisitor) {
            return NonbinaryOperation.create(this.type, this.input.apply(densityFunctionVisitor));
        }

        @Override
        public CodecHolder<? extends DensityFunction> getCodecHolder() {
            return this.type.codecHolder;
        }

        enum Type
        {
            SIN("sin"),
            COS("cos"),
            SQRT("sqrt"),
            INVERT("invert"),
            LN("ln"),
            EXP("exp");

            private final String name;
            final CodecHolder<NonbinaryOperation> codecHolder = CodecHolder.of((DensityFunction.FUNCTION_CODEC.fieldOf("argument")).xmap(input -> create(this, input), NonbinaryOperation::input));

            Type(String name) {
                this.name = name;
            }
        }
    }

    record Coordinate(double scale, int axis) implements DensityFunction.Base {
        public static final CodecHolder<Coordinate> CODEC_HOLDER = CodecHolder.of(RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.DOUBLE.fieldOf("scale").orElse(1.0).forGetter(a -> a.scale),
                Codec.INT.fieldOf("axis").forGetter(a -> a.axis)).apply(
                instance, Coordinate::new)));

        @Override
        public double sample(NoisePos pos) {
            if (axis == -1 || scale == 0.0) return apply(pos);
            double d = apply(pos) * scale;
            d -= Math.floor(d);
            return (d - 0.5)*2*Math.PI;
        }

        @Override
        public double minValue() {
            return -1 * Math.PI;
        }

        @Override
        public double maxValue() {
            return Math.PI;
        }

        @Override
        public CodecHolder<? extends DensityFunction> getCodecHolder() {
            return CODEC_HOLDER;
        }

        public double apply(NoisePos pos) {
            return switch(axis) {
                case 1 -> pos.blockX(); //x
                case 2 -> pos.blockY(); //y
                case 3 -> pos.blockZ(); //z
                case 0 -> r(pos); //r
                case -1 -> Math.acos(pos.blockX() / r(pos))*(pos.blockZ() < 0 ? -1 : 1); //phi
                default -> 0.0;
            };
        }

        static double r(NoisePos pos) {
            double res = Math.sqrt(pos.blockX()*pos.blockX() + pos.blockZ()*pos.blockZ());
            return Math.max(res, 0.01);
        }
    }

    record Menger(double scale, int max_y) implements DensityFunction.Base {
        public static final CodecHolder<Menger> CODEC_HOLDER = CodecHolder.of(RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.DOUBLE.fieldOf("scale").orElse(1.0).forGetter(a -> a.scale),
                Codec.INT.fieldOf("max_y").orElse(0).forGetter(a -> a.max_y)).apply(
                instance, Menger::new)));

        @Override
        public double sample(NoisePos pos) {
            int x = pos.blockX();
            int y = pos.blockY();
            int z = pos.blockZ();
            return (y > max_y || check(x, z) || check(x, y) || check(y, z)) ? -scale : scale;
        }

        @Override
        public double minValue() {
            return -scale;
        }

        @Override
        public double maxValue() {
            return scale;
        }

        @Override
        public CodecHolder<? extends DensityFunction> getCodecHolder() {
            return CODEC_HOLDER;
        }

        public boolean check(int a, int b) {
            int a1 = Math.abs(a);
            int b1 = Math.abs(b);
            while (a1 > 0 && b1 > 0) {
                if (a1 % 3 == 1 && b1 % 3 == 1) return true;
                a1 /= 3;
                b1 /= 3;
            }
            return false;
        }
    }

    record Skygrid(double scale, int size, int separation) implements DensityFunction.Base {
        public static final CodecHolder<Skygrid> CODEC_HOLDER = CodecHolder.of(RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.DOUBLE.fieldOf("scale").orElse(1.0).forGetter(a -> a.scale),
                Codec.INT.fieldOf("size").orElse(1).forGetter(a -> a.size),
                Codec.INT.fieldOf("separation").orElse(3).forGetter(a -> a.separation)).apply(
                instance, Skygrid::new)));

        @Override
        public double sample(NoisePos pos) {
            int n = separation + size;
            int x = Math.abs(pos.blockX());
            int y = Math.abs(pos.blockY());
            int z = Math.abs(pos.blockZ());
            return (x % n < size && y % n < size && z % n < size) ? scale : -scale;
        }

        @Override
        public double minValue() {
            return -scale;
        }

        @Override
        public double maxValue() {
            return scale;
        }

        @Override
        public CodecHolder<? extends DensityFunction> getCodecHolder() {
            return CODEC_HOLDER;
        }
    }

    interface TileableStructure extends DensityFunction.Base {
        int size(int axis);
        boolean testBlock(int x, int y, int z);

        default int normalize(int x, int axis) {
            int a = Math.abs(x < 0 ? x+1 : x) % size(axis);
            return (x < 0) ? size(axis) - 1 - a : a;
        }

        @Override
        default double sample(NoisePos pos) {
            int x = normalize(pos.blockX(),0);
            int y = normalize(pos.blockY(),1);
            int z = normalize(pos.blockZ(),2);
            return testBlock(x, y, z) ? 1 : -1;
        }
        @Override
        default double minValue() {
            return -1;
        }
        @Override
        default double maxValue() {
            return 1;
        }
    }

    public enum Library implements TileableStructure {
        INSTANCE;
        static final CodecHolder<Library> CODEC_HOLDER = CodecHolder.of(MapCodec.unit(INSTANCE));

        @Override
        public int size(int axis) {
            return (axis == 1) ? 16 : 15;
        }

        @Override
        public boolean testBlock(int x, int y, int z) {
            int max_xz = Math.max(Math.abs(7 - x), Math.abs(7 - z));
            int min_xz = Math.min(Math.abs(7 - x), Math.abs(7 - z));
            if (max_xz == 7) {
                return y >= 3 || y == 0 || min_xz > 1; //walls
            }
            if (max_xz < 2) {
                return true; // central column 3*3
            }
            if (max_xz == 2 && min_xz == 1) return true; //ladders
            return y == 0; // floor
        }

        @Override
        public CodecHolder<? extends DensityFunction> getCodecHolder() {
            return CODEC_HOLDER;
        }
    }

    static DoublePerlinNoiseSampler sampler, sampler2;

    public record Classic(int sealevel) implements DensityFunction.Base {
        public static final CodecHolder<Classic> CODEC_HOLDER = CodecHolder.of(RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.INT.fieldOf("sealevel").orElse(64).forGetter(a -> a.sealevel)).apply(
                instance, Classic::new)));

        @Override
        public double sample(NoisePos pos) {
            return pos.blockY() < sample(pos.blockX(), pos.blockZ()) ? 1 : -1;
        }

        int sample(int x, int z) {
            double heightLow = sampler.sample(x * 1.3, 0, z * 1.3)*6 - 6;
            double heightHigh = sampler.sample(x * 1.3, 200, z * 1.3)*7.2 + 6;
            if (sampler2.sample(x, 0, z) > 0.0) {
                heightHigh = heightLow;
            }
            double heightResult = Math.max(heightLow, heightHigh) / 2;

            if (heightResult < 0.0) {
                heightResult *= 0.8;
            }

            return sealevel + (int)heightResult;
        }

        @Override
        public double minValue() {
            return -1;
        }
        @Override
        public double maxValue() { return 1; }
        @Override
        public CodecHolder<? extends DensityFunction> getCodecHolder() { return CODEC_HOLDER; }
    }


    public static final DeferredRegister<Codec<? extends DensityFunction>> DENSITY_FUNCTION_TYPES = DeferredRegister.create(MOD_ID, RegistryKeys.DENSITY_FUNCTION_TYPE);

    public static <T extends DensityFunction> void register(String name, CodecHolder<T> holder) {
        DENSITY_FUNCTION_TYPES.register(name, holder::codec);
    }

    public static void registerFunctions() {
        sampler = DoublePerlinNoiseSampler.create(new CheckedRandom(0L), -5, InfinityMethods.genOctaves(8));
        sampler2 = DoublePerlinNoiseSampler.create(new CheckedRandom(0L), -6, InfinityMethods.genOctaves(8));
        for (NonbinaryOperation.Type enum_ : NonbinaryOperation.Type.values()) {
            register(enum_.name, enum_.codecHolder);
        }
        register("coordinate", Coordinate.CODEC_HOLDER);
        register("menger", Menger.CODEC_HOLDER);
        register("skygrid", Skygrid.CODEC_HOLDER);
        register("library", Library.CODEC_HOLDER);
        register("classic", Classic.CODEC_HOLDER);
        DENSITY_FUNCTION_TYPES.register();
    }
}
