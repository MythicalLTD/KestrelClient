package xyz.nayskutzu.mythicalclient;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLModDisabledEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import xyz.nayskutzu.mythicalclient.data.MemoryStorageDriveData;
import xyz.nayskutzu.mythicalclient.utils.ChatColor;
import xyz.nayskutzu.mythicalclient.utils.Config;
import xyz.nayskutzu.mythicalclient.utils.NotificationType;
import xyz.nayskutzu.mythicalclient.v2.ChatServer;
import xyz.nayskutzu.mythicalclient.v2.WebServer;
import xyz.nayskutzu.mythicalclient.gui.ToggleGui;
import net.minecraftforge.fml.common.Loader;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;

import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import xyz.nayskutzu.mythicalclient.commands.*;
import xyz.nayskutzu.mythicalclient.hacks.Aimbot;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;

@Mod(modid = "mythicalclient", clientSideOnly = true, useMetadata = true)
public class MythicalClientMod {
    public static boolean ToggleSneak = false;
    private static final Logger LOGGER = LogManager.getLogger("[KestrelClient]");
    public static MythicalClientMod instance = new MythicalClientMod();
    public static KeyBinding KeyBindSafewalk;
    public static KeyBinding KeyBindDashboard;
    public static KeyBinding KeyBindPlayerInfo;
    public static KeyBinding KeyBindGroundItems;
    public static KeyBinding KeyBindBedDefence;
    public static KeyBinding KeyBindAimbot;
    public static KeyBinding KeyBindToggleGui;
    public static KeyBinding KeyBindConsole;
    private static Config config;
    private boolean toggled = false;
    public static boolean injectDashboard = false;

    public static int port;
    public static int chatPort;
    public static MemoryStorageDriveData data = new MemoryStorageDriveData();

    @Mod.EventHandler
    public void onPreInit(FMLPreInitializationEvent event) {
        config.updateConfig(event.getSuggestedConfigurationFile(), true);
        port = 9865;
        chatPort = 9866;
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        try {
            // Register keybindings
            KeyBindPlayerInfo = new KeyBinding("Player Info", Keyboard.KEY_P, "KestrelClient");
            KeyBindGroundItems = new KeyBinding("Ground Items", Keyboard.KEY_G, "KestrelClient");
            KeyBindBedDefence = new KeyBinding("Bed Defence", Keyboard.KEY_B, "KestrelClient");

            ClientRegistry.registerKeyBinding(KeyBindPlayerInfo);
            ClientRegistry.registerKeyBinding(KeyBindGroundItems);
            ClientRegistry.registerKeyBinding(KeyBindBedDefence);

            if (injectDashboard) {
                KeyBindAimbot = new KeyBinding("Aimbot", Keyboard.KEY_R, "KestrelClient");
                KeyBindToggleGui = new KeyBinding("Toggle GUI", Keyboard.KEY_LCONTROL, "KestrelClient");
                KeyBindConsole = new KeyBinding("Console Overlay", Keyboard.KEY_H, "KestrelClient");

                ClientRegistry.registerKeyBinding(KeyBindConsole);
                ClientRegistry.registerKeyBinding(KeyBindToggleGui);
                ClientRegistry.registerKeyBinding(KeyBindAimbot);
            }

            // Start web server in a separate thread to avoid blocking the main thread
            new Thread(() -> {
                try {
                    int attemptPort = port;
                    while (isPortInUse(attemptPort)) {
                        attemptPort++;
                    }
                    port = attemptPort;

                    WebServer webServer = new WebServer(port);
                    webServer.start();
                    LOGGER.info("Web server started on port " + port);

                    MythicalClientMod.data.put("name", "NaysKutzu");
                    MythicalClientMod.data.put("uuid", "PLM");
                    MythicalClientMod.data.put("version", "1.8.9");
                } catch (IOException e) {
                    LOGGER.error("Failed to start web server", e);
                }
            }, "KestrelClient-WebServer").start();

            // Start chat server in a separate thread to avoid blocking the main thread
            new Thread(() -> {
                try {
                    int attemptChatPort = chatPort;
                    while (isPortInUse(attemptChatPort)) {
                        attemptChatPort++;
                    }
                    chatPort = attemptChatPort;

                    ChatServer chatServer = new ChatServer(chatPort);
                    chatServer.start();
                    LOGGER.info("Chat server started on port " + chatPort);
                } catch (Exception e) {
                    LOGGER.error("Failed to start chat server", e);
                }
            }, "KestrelClient-ChatServer").start();

            // Register commands
            registerCommands();

            // Register custom console appender
            try {
                xyz.nayskutzu.mythicalclient.gui.ConsoleAppender appender = xyz.nayskutzu.mythicalclient.gui.ConsoleAppender
                        .create("MythicalConsole");
                appender.start();
                ((org.apache.logging.log4j.core.Logger) org.apache.logging.log4j.LogManager.getRootLogger())
                        .addAppender(appender);
                LOGGER.info("Console overlay appender registered.");
            } catch (Throwable t) {
                LOGGER.warn("Failed to register console overlay appender: " + t.getMessage());
            }

            // Register event handlers
            MinecraftForge.EVENT_BUS.register(this);

            LOGGER.info("KestrelClient is initialized");
        } catch (Exception e) {
            LOGGER.error("Error during initialization", e);
        }
    }

