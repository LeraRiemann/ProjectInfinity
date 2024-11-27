package net.lerariemann.infinity.item;

import net.lerariemann.infinity.util.WarpLogic;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;

public class HomeItem extends Item {
    public HomeItem(Settings settings) {
        super(settings);
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        if (user instanceof ServerPlayerEntity player) {
            WarpLogic.respawnAlive(player);
        }
        return super.finishUsing(stack, world, user);
    }
}
