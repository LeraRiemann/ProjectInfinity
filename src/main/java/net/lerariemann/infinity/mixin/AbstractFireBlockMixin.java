package net.lerariemann.infinity.mixin;

import net.minecraft.block.AbstractFireBlock;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(AbstractFireBlock.class)
public class AbstractFireBlockMixin {
    @Overwrite
    private static boolean isOverworldOrNether(World world) {
        return world.getRegistryKey() != World.END;
    }
}
