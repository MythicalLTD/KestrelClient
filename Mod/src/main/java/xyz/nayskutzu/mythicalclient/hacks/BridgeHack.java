package xyz.nayskutzu.mythicalclient.hacks;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import xyz.nayskutzu.mythicalclient.MythicalClientMod;
import xyz.nayskutzu.mythicalclient.commands.SafeWalkCommand;
import xyz.nayskutzu.mythicalclient.event.SafeWalk;

public class BridgeHack {
    public static boolean enabled;

    public static void main() {
        System.out.println("Bridge hack started...");
        // Bridge hack code here
        if (enabled) {
            enabled = false;
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.unregister(SafeWalk.instance);
            MythicalClientMod.sendMessageToChat("&7SafeWalk is now &cdisabled&7.",false);
        } else {
            enabled = true;
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(SafeWalk.instance);
            MythicalClientMod.KeyBindSafewalk = new KeyBinding("Toggle Safewalk", 47, "MythicalClient");
            ClientRegistry.registerKeyBinding(MythicalClientMod.KeyBindSafewalk);
            ClientCommandHandler.instance.registerCommand(SafeWalkCommand.instance);
            MythicalClientMod.sendMessageToChat("&7SafeWalk is now &aenabled&7.",false);
        }
        System.out.println("Bridge hack completed.");
    }

}
