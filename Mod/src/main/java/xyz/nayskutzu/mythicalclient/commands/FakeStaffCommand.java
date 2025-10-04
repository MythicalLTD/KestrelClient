package xyz.nayskutzu.mythicalclient.commands;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.client.Minecraft;
import java.util.Random;
import java.util.Arrays;

public class FakeStaffCommand extends CommandBase {
    private static final Random random = new Random();
    private static final String PREFIX = "§8[§c§lGamster§8] ";
    private static final String STAFF_PREFIX = "§c§lDEV §7NaysKutzu";
    private static final String SEPARATOR = "§8§m                                                  ";
    
    private static final String[] BAN_REASONS = {
        "Use of Unauthorized Modifications",
        "Inappropriate Behavior",
        "Chat Violation",
        "Exploiting Game Mechanics",
        "Harassment of Players",
        "Advertising External Content",
        "Ban Evasion Attempt",
        "Malicious Activity"
    };

    @Override
    public String getCommandName() {
        return "gpunish";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/gpunish <ban/mute/kick/warn> <player> [reason]";
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return true;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        if (args.length < 2) {
            sendMessage("§cUsage: " + getCommandUsage(sender));
            return;
        }

        String action = args[0].toLowerCase();
        String target = args[1];
        String reason = args.length > 2 ? String.join(" ", Arrays.copyOfRange(args, 2, args.length)) 
                                      : BAN_REASONS[random.nextInt(BAN_REASONS.length)];

        switch (action) {
            case "ban":
                sendBanMessage(target, reason);
                break;
            case "mute":
                sendMuteMessage(target, reason);
                break;
            case "kick":
                sendKickMessage(target, reason);
                break;
            case "warn":
                sendWarnMessage(target, reason);
                break;
            default:
                sendMessage("§cInvalid action! Use: ban, mute, kick, or warn");
        }
    }

    private void sendBanMessage(String target, String reason) {
        String[] messages = {
            SEPARATOR,
            PREFIX + "§8▎ §c" + target + " §7has been §4§lPERMANENTLY §7banned",
            PREFIX + "§8▎ §7Banned by §8» " + STAFF_PREFIX,
            PREFIX + "§8▎ §7Reason §8» §f" + reason,
            SEPARATOR
        };
        
        for (String message : messages) {
            sendMessage(message);
        }
    }

    private void sendMuteMessage(String target, String reason) {
        String[] messages = {
            SEPARATOR,
            PREFIX + "§8▎ §c" + target + " §7has been muted",
            PREFIX + "§8▎ §7Muted by §8» " + STAFF_PREFIX,
            PREFIX + "§8▎ §7Reason §8» §f" + reason,
            SEPARATOR
        };
        
        for (String message : messages) {
            sendMessage(message);
        }
    }

    private void sendKickMessage(String target, String reason) {
        String[] messages = {
            SEPARATOR,
            PREFIX + "§8▎ §c" + target + " §7has been kicked from the server",
            PREFIX + "§8▎ §7Kicked by §8» " + STAFF_PREFIX,
            PREFIX + "§8▎ §7Reason §8» §f" + reason,
            SEPARATOR
        };
        
        for (String message : messages) {
            sendMessage(message);
        }
    }

    private void sendWarnMessage(String target, String reason) {
        String[] messages = {
            SEPARATOR,
            PREFIX + "§8▎ §e" + target + " §7has received a warning",
            PREFIX + "§8▎ §7Warned by §8» " + STAFF_PREFIX,
            PREFIX + "§8▎ §7Reason §8» §f" + reason,
            PREFIX + "§8▎ §7Further violations may result in a punishment",
            SEPARATOR
        };
        
        for (String message : messages) {
            sendMessage(message);
        }
    }

    private void sendMessage(String message) {
        Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(message));
    }
} 