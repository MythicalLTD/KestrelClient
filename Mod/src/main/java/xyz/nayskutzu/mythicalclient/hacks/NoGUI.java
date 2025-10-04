package xyz.nayskutzu.mythicalclient.hacks;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import xyz.nayskutzu.mythicalclient.MythicalClientMod;
import java.awt.Robot;
import java.awt.AWTException;
import java.awt.event.KeyEvent;

public class NoGUI {
    public static boolean enabled;
    private static final NoGUI instance = new NoGUI();

    public static void main() {
        System.out.println("NoGUI class booting up...");
        if (enabled) {
            enabled = false;
            System.out.println("NoGUI class disabled.");
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.unregister(instance);
            MythicalClientMod.sendToggleNotification("NoGUI", false);

        } else {
            enabled = true;
            System.out.println("NoGUI class enabled.");
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(instance);
            MythicalClientMod.sendToggleNotification("NoGUI", true);
        }
    }

    @SubscribeEvent
    public void onGuiOpen(net.minecraftforge.client.event.GuiOpenEvent event) {
        if (enabled && event.gui != null) {
            net.minecraft.client.Minecraft.getMinecraft().thePlayer.closeScreen();
            net.minecraft.client.Minecraft.getMinecraft().displayGuiScreen(null);
            try {
                Robot robot = new Robot();
                robot.keyPress(KeyEvent.VK_ESCAPE);
                robot.keyRelease(KeyEvent.VK_ESCAPE);
            } catch (AWTException e) {
                e.printStackTrace();
            }
            MythicalClientMod.sendMessageToChat("&7A GUI was &cclosed&7 because NoGUI is enabled.",false);
        }
    }

}
