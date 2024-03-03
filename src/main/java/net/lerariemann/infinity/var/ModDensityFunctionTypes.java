package net.lerariemann.infinity.var;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.lerariemann.infinity.InfinityMod;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.dynamic.CodecHolder;
import net.minecraft.world.gen.densityfunction.DensityFunction;

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
        public static final CodecHolder<Coordinate> CODEC_HOLDER = CodecHolder.of(RecordCodecBuilder.create(instance -> instance.group(
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
            return Math.sqrt(pos.blockX()*pos.blockX() + pos.blockZ()*pos.blockZ());
        }
    }

    record Menger(double scale, int max_y) implements DensityFunction.Base {
        public static final CodecHolder<Menger> CODEC_HOLDER = CodecHolder.of(RecordCodecBuilder.create(instance -> instance.group(
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
        public static final CodecHolder<Skygrid> CODEC_HOLDER = CodecHolder.of(RecordCodecBuilder.create(instance -> instance.group(
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

    public static void registerFunctions() {
        for (NonbinaryOperation.Type enum_ : NonbinaryOperation.Type.values()) {
            Registry.register(Registries.DENSITY_FUNCTION_TYPE, InfinityMod.MOD_ID + ":" + enum_.name, enum_.codecHolder.codec());
        }
        Registry.register(Registries.DENSITY_FUNCTION_TYPE, InfinityMod.MOD_ID + ":coordinate", Coordinate.CODEC_HOLDER.codec());
        Registry.register(Registries.DENSITY_FUNCTION_TYPE, InfinityMod.MOD_ID + ":menger", Menger.CODEC_HOLDER.codec());
        Registry.register(Registries.DENSITY_FUNCTION_TYPE, InfinityMod.MOD_ID + ":skygrid", Skygrid.CODEC_HOLDER.codec());
    }
}
