package xyz.nayskutzu.mythicalclient.commands;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemArmor;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.client.Minecraft;
import xyz.nayskutzu.mythicalclient.MythicalClientMod;
import xyz.nayskutzu.mythicalclient.utils.FriendlyPlayers;

public class PlayerInfoCommand extends CommandBase {
    private static final Minecraft mc = Minecraft.getMinecraft();

    @Override
    public String getCommandName() {
        return "plinfo";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/plinfo <player>";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        if (args.length != 1) {
            MythicalClientMod.sendMessageToChat("&c&l[!] &cUsage: /plinfo <player>", false);
            return;
        }

        String targetName = args[0];
        EntityPlayer target = mc.theWorld.getPlayerEntityByName(targetName);

        if (target == null) {
            MythicalClientMod.sendMessageToChat("&c&l[!] &cPlayer not found!", false);
            return;
        }

        // Header with player name and status
        MythicalClientMod.sendMessageToChat("&8&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", false);
        String friendlyStatus = FriendlyPlayers.isFriendly(target.getName()) ? "&a[FRIENDLY]" : "&c[ENEMY]";
        MythicalClientMod.sendMessageToChat("&b&l[PLAYER INFO] &f" + target.getName() + " " + friendlyStatus, false);
        MythicalClientMod.sendMessageToChat("&8&m┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄", false);

        // Health bar visualization
        float healthPercent = (target.getHealth() / target.getMaxHealth()) * 100;
        StringBuilder healthBar = new StringBuilder("&8[");
        int bars = 20;
        int filledBars = (int) ((healthPercent / 100) * bars);
        for (int i = 0; i < bars; i++) {
            if (i < filledBars) {
                healthBar.append(healthPercent > 50 ? "&a│" : healthPercent > 25 ? "&e│" : "&c│");
            } else {
                healthBar.append("&8│");
            }
        }
        healthBar.append("&8]");
        
        // Stats section
        MythicalClientMod.sendMessageToChat("&7❤ Health: " + healthBar.toString() + " &7(&c" + String.format("%.1f&7/&c%.1f", target.getHealth(), target.getMaxHealth()) + "&7)", false);
        MythicalClientMod.sendMessageToChat("&7⌖ Position: &f" + String.format("%.1f, %.1f, %.1f", target.posX, target.posY, target.posZ), false);
        MythicalClientMod.sendMessageToChat("&7↔ Distance: &f" + String.format("%.1f", mc.thePlayer.getDistanceToEntity(target)) + " &7blocks", false);

        // Status effects
        StringBuilder status = new StringBuilder("&7⚡ Status: ");
        if (target.isSneaking()) status.append("&e[Sneaking] ");
        if (target.isSprinting()) status.append("&b[Sprinting] ");
        if (target.isInvisible()) status.append("&7[Invisible] ");
        if (status.toString().equals("&7⚡ Status: ")) status.append("&7None");
        MythicalClientMod.sendMessageToChat(status.toString(), false);

        // Armor section
        MythicalClientMod.sendMessageToChat("&8&m┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄", false);
        MythicalClientMod.sendMessageToChat("&b&l[ARMOR]", false);
        
        ItemStack[] armorInventory = target.inventory.armorInventory;
        String[] armorSlots = {"⛨ Boots", "⛨ Leggings", "⛨ Chestplate", "⛨ Helmet"};
        
        for (int i = 0; i < armorInventory.length; i++) {
            ItemStack armor = armorInventory[i];
            if (armor != null) {
                StringBuilder info = new StringBuilder();
                info.append("&7").append(armorSlots[i]).append(": &f");
                
                // Item name with color based on material
                String itemName = armor.getDisplayName();
                if (itemName.contains("Diamond")) {
                    info.append("&b");
                } else if (itemName.contains("Iron")) {
                    info.append("&f");
                } else if (itemName.contains("Gold")) {
                    info.append("&6");
                } else if (itemName.contains("Chain")) {
                    info.append("&8");
                } else if (itemName.contains("Leather")) {
                    info.append("&c");
                }
                info.append(itemName);
                
                // Protection value
                if (armor.getItem() instanceof ItemArmor) {
                    ItemArmor armorItem = (ItemArmor) armor.getItem();
                    info.append(" &8(&7Prot: &f").append(armorItem.getArmorMaterial().getDamageReductionAmount(armorItem.armorType)).append("&8)");
                }
                
                // Enchantments
                if (armor.isItemEnchanted()) {
                    info.append(" &9&l[");
                    if (EnchantmentHelper.getEnchantmentLevel(0, armor) > 0) {
                        info.append("P").append(EnchantmentHelper.getEnchantmentLevel(0, armor));
                    }
                    if (EnchantmentHelper.getEnchantmentLevel(34, armor) > 0) {
                        info.append(" U").append(EnchantmentHelper.getEnchantmentLevel(34, armor));
                    }
                    info.append("&9&l]");
                }
                
                MythicalClientMod.sendMessageToChat(info.toString(), false);
            } else {
                MythicalClientMod.sendMessageToChat("&7" + armorSlots[i] + ": &cNone", false);
            }
        }

        // Held item section
        MythicalClientMod.sendMessageToChat("&8&m┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄", false);
        MythicalClientMod.sendMessageToChat("&b&l[HELD ITEM]", false);
        
        ItemStack heldItem = target.getHeldItem();
        if (heldItem != null) {
            StringBuilder info = new StringBuilder("&7⚔ Item: &f");
            String itemName = heldItem.getDisplayName();
            
            // Color based on material
            if (itemName.contains("Diamond")) {
                info.append("&b");
            } else if (itemName.contains("Iron")) {
                info.append("&f");
            } else if (itemName.contains("Gold")) {
                info.append("&6");
            } else if (itemName.contains("Stone")) {
                info.append("&8");
            } else if (itemName.contains("Wooden")) {
                info.append("&6");
            }
            info.append(itemName);
            
            // Enchantments
            if (heldItem.isItemEnchanted()) {
                info.append(" &9&l[");
                if (EnchantmentHelper.getEnchantmentLevel(16, heldItem) > 0) {
                    info.append("S").append(EnchantmentHelper.getEnchantmentLevel(16, heldItem));
                }
                if (EnchantmentHelper.getEnchantmentLevel(34, heldItem) > 0) {
                    info.append(" U").append(EnchantmentHelper.getEnchantmentLevel(34, heldItem));
                }
                info.append("&9&l]");
            }
            
            MythicalClientMod.sendMessageToChat(info.toString(), false);
        } else {
            MythicalClientMod.sendMessageToChat("&7⚔ Item: &cNone", false);
        }

        // Footer
        MythicalClientMod.sendMessageToChat("&8&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", false);
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return true;
    }
} 