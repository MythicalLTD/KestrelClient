package xyz.nayskutzu.mythicalclient.event;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import org.lwjgl.input.Keyboard;
import xyz.nayskutzu.mythicalclient.MythicalClientMod;
import xyz.nayskutzu.mythicalclient.utils.Delay;
import xyz.nayskutzu.mythicalclient.utils.MythicalPlayer;

public class SafeWalk {
    public static SafeWalk instance = new SafeWalk();
    private Minecraft mc = Minecraft.getMinecraft();
    private MythicalClientMod safewalk = MythicalClientMod.instance;
    private MythicalPlayer player = MythicalPlayer.instance;

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        try {
            if (mc == null || mc.gameSettings == null || mc.gameSettings.keyBindSneak == null) {
                return;
            }

            if (MythicalClientMod.KeyBindSafewalk != null && MythicalClientMod.KeyBindSafewalk.isPressed()) {
                if (safewalk != null) {
                    safewalk.toggle();
                    safewalk.sendToggle("BridgeNoLagAssist", "on", "off", safewalk.isToggled());
                    
                    if (!safewalk.isToggled()) {
                        KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), false);
                    }
                }
            }

            if (player != null) {
                player.setUserSneaking(Keyboard.isKeyDown(mc.gameSettings.keyBindSneak.getKeyCode()));
            }
        } catch (Exception e) {
            // Silently fail rather than crash
        }
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        try {
            if (mc == null || mc.thePlayer == null || safewalk == null || player == null) {
                return;
            }

            if (event.player == mc.thePlayer && safewalk.isToggled()) {
                player.onTick();
            }
        } catch (Exception e) {
            // Silently fail rather than crash
        }
    }

    @SubscribeEvent
    public void onEntityJump(LivingEvent.LivingJumpEvent event) {
        try {
            if (mc == null || mc.thePlayer == null || player == null || event.entity == null) {
                return;
            }

            if (event.entity == mc.thePlayer) {
                player.onJump();
            }
        } catch (Exception e) {
            // Silently fail rather than crash
        }
    }

    @SubscribeEvent
    public void onConnect(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        try {
            if (MythicalClientMod.ToggleSneak) {
                new Delay(2000) {
                    @Override
                    public void onTick() {
                        try {
                            MythicalClientMod.sendMessageToChat("Toggled sneak is installed please uninstall to use safewalk!", false);
                        } catch (Exception e) {
                            // Silently fail rather than crash
                        }
                    }
                };
            }
        } catch (Exception e) {
            // Silently fail rather than crash
        }
    }

}