    private void registerCommands() {
        net.minecraftforge.client.ClientCommandHandler commandHandler = net.minecraftforge.client.ClientCommandHandler.instance;

        commandHandler.registerCommand(new AntiCheatCommand());
        commandHandler.registerCommand(new FakeStaffCommand());
        commandHandler.registerCommand(new BanMeCommand());
        commandHandler.registerCommand(new DutyCommand());
        commandHandler.registerCommand(new PlayerInfoCommand());
        commandHandler.registerCommand(new ShowStatsCommand());
        commandHandler.registerCommand(new GroundItemFinderCommand());
        commandHandler.registerCommand(new BedDefenceInfoCommand());
        commandHandler.registerCommand(new xyz.nayskutzu.mythicalclient.commands.NotificationCommand());
    }

    public void sendHelp() {
        this.sendChat(ChatFormatting.LIGHT_PURPLE + "MythicalClient Help (SafeWalk)");
        this.sendChat(ChatFormatting.GRAY + "Commands:");
        this.sendChat(ChatFormatting.GRAY + "/safewalk mode - Change the mode");
        this.sendChat(ChatFormatting.GRAY + "/safewalk chat - Toggle chat messages");
        this.sendChat(ChatFormatting.GRAY + "/safewalk click - Toggle auto-click");
        this.sendChat(ChatFormatting.GRAY + "/safewalk fall - Toggle auto-disable on fall");
        this.sendChat(ChatFormatting.GRAY + "/safewalk jump - Toggle auto-disable on jump");
        this.sendChat(ChatFormatting.GRAY + "/ac - Toggle anticheat");
        this.sendChat(ChatFormatting.GRAY + "/banme - Ban yourself");
        this.sendChat(ChatFormatting.GRAY + "/duty - Toggle duty mode");
        this.sendChat(ChatFormatting.GRAY + "/playerinfo - Get player info");
        this.sendChat(ChatFormatting.GRAY + "/showstats - Get player stats");
        this.sendChat(ChatFormatting.GRAY + "/plinfo - Get player info (P)");
        this.sendChat(ChatFormatting.GRAY + "/grounditems - Get ground items (G)");
        this.sendChat(ChatFormatting.GRAY + "/beddefence - Get bed defence info (B)");
        this.sendChat(ChatFormatting.GRAY + "/notify - Manage notification settings");
        this.sendChat(ChatFormatting.GRAY + "Press R - Toggle Aimbot (continuous tracking)");
        this.sendChat(ChatFormatting.GRAY + "Press LCONTROL - Toggle GUI (L)");
        this.sendChat(ChatFormatting.GRAY + "Press H - Open Console Overlay");
    }

