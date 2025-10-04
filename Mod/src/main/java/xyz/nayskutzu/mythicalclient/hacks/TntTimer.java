package xyz.nayskutzu.mythicalclient.hacks;

import xyz.nayskutzu.mythicalclient.MythicalClientMod;
import xyz.nayskutzu.mythicalclient.utils.TNTCountdownRenderer;

public class TntTimer {
    
    public static boolean enabled;
    private static final TNTCountdownRenderer instance = new TNTCountdownRenderer();

    public static void main() {
        if (enabled) {
            enabled = false;
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.unregister(instance);
            MythicalClientMod.sendMessageToChat("&7TntTimer is now &cdisabled&7.",false);
        } else {
            enabled = true;
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(instance);
            MythicalClientMod.sendMessageToChat("&7TntTimer is now &aenabled&7.",false);
        }
    }
}
