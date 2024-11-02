package net.lerariemann.infinity.block.custom;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Random;

public class BookBoxBlock extends Block {
    public BookBoxBlock(AbstractBlock.Settings settings) {
        super(settings);
    }

    public String title(BlockPos pos) {
        return pos.getX() + "." + pos.getY() + "." + pos.getZ();
    }

    public String text(BlockPos pos) {
        Random r = new Random(pos.getX() + 30000000L*(pos.getZ() + 30000000L*pos.getY()));
        String s = "QWERTYUIOPASDFGHJKLZXCVBNMqwertyuiopasdfghjklzxcvbnm1234567890.^&*№+-_~@$%()!?";
        StringBuilder res = new StringBuilder();
        for (int j = 0; j<256; j++) {
            res.append(s.charAt(r.nextInt(s.length())));
        }
        return res.toString();
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!world.isClient) {
            ItemStack itemStack1 = Items.WRITTEN_BOOK.getDefaultStack();
            NbtCompound compound = itemStack1.getOrCreateNbt();
            compound.putString("author", "§kLeraRiemann");
            NbtList lst = new NbtList();
            lst.add(NbtString.of(text(pos)));
            compound.put("pages", lst);
            compound.putString("title", title(pos));
            player.getInventory().insertStack(itemStack1);
        }
        return ActionResult.SUCCESS;
    }
}
