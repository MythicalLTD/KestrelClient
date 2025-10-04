package xyz.nayskutzu.mythicalclient.handlers;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientConnectedToServerEvent;

public class ConnectionHandler {
    private static final String PREFIX = "§8[§c§lGamster§8] ";
    private static final String SEPARATOR = "§8§m                                                  ";

    @SubscribeEvent
    public void onServerConnect(ClientConnectedToServerEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        
        // Wait a bit to ensure player is fully connected
        new Thread(() -> {
            try {
                Thread.sleep(8000); // Wait 2 seconds after connecting
                
                mc.addScheduledTask(() -> {
                    if (mc.getCurrentServerData() != null && 
                        mc.getCurrentServerData().serverIP != null && 
                        mc.getCurrentServerData().serverIP.toLowerCase().contains("test.gamster.org")) {
                            
                        String[] messages = {
                            SEPARATOR,
                            PREFIX + "§8▎ §7Staff Mode has been §aenabled",
                            PREFIX + "§8▎ §7Your rank has been hidden",
                            PREFIX + "§8▎ §7You are now marked as §5§lImmortal+",
                            PREFIX + "§8▎ §7Players can no longer see your true rank",
                            SEPARATOR
                        };
                        
                        for (String message : messages) {
                            mc.thePlayer.addChatMessage(new ChatComponentText(message));
                        }
                        
                    }
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }
} 