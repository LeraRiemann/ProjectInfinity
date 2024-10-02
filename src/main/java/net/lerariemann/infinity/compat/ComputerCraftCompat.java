package net.lerariemann.infinity.compat;

import dan200.computercraft.shared.ModRegistry;
import net.minecraft.item.ItemStack;

public class ComputerCraftCompat {
    public static String checkPrintedPage(ItemStack itemStack) {
        var print = (itemStack.getComponents().get(ModRegistry.DataComponents.PRINTOUT.get()));
        if (print != null) {
            String string = "";
            for (var l : print.lines()) {
                string = string.concat(l.text());
            }
            return string;
        }
        return null;
    }
}
