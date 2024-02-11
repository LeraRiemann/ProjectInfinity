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
            INVERT("invert");

            private final String name;
            final CodecHolder<NonbinaryOperation> codecHolder = CodecHolder.of((DensityFunction.FUNCTION_CODEC.fieldOf("argument")).xmap(input -> create(this, input), NonbinaryOperation::input));

            Type(String name) {
                this.name = name;
            }
        }
    }

    record Coordinate(double scale, int axis) implements DensityFunction.Base {
        public static final CodecHolder<Coordinate> CODEC_HOLDER = CodecHolder.of(RecordCodecBuilder.create(instance -> instance.group(
                Codec.DOUBLE.fieldOf("scale").forGetter(a -> a.scale),
                Codec.INT.fieldOf("axis").forGetter(a -> a.axis)).apply(
                instance, Coordinate::new)));

        @Override
        public double sample(NoisePos pos) {
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
                case 1 -> pos.blockX();
                case 2 -> pos.blockY();
                case 3 -> pos.blockZ();
                case 0 -> Math.sqrt(pos.blockX()*pos.blockX() + pos.blockZ()*pos.blockZ());
                default -> 0.0;
            };
        }
    }

    public static void registerFunctions() {
        for (NonbinaryOperation.Type enum_ : NonbinaryOperation.Type.values()) {
            Registry.register(Registries.DENSITY_FUNCTION_TYPE, InfinityMod.MOD_ID + ":" + enum_.name, enum_.codecHolder.codec());
        }
        Registry.register(Registries.DENSITY_FUNCTION_TYPE, InfinityMod.MOD_ID + ":coordinate", Coordinate.CODEC_HOLDER.codec());
    }
}
