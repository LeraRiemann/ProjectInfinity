package net.lerariemann.infinity.var;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.dynamic.CodecHolder;
import net.minecraft.world.gen.surfacebuilder.MaterialRules;
import net.lerariemann.infinity.mixin.MaterialRuleContextAccess;

public class ModMaterialConditions {
    record LinearCondition(double k_x, double k_y, double k_z, double min, double max, int separation) implements MaterialRules.MaterialCondition
    {
        static final CodecHolder<LinearCondition> CODEC = CodecHolder.of(RecordCodecBuilder.create(instance -> instance.group(
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

    record CheckerboardCondition(int size) implements MaterialRules.MaterialCondition
    {
        static final CodecHolder<CheckerboardCondition> CODEC = CodecHolder.of(RecordCodecBuilder.create(instance -> instance.group(
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

    public static void registerConditions() {
        Registry.register(Registries.MATERIAL_CONDITION, "infinity:linear", LinearCondition.CODEC.codec());
        Registry.register(Registries.MATERIAL_CONDITION, "infinity:checkerboard", CheckerboardCondition.CODEC.codec());
    }
}
