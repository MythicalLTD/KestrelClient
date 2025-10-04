package xyz.nayskutzu.mythicalclient.utils;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;

public class BowTracker {
    public static boolean isHoldingBow(EntityPlayer player) {
        if (player == null) return false;
        
        ItemStack mainHand = player.getHeldItem();
        return mainHand != null && mainHand.getItem() instanceof ItemBow;
    }

    public static int getArrowCount(EntityPlayer player) {
        if (player == null) return 0;
        
        int arrowCount = 0;
        for (ItemStack stack : player.inventory.mainInventory) {
            if (stack != null && stack.getItem().toString().contains("arrow")) {
                arrowCount += stack.stackSize;
            }
        }
        return arrowCount;
    }

    public static boolean isDrawingBow(EntityPlayer player) {
        return isHoldingBow(player) && player.isUsingItem();
    }

    public static float getBowPower(EntityPlayer player) {
        if (!isDrawingBow(player)) return 0.0f;
        
        int useTime = player.getItemInUseCount();
        float power = (float)(72000 - useTime) / 20.0F;
        power = (power * power + power * 2.0F) / 3.0F;
        
        if (power > 1.0F) {
            power = 1.0F;
        }
        
        return power;
    }
} 