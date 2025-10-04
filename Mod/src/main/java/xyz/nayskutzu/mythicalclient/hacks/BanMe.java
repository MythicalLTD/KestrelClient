package xyz.nayskutzu.mythicalclient.hacks;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiDisconnected;
import net.minecraft.util.IChatComponent;
import net.minecraft.client.multiplayer.WorldClient;

public class BanMe {
    public static boolean enabled = false;
    @SuppressWarnings("unused")
    private static final BanMe instance = new BanMe();

    public static void main() {
        System.out.println("BanMe class booting up...");
        if (enabled) {
            enabled = false;
            System.out.println("BanMe class disabled.");
        } else {
            enabled = true;
            System.out.println("BanMe class enabled.");
            executeBanMe("Unfair Advantage");
        }
    }

    public static void executeBanMe(String reason) {
        final Minecraft mc = Minecraft.getMinecraft();
        
        // Send a message before disconnecting
        
        // Use a safer approach - schedule the disconnection on the main thread
        mc.addScheduledTask(() -> {
            try {
                // First disconnect from the server safely
                if (mc.theWorld != null && mc.thePlayer != null) {
                    // Send disconnect packet
                    mc.theWorld.sendQuittingDisconnectingPacket();
                    
                    // Clear the world after a short delay to prevent rendering issues
                    mc.addScheduledTask(() -> {
                        try {
                            mc.loadWorld((WorldClient)null);
                            
                            // Show ban screen after world is cleared
                            mc.addScheduledTask(() -> {
                                try {
                                    IChatComponent message = new ChatComponentText(
                                        "§7⊲ §c§lGamster.org §7⊳\n" +
                                        "\n§cYou were §4§lPERMANENTLY§r§c banned from Gamster Network.\n" +
                                        "§7You were banned for §8⋙ §e" + reason + "\n" +
                                        "\n§7You can appeal this ban at §bhttps://unban.gamster.org"
                                    );
                                    mc.displayGuiScreen(new GuiDisconnected(new GuiMainMenu(), "connect.failed", message));
                                } catch (Exception e) {
                                    System.err.println("Error showing ban screen: " + e.getMessage());
                                    // Fallback to main menu if ban screen fails
                                    mc.displayGuiScreen(new GuiMainMenu());
                                }
                            });
                        } catch (Exception e) {
                            System.err.println("Error clearing world: " + e.getMessage());
                            // Fallback to main menu
                            mc.displayGuiScreen(new GuiMainMenu());
                        }
                    });
                } else {
                    // If not connected to a server, just show the ban screen
                    IChatComponent message = new ChatComponentText(
                        "§7⊲ §c§lGamster.org §7⊳\n" +
                        "\n§cYou were §4§lPERMANENTLY§r§c banned from Gamster Network.\n" +
                        "§7You were banned for §8⋙ §e" + reason + "\n" +
                        "\n§7You can appeal this ban at §bhttps://unban.gamster.org"
                    );
                    mc.displayGuiScreen(new GuiDisconnected(new GuiMainMenu(), "connect.failed", message));
                }
            } catch (Exception e) {
                System.err.println("Error in BanMe execution: " + e.getMessage());
                // Fallback to main menu
                mc.displayGuiScreen(new GuiMainMenu());
            }
        });
    }

    public static void executeBanMe() {
        executeBanMe("Unfair Advantage");
    }
} 