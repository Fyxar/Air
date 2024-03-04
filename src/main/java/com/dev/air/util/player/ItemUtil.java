package com.dev.air.util.player;

import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

import static com.dev.air.util.MinecraftInstance.*;

public class ItemUtil {

    public static int searchBlock() {
        for(int i = 36; i < 45; ++i) {
            ItemStack stack = mc.player.inventoryContainer.getSlot(i).getStack();
            if (stack != null && stack.getItem() instanceof ItemBlock) {
                return i;
            }
        }

        return -1;
    }

}
