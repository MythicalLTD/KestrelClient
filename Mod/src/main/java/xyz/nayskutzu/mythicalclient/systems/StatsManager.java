package xyz.nayskutzu.mythicalclient.systems;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class StatsManager {
    private static final StatsManager INSTANCE = new StatsManager();
    private static final String PREFIX = "§8[§c§lGamster§8] ";
    private static final Random random = new Random();
    
    private boolean enabled = true; // On by default
    private Timer updateTimer;
    private int reportCount = 0;
    private int flagCount = 0;
    private int watchlistCount = 0;
    
    private StatsManager() {
        startUpdating(); // Start on initialization
    }
    
    public static StatsManager getInstance() {
        return INSTANCE;
    }
    
    public void toggle() {
        enabled = !enabled;
        if (enabled) {
            startUpdating();
            sendMessage(PREFIX + "§8▎ §7Staff statistics are now visible in action bar");
        } else {
            stopUpdating();
            sendMessage(PREFIX + "§8▎ §7Staff statistics are now hidden");
        }
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    private void startUpdating() {
        if (updateTimer != null) {
            updateTimer.cancel();
        }
        
        updateTimer = new Timer();
        updateTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (!enabled) return;
                
                // Randomly update stats
                if (random.nextInt(100) < 30) { // 30% chance to update reports
                    reportCount = Math.min(reportCount + random.nextInt(3), 15);
                }
                if (random.nextInt(100) < 20) { // 20% chance to update flags
                    flagCount = Math.min(flagCount + random.nextInt(2), 8);
                }
                if (random.nextInt(100) < 10) { // 10% chance to update watchlist
                    watchlistCount = Math.min(watchlistCount + random.nextInt(2), 5);
                }
                
                updateActionBar();
            }
        }, 0, 2000); // Update every 2 seconds
    }
    
    private void stopUpdating() {
        if (updateTimer != null) {
            updateTimer.cancel();
            updateTimer = null;
        }
        reportCount = 0;
        flagCount = 0;
        watchlistCount = 0;
        
        // Clear the action bar
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer != null) {
            mc.ingameGUI.setRecordPlaying("", false);
        }
    }
    
    private void updateActionBar() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null) return;
        
        String stats = String.format("§8▎ §cReports: §7%d §8| §cAC Flags: §7%d §8| §cWatchlist: §7%d",
                                   reportCount, flagCount, watchlistCount);
        
        mc.ingameGUI.setRecordPlaying(stats, false);
    }
    
    private void sendMessage(String message) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer != null) {
            mc.thePlayer.addChatMessage(new ChatComponentText(message));
        }
    }
    
    public void onClientStart() {
        startUpdating();
    }
    
    public void onClientStop() {
        if (updateTimer != null) {
            updateTimer.cancel();
            updateTimer = null;
        }
    }
} 