    @Mod.EventHandler
    public void onPostInit(FMLPostInitializationEvent event) {
        if (Loader.isModLoaded("ToggleSneak")) {
            ToggleSneak = true;
        }
    }

    @Mod.EventHandler
    public void FMLModDisabledEvent(FMLModDisabledEvent event) {
        config.saveConfig();
    }

    public static void sendMessageToChat(String message, boolean raw) {
        if (Minecraft.getMinecraft().thePlayer == null || Minecraft.getMinecraft().theWorld == null) {
            LOGGER.debug("Player or world is null, cannot send chat message.");
            return;
        }

        String playerName = Minecraft.getMinecraft().thePlayer.getName();
        String formattedMessage = message.replace("%player%", playerName);

        if (!raw) {
            formattedMessage = "&7[&5&lKestrel&7&lClient&7] ➡ " + formattedMessage;
        }

        Minecraft.getMinecraft().thePlayer.addChatMessage(
                new ChatComponentText(ChatColor.translateAlternateColorCodes('&', formattedMessage)));
    }

    public void sendChat(String message) {
        if (MythicalClientMod.config.chat || message.contains("Chat Messages") || message.contains("for help.")) {
            Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(message));
        }
    }

    public void sendToggle(String name, String good, String bad, boolean isGood) {
        if (Minecraft.getMinecraft().thePlayer == null) {
            return;
        }

        // Update window title
        String titleMessage = name + " " + (isGood ? good : bad);
        sendNotification(titleMessage);
    }

    /**
     * Updates the game window title with a status message (thread-safe)
     * 
     * @param message The message to display in the window title
     */
    public void updateWindowTitle(String message) {
        // Schedule the title update on the main Minecraft thread to avoid crashes
        Minecraft.getMinecraft().addScheduledTask(() -> {
            try {
                String baseTitle = "Minecraft 1.8.9";
                String newTitle = baseTitle + " - " + message;
                Display.setTitle(newTitle);

                // Reset title after 3 seconds on the main thread
                new java.util.Timer().schedule(new java.util.TimerTask() {
                    @Override
                    public void run() {
                        Minecraft.getMinecraft().addScheduledTask(() -> {
                            try {
                                Display.setTitle(baseTitle);
                            } catch (Exception e) {
                                LOGGER.warn("Failed to reset window title: " + e.getMessage());
                            }
                        });
                    }
                }, 3000);
            } catch (Exception e) {
                LOGGER.warn("Failed to update window title: " + e.getMessage());
            }
        });
    }

    /**
     * Updates window title for feature toggles (thread-safe)
     * 
     * @param featureName The name of the feature (e.g., "SafeWalk", "Aimbot")
     * @param enabled     Whether the feature is enabled or disabled
     */
    public static void updateWindowTitleForToggle(String featureName, boolean enabled) {
        try {
            // Only update if we're in-game to prevent crashes
            if (Minecraft.getMinecraft().thePlayer == null || Minecraft.getMinecraft().theWorld == null) {
                return;
            }

            String status = enabled ? "Enabled" : "Disabled";
            String message = featureName + " " + status;
            instance.updateWindowTitle(message);
        } catch (Exception e) {
            LOGGER.warn("Failed to update window title for toggle: " + e.getMessage());
        }
    }

    /**
     * Send a notification based on the configured notification type
     * 
     * @param message     The message to send
     * @param featureName Optional feature name for title bar notifications
     * @param enabled     Optional enabled state for title bar notifications
     */
    public static void sendNotification(String message, String featureName, Boolean enabled) {
        try {
            if (Minecraft.getMinecraft().thePlayer == null || Minecraft.getMinecraft().theWorld == null) {
                return;
            }

            NotificationType notifyType = NotificationType.fromId(config.notifyMode);

            switch (notifyType) {
                case CHAT:
                    sendMessageToChat(message, false);
                    break;
                case ACTION_BAR:
                    Minecraft.getMinecraft().ingameGUI.setRecordPlaying(
                            ChatColor.translateAlternateColorCodes('&', message), false);
                    break;
                case TITLE_BAR:
                    if (featureName != null && enabled != null) {
                        updateWindowTitleForToggle(featureName, enabled);
                    } else {
                        instance.updateWindowTitle(
                                ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', message)));
                    }
                    break;
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to send notification: " + e.getMessage());
        }
    }

    /**
     * Send a simple notification with just a message
     * 
     * @param message The message to send
     */
    public static void sendNotification(String message) {
        sendNotification(message, null, null);
    }

    /**
     * Send a toggle notification for a feature
     * 
     * @param featureName The name of the feature
     * @param enabled     Whether the feature is enabled
     */
    public static void sendToggleNotification(String featureName, boolean enabled) {
        String status = enabled ? "&aenabled" : "&cdisabled";
        String message = "&7" + featureName + " is now " + status + "&7.";
        sendNotification(message, featureName, enabled);
    }

    public boolean isToggled() {
        return this.toggled;
    }

    public void toggle(boolean toggled) {
        this.toggled = toggled;
    }

    public void toggle() {
        this.toggle(!this.isToggled());
    }

    static {
        config = Config.instance;
        data = new MemoryStorageDriveData();
    }

    /**
     * Check if a port is in use
     * 
     * @param port The port to check
     * 
     * @return boolean
     */
    public static boolean isPortInUse(int port) {
        try (java.net.ServerSocket socket = new java.net.ServerSocket(port)) {
            // Port is available
            return false;
        } catch (java.io.IOException e) {
            // Port is in use
            return true;
        }
    }

    @SubscribeEvent
    public void onClientTick(ClientTickEvent event) {
        // Only process on the client phase to reduce redundant processing
        if (event.phase != net.minecraftforge.fml.common.gameevent.TickEvent.Phase.END) {
            return;
        }

        // Update aimbot tracking
        Aimbot.update();
    }

    @SubscribeEvent
    public void onKeyInput(KeyInputEvent event) {
        try {
            Minecraft mc = Minecraft.getMinecraft();
            if (mc.thePlayer == null || mc.theWorld == null)
                return;

            if (KeyBindPlayerInfo.isPressed()) {
                // Find closest player
                net.minecraft.entity.player.EntityPlayer closestPlayer = null;
                double closestDistance = Double.MAX_VALUE;

                for (Object obj : mc.theWorld.playerEntities) {
                    if (obj instanceof net.minecraft.entity.player.EntityPlayer) {
                        net.minecraft.entity.player.EntityPlayer player = (net.minecraft.entity.player.EntityPlayer) obj;
                        if (player != mc.thePlayer) {
                            double distance = mc.thePlayer.getDistanceToEntity(player);
                            if (distance < closestDistance) {
                                closestDistance = distance;
                                closestPlayer = player;
                            }
                        }
                    }
                }

                if (closestPlayer != null) {
                    net.minecraftforge.client.ClientCommandHandler.instance.executeCommand(mc.thePlayer,
                            "plinfo " + closestPlayer.getName());
                } else {
                    sendMessageToChat("&c&l[!] &cNo players found nearby!", false);
                }
            }

            if (KeyBindGroundItems.isPressed()) {
                net.minecraftforge.client.ClientCommandHandler.instance.executeCommand(mc.thePlayer, "grounditems");
            }

            if (KeyBindBedDefence.isPressed()) {
                net.minecraftforge.client.ClientCommandHandler.instance.executeCommand(mc.thePlayer, "beddefence");
            }
            
            if (injectDashboard) {
                if (KeyBindAimbot.isPressed()) {
                    Aimbot.toggle();
                }

                if (KeyBindToggleGui.isPressed()) {
                    mc.displayGuiScreen(new ToggleGui(mc.currentScreen));
                }

                if (KeyBindConsole.isPressed()) {
                    mc.displayGuiScreen(new xyz.nayskutzu.mythicalclient.gui.ConsoleOverlayGui());
                }
            }

        } catch (Exception e) {
            LOGGER.error("Error in key input handler: " + e.getMessage(), e);
        }
    }
}
