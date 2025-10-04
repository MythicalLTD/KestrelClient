package xyz.nayskutzu.mythicalclient.data;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import java.util.*;

public class MockDataManager {
    private static final Random random = new Random();
    
    private static final List<String> HACK_TYPES = Arrays.asList(
        "KillAura", "Speed", "Fly", "AutoClicker", "Reach", "NoFall",
        "Scaffold", "Timer", "FastPlace", "Velocity", "AntiKB", "Jesus"
    );
    
    private static final List<String> REPORT_REASONS = Arrays.asList(
        "Chat Abuse", "Hacking", "Team Griefing", "Bug Abuse", "Inappropriate Skin",
        "Cross Teaming", "Combat Logging", "Ban Evasion"
    );
    
    public static class PlayerData {
        public String name;
        public int violations;
        public String lastHack;
        public long lastSeen;
        public boolean isOnline;
        public String gameMode;
        public int reports;
        public int warnings;
        public int bans;
        public double distance;
        
        public PlayerData(EntityPlayer player) {
            this.name = player.getName();
            this.violations = random.nextInt(10);
            this.lastHack = HACK_TYPES.get(random.nextInt(HACK_TYPES.size()));
            this.lastSeen = System.currentTimeMillis();
            this.isOnline = true;
            this.gameMode = getRandomGameMode(); // TODO: Get actual gamemode
            this.reports = random.nextInt(5);
            this.warnings = random.nextInt(3);
            this.bans = random.nextInt(2);
            
            EntityPlayer localPlayer = Minecraft.getMinecraft().thePlayer;
            if (localPlayer != null) {
                this.distance = player.getDistanceToEntity(localPlayer);
            }
        }
        
        private String getRandomGameMode() {
            String[] modes = {"Practice", "SkyWars", "BedWars", "Lobby", "UHC", "Creative"};
            return modes[random.nextInt(modes.length)];
        }
    }
    
    public static List<PlayerData> getNearbyPlayers() {
        List<PlayerData> players = new ArrayList<>();
        Minecraft mc = Minecraft.getMinecraft();
        
        if (mc.theWorld != null && mc.thePlayer != null) {
            // Get all players in the world
            for (EntityPlayer player : mc.theWorld.playerEntities) {
                if (player != mc.thePlayer) { // Don't include the local player
                    players.add(new PlayerData(player));
                }
            }
            
            // Sort by distance
            players.sort((p1, p2) -> Double.compare(p1.distance, p2.distance));
        }
        
        return players;
    }
    
    public static List<ACAlert> getRecentAlerts() {
        List<ACAlert> alerts = new ArrayList<>();
        Minecraft mc = Minecraft.getMinecraft();
        
        if (mc.theWorld != null) {
            // Create alerts for random online players
            List<EntityPlayer> onlinePlayers = mc.theWorld.playerEntities;
            int alertCount = Math.min(5 + random.nextInt(6), onlinePlayers.size());
            
            for (int i = 0; i < alertCount; i++) {
                ACAlert alert = new ACAlert();
                EntityPlayer randomPlayer = onlinePlayers.get(random.nextInt(onlinePlayers.size()));
                alert.player = randomPlayer.getName();
                alerts.add(alert);
            }
        }
        
        alerts.sort((a1, a2) -> Long.compare(a2.timestamp, a1.timestamp));
        return alerts;
    }
    
    public static class ACAlert {
        public String player;
        public String hack;
        public int violations;
        public long timestamp;
        public double confidence;
        
        public ACAlert() {
            this.hack = HACK_TYPES.get(random.nextInt(HACK_TYPES.size()));
            this.violations = random.nextInt(10) + 1;
            this.timestamp = System.currentTimeMillis() - random.nextInt(300000);
            this.confidence = 0.5 + (random.nextDouble() * 0.5);
        }
    }
    
    public static List<Report> getRecentReports() {
        List<Report> reports = new ArrayList<>();
        Minecraft mc = Minecraft.getMinecraft();
        
        if (mc.theWorld != null) {
            List<EntityPlayer> onlinePlayers = mc.theWorld.playerEntities;
            if (!onlinePlayers.isEmpty()) {
                int reportCount = Math.min(4 + random.nextInt(4), onlinePlayers.size());
                
                for (int i = 0; i < reportCount; i++) {
                    Report report = new Report();
                    // Use actual online players for reports
                    EntityPlayer reported = onlinePlayers.get(random.nextInt(onlinePlayers.size()));
                    EntityPlayer reporter = onlinePlayers.get(random.nextInt(onlinePlayers.size()));
                    report.reported = reported.getName();
                    report.reporter = reporter.getName();
                    reports.add(report);
                }
            }
        }
        
        reports.sort((r1, r2) -> Long.compare(r2.timestamp, r1.timestamp));
        return reports;
    }
    
    public static class Report {
        public String reporter;
        public String reported;
        public String reason;
        public long timestamp;
        public boolean handled;
        
        public Report() {
            this.reason = REPORT_REASONS.get(random.nextInt(REPORT_REASONS.size()));
            this.timestamp = System.currentTimeMillis() - random.nextInt(3600000);
            this.handled = random.nextBoolean();
        }
    }
    
    public static Map<String, Integer> getServerStats() {
        Map<String, Integer> stats = new HashMap<>();
        Minecraft mc = Minecraft.getMinecraft();
        
        if (mc.theWorld != null) {
            stats.put("Online Players", mc.theWorld.playerEntities.size());
            stats.put("Online Staff", 1 + random.nextInt(3)); // Keep some randomness for staff
            stats.put("Active Reports", 2 + random.nextInt(5));
            stats.put("AC Alerts", Math.max(1, mc.theWorld.playerEntities.size() / 4));
        } else {
            stats.put("Online Players", 0);
            stats.put("Online Staff", 0);
            stats.put("Active Reports", 0);
            stats.put("AC Alerts", 0);
        }
        
        return stats;
    }
} 