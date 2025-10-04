package xyz.nayskutzu.mythicalclient.commands;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraft.util.EnumChatFormatting;
import org.lwjgl.input.Keyboard;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.Random;

public class AntiCheatCommand extends CommandBase {
    private static final Map<UUID, PlayerInfo> playerData = new HashMap<>();
    private static boolean enabled = false;
    private static final double DETECTION_RANGE = 6.0;
    private static final Random random = new Random();
    private static long lastAlert = 0;

    private static final String PREFIX = "§8[§c§lIntave§8] ";
    
    private static final String[] HACK_MESSAGES = {
        PREFIX + "§7Player §c%s §7failed §cKillAura §7check §8(§c%d§8)",
        PREFIX + "§7Player §c%s §7failed §cAutoClicker §7check §8(§c%d§8)",
        PREFIX + "§7Player §c%s §7failed §cReach §7check §8(§c%d§8)",
        PREFIX + "§7Player §c%s §7failed §cCombat §7check §8(§c%d§8)",
        PREFIX + "§7Player §c%s §7failed §cPattern §7check §8(§c%d§8)",
        PREFIX + "§7Player §c%s §7failed §cRotation §7check §8(§c%d§8)",
        PREFIX + "§7Player §c%s §7failed §cVelocity §7check §8(§c%d§8)",
        PREFIX + "§7Player §c%s §7failed §cImpossible §7check §8(§c%d§8)",
        PREFIX + "§7Player §c%s §7failed §cCriticals §7check §8(§c%d§8)",
        PREFIX + "§7Player §c%s §7failed §cPacket §7check §8(§c%d§8)",
        PREFIX + "§7Player §c%s §7failed §cAimbot §7check §8(§c%d§8)",
        PREFIX + "§7Player §c%s §7failed §cMovement §7check §8(§c%d§8)",
        PREFIX + "§7Player §c%s §7failed §cTimer §7check §8(§c%d§8)",
        PREFIX + "§7Player §c%s §7failed §cHitbox §7check §8(§c%d§8)",
        PREFIX + "§7Player §c%s §7failed §cAngle §7check §8(§c%d§8)",
        PREFIX + "§7Player §c%s §7failed §cBehavior §7check §8(§c%d§8)",
        PREFIX + "§7Player §c%s §7failed §cRange §7check §8(§c%d§8)",
        PREFIX + "§7Player §c%s §7failed §cAnalysis §7check §8(§c%d§8)",
        PREFIX + "§7Player §c%s §7failed §cMulti-Hit §7check §8(§c%d§8)",
        PREFIX + "§7Player §c%s §7failed §cTiming §7check §8(§c%d§8)"
    };

    private static class PlayerInfo {
        float lastSwingProgress = 0;
        int consecutiveHits = 0;
        long lastHitTime = 0;
        boolean wasReported = false;

        void registerHit() {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastHitTime < 200) {
                consecutiveHits++;
            } else {
                consecutiveHits = 1;
            }
            lastHitTime = currentTime;
        }
    }

    public AntiCheatCommand() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public String getCommandName() {
        return "ac";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/ac";
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return true;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        enabled = !enabled;
        sendToggleMessage(enabled);
    }

    @SubscribeEvent
    public void onKeyInput(KeyInputEvent event) {
        if (enabled && Keyboard.isKeyDown(Keyboard.KEY_U) && 
            (Keyboard.isKeyDown(Keyboard.KEY_RSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_LSHIFT))) {
            
            Minecraft mc = Minecraft.getMinecraft();
            if (mc.thePlayer == null || mc.theWorld == null) return;

            EntityPlayer nearestPlayer = null;
            double nearestDistance = Double.MAX_VALUE;

            for (EntityPlayer player : mc.theWorld.playerEntities) {
                if (player != mc.thePlayer) {
                    double distance = mc.thePlayer.getDistanceToEntity(player);
                    if (distance < DETECTION_RANGE && distance < nearestDistance) {
                        nearestDistance = distance;
                        nearestPlayer = player;
                    }
                }
            }

            if (nearestPlayer != null) {
                PlayerInfo info = playerData.computeIfAbsent(nearestPlayer.getUniqueID(), k -> new PlayerInfo());
                sendAlert(nearestPlayer.getName(), info);
            }
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (!enabled || event.phase != TickEvent.Phase.END) return;
        checkPlayers();
    }

    private void checkPlayers() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null || mc.theWorld == null) return;

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastAlert < 5000) return;

        for (EntityPlayer player : mc.theWorld.playerEntities) {
            if (player != mc.thePlayer && mc.thePlayer.getDistanceToEntity(player) < DETECTION_RANGE) {
                PlayerInfo info = playerData.computeIfAbsent(player.getUniqueID(), k -> new PlayerInfo());
                
                if (player.isSwingInProgress && player.swingProgress < info.lastSwingProgress) {
                    info.registerHit();
                    
                    if (info.consecutiveHits > 3 && !info.wasReported && random.nextInt(100) < 15) {
                        sendAlert(player.getName(), info);
                        info.wasReported = true;
                        lastAlert = currentTime;
                        break;
                    }
                }
                info.lastSwingProgress = player.swingProgress;
            }
        }
    }

    private void sendAlert(String playerName, PlayerInfo info) {
        String message = HACK_MESSAGES[random.nextInt(HACK_MESSAGES.length)];
        int violationLevel = 1 + random.nextInt(5);
        
        // Add a random chance for experimental/dev messages
        if (random.nextInt(100) < 10) { // 10% chance
            message = PREFIX + "§7Player §c%s §7failed §c§lEXPERIMENTAL §7check §8(§c%d§8)";
        } else if (random.nextInt(100) < 5) { // 5% chance
            message = PREFIX + "§7Player §c%s §7failed §c§lDEV §7check §8(§c%d§8)";
        }
        
        Minecraft.getMinecraft().thePlayer.addChatMessage(
            new ChatComponentText(String.format(message, playerName, violationLevel))
        );
    }

    private void sendToggleMessage(boolean enabled) {
        String status = enabled ? "§aenabled" : "§cdisabled";
        Minecraft.getMinecraft().thePlayer.addChatMessage(
            new ChatComponentText(PREFIX + 
                                EnumChatFormatting.GRAY + "Staff notifications have been " + status));
    }
} 