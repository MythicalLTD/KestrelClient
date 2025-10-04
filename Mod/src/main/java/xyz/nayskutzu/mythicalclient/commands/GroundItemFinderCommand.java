package xyz.nayskutzu.mythicalclient.commands;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import xyz.nayskutzu.mythicalclient.MythicalClientMod;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroundItemFinderCommand extends CommandBase {
    private static final Minecraft mc = Minecraft.getMinecraft();
    private static final int SEARCH_RADIUS = 50; // blocks

    @Override
    public String getCommandName() {
        return "grounditems";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/grounditems [radius]";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        try {
            int radius = SEARCH_RADIUS;
            if (args.length > 0) {
                try {
                    radius = Integer.parseInt(args[0]);
                    if (radius < 1 || radius > 100) {
                        MythicalClientMod.sendMessageToChat("&c&l[!] &cRadius must be between 1 and 100 blocks!", false);
                        return;
                    }
                } catch (NumberFormatException e) {
                    MythicalClientMod.sendMessageToChat("&c&l[!] &cInvalid radius! Using default: " + SEARCH_RADIUS, false);
                }
            }

            // Get all items within radius
            List<EntityItem> nearbyItems = new ArrayList<>();
            Map<String, ItemInfo> itemGroups = new HashMap<>();

            for (Object obj : mc.theWorld.loadedEntityList) {
                if (obj instanceof EntityItem) {
                    EntityItem item = (EntityItem) obj;
                    double distance = mc.thePlayer.getDistanceToEntity(item);
                    if (distance <= radius) {
                        nearbyItems.add(item);
                        
                        // Group similar items
                        ItemStack stack = item.getEntityItem();
                        String itemKey = stack.getDisplayName();
                        
                        ItemInfo info = itemGroups.getOrDefault(itemKey, new ItemInfo());
                        info.count += stack.stackSize;
                        info.locations.add(new Location(item.posX, item.posY, item.posZ, distance));
                        itemGroups.put(itemKey, info);
                    }
                }
            }

            if (nearbyItems.isEmpty()) {
                MythicalClientMod.sendMessageToChat("&c&l[!] &cNo items found within " + radius + " blocks!", false);
                return;
            }

            // Header
            MythicalClientMod.sendMessageToChat("&8&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", false);
            MythicalClientMod.sendMessageToChat("&b&l[GROUND ITEMS] &7(Radius: &f" + radius + " &7blocks)", false);
            MythicalClientMod.sendMessageToChat("&8&m┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄", false);

            // Sort items by distance
            itemGroups.forEach((name, info) -> {
                // Item header with total count
                String itemColor = getItemColor(name);
                MythicalClientMod.sendMessageToChat(
                    itemColor + "✦ " + name + " &8(&7x" + info.count + "&8)", false
                );

                // Sort locations by distance
                info.locations.sort((a, b) -> Double.compare(a.distance, b.distance));

                // Show up to 3 closest locations
                for (int i = 0; i < Math.min(3, info.locations.size()); i++) {
                    Location loc = info.locations.get(i);
                    MythicalClientMod.sendMessageToChat(
                        String.format("  &8└ &7%.1f, %.1f, %.1f &8(&f%.1f &7blocks&8)",
                            loc.x, loc.y, loc.z, loc.distance),
                        false
                    );
                }

                // If there are more locations, show count
                if (info.locations.size() > 3) {
                    MythicalClientMod.sendMessageToChat(
                        "  &8└ &7... and " + (info.locations.size() - 3) + " more locations",
                        false
                    );
                }
            });

            // Footer with total count
            MythicalClientMod.sendMessageToChat("&8&m┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄", false);
            MythicalClientMod.sendMessageToChat(
                String.format("&7Total Items: &f%d &7in &f%d &7locations", 
                    itemGroups.values().stream().mapToInt(info -> info.count).sum(),
                    nearbyItems.size()),
                false
            );
            MythicalClientMod.sendMessageToChat("&8&m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", false);

        } catch (Exception e) {
            MythicalClientMod.sendMessageToChat("&c&l[!] &cError while finding items: " + e.getMessage(), false);
        }
    }

    private String getItemColor(String itemName) {
        if (itemName.contains("Diamond")) return "&b";
        if (itemName.contains("Iron")) return "&f";
        if (itemName.contains("Gold")) return "&6";
        if (itemName.contains("Emerald")) return "&a";
        if (itemName.contains("Stone")) return "&8";
        if (itemName.contains("Wooden")) return "&6";
        if (itemName.contains("Leather")) return "&c";
        return "&f";
    }

    private static class ItemInfo {
        int count = 0;
        List<Location> locations = new ArrayList<>();
    }

    private static class Location {
        double x, y, z, distance;

        Location(double x, double y, double z, double distance) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.distance = distance;
        }
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