package net.lerariemann.infinity.mixin.core;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.util.core.NbtUtils;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.world.level.LevelProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;
import java.util.function.Consumer;

/** Removes infinite dimensions from level.dat to allow the mod to change and erase them easily without trailing data. */
@Mixin(LevelProperties.class)
public class LevelPropertiesMixin {
    @WrapOperation(method = "updateProperties", at = @At(value = "INVOKE", target = "Ljava/util/Optional;ifPresent(Ljava/util/function/Consumer;)V"))
    void inj(Optional<NbtElement> instance, Consumer<? super NbtElement> action, Operation<Void> original) {
        if (instance.isEmpty()) return;
        NbtCompound c = ((NbtCompound) instance.get());
        if (c.contains("dimensions")) {
            NbtCompound d = NbtUtils.getCompound(c, "dimensions");
            NbtCompound newD = new NbtCompound();
            d.getKeys().stream().filter(s -> !s.startsWith(InfinityMod.MOD_ID + ":")).forEach(key -> newD.put(key, d.get(key)));
            c.put("dimensions", newD);
        }
        action.accept(c);
    }
}
