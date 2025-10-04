package xyz.nayskutzu.mythicalclient.systems;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import java.util.*;

public class PunishmentSystem {
    private static final Map<String, List<Punishment>> punishmentHistory = new HashMap<>();
    private static final List<String> watchlist = new ArrayList<>();
    
    public static class Punishment {
        public final String player;
        public final String type;
        public final String reason;
        public final long duration;
        public final long timestamp;
        public final String staffMember;
        
        public Punishment(String player, String type, String reason, long duration) {
            this.player = player;
            this.type = type;
            this.reason = reason;
            this.duration = duration;
            this.timestamp = System.currentTimeMillis();
            this.staffMember = Minecraft.getMinecraft().thePlayer.getName();
        }
    }
    
    public static void banPlayer(String player, String reason, long duration) {
        Punishment punishment = new Punishment(player, "BAN", reason, duration);
        addPunishment(punishment);
        
        // Send to server (you'd implement this based on your server's protocol)
        sendPunishmentToServer(punishment);
        
        // Notify staff
        notifyStaff(String.format("§c%s §7has been banned by §c%s §7for §c%s", 
            player, punishment.staffMember, reason));
    }
    
    public static void mutePlayer(String player, String reason, long duration) {
        Punishment punishment = new Punishment(player, "MUTE", reason, duration);
        addPunishment(punishment);
        
        sendPunishmentToServer(punishment);
        
        notifyStaff(String.format("§e%s §7has been muted by §e%s §7for §e%s", 
            player, punishment.staffMember, reason));
    }
    
    public static void kickPlayer(String player, String reason) {
        Punishment punishment = new Punishment(player, "KICK", reason, 0);
        addPunishment(punishment);
        
        sendPunishmentToServer(punishment);
        
        notifyStaff(String.format("§6%s §7has been kicked by §6%s §7for §6%s", 
            player, punishment.staffMember, reason));
    }
    
    public static void warnPlayer(String player, String reason) {
        Punishment punishment = new Punishment(player, "WARN", reason, 0);
        addPunishment(punishment);
        
        sendPunishmentToServer(punishment);
        
        notifyStaff(String.format("§a%s §7has been warned by §a%s §7for §a%s", 
            player, punishment.staffMember, reason));
    }
    
    private static void addPunishment(Punishment punishment) {
        punishmentHistory.computeIfAbsent(punishment.player, k -> new ArrayList<>())
            .add(punishment);
    }
    
    public static List<Punishment> getPlayerPunishments(String player) {
        return punishmentHistory.getOrDefault(player, new ArrayList<>());
    }
    
    public static void addToWatchlist(String player, String reason) {
        if (!watchlist.contains(player)) {
            watchlist.add(player);
            notifyStaff(String.format("§e%s §7has been added to the watchlist: §f%s", 
                player, reason));
        }
    }
    
    public static void removeFromWatchlist(String player) {
        if (watchlist.remove(player)) {
            notifyStaff(String.format("§e%s §7has been removed from the watchlist", player));
        }
    }
    
    private static void sendPunishmentToServer(Punishment punishment) {
        // TODO: Implement server communication
        // This would use your server's network protocol
    }
    
    private static void notifyStaff(String message) {
        Minecraft.getMinecraft().thePlayer.addChatMessage(
            new ChatComponentText("§8[§c§lGamster§8] " + message)
        );
    }
} 