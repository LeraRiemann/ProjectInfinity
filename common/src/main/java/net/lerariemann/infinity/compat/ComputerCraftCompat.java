package net.lerariemann.infinity.compat;

import dan200.computercraft.shared.ModRegistry;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

public class ComputerCraftCompat {
    public static boolean isPrintedPage(Item item) {
        return ModRegistry.Items.PRINTED_PAGE.get().equals(item);
    }

    public static String checkPrintedPage(NbtCompound itemStack) {
        String print = (itemStack.getString("Text0"));
        if (print != null) {
            String text = itemStack.getString("Text0") +
                    itemStack.getString("Text1") +
                    itemStack.getString("Text2") +
                    itemStack.getString("Text3") +
                    itemStack.getString("Text4") +
                    itemStack.getString("Text5") +
                    itemStack.getString("Text6") +
                    itemStack.getString("Text7") +
                    itemStack.getString("Text8") +
                    itemStack.getString("Text9") +
                    itemStack.getString("Text10") +
                    itemStack.getString("Text11") +
                    itemStack.getString("Text12") +
                    itemStack.getString("Text13") +
                    itemStack.getString("Text14") +
                    itemStack.getString("Text15") +
                    itemStack.getString("Text16") +
                    itemStack.getString("Text17") +
                    itemStack.getString("Text18") +
                    itemStack.getString("Text19") +
                    itemStack.getString("Text20");
            return text.strip();
        }
        return "";
    }
}
