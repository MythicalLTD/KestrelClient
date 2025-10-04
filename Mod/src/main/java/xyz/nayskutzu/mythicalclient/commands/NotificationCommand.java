package xyz.nayskutzu.mythicalclient.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.mojang.realmsclient.gui.ChatFormatting;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;
import xyz.nayskutzu.mythicalclient.MythicalClientMod;
import xyz.nayskutzu.mythicalclient.utils.Config;
import xyz.nayskutzu.mythicalclient.utils.NotificationType;

public class NotificationCommand implements ICommand {
    public static NotificationCommand instance = new NotificationCommand();
    private MythicalClientMod mod = MythicalClientMod.instance;
    private Config config = Config.instance;

    @Override
    public String getCommandName() {
        return "notify";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/notify [mode|test] - Manage notification settings";
    }

    @Override
    public List<String> getCommandAliases() {
        return Arrays.asList("notification", "notifications", "notif");
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        if (args.length == 0) {
            sendHelp();
        } else if (args.length == 1) {
            if (args[0].equals("help")) {
                sendHelp();
            } else if (args[0].equals("mode")) {
                config.changeNotifyMode();
                String currentMode = config.getNotifyMode(config.notifyMode);
                mod.sendChat("[" + ChatFormatting.LIGHT_PURPLE + "Notifications" + ChatFormatting.RESET + "] " 
                    + ChatFormatting.YELLOW + "Mode set to " + ChatFormatting.GREEN + currentMode);
                config.saveConfig();
            } else if (args[0].equals("test")) {
                MythicalClientMod.sendToggleNotification("Test Feature", true);
            }
        } else if (args.length == 2) {
            if (args[0].equals("mode")) {
                String requestedMode = args[1];
                NotificationType type = NotificationType.fromDisplayName(requestedMode);
                
                if (type != null) {
                    config.notifyMode = type.getId();
                    mod.sendChat("[" + ChatFormatting.LIGHT_PURPLE + "Notifications" + ChatFormatting.RESET + "] " 
                        + ChatFormatting.YELLOW + "Mode set to " + ChatFormatting.GREEN + type.getDisplayName());
                    config.saveConfig();
                } else {
                    mod.sendChat("[" + ChatFormatting.LIGHT_PURPLE + "Notifications" + ChatFormatting.RESET + "] " 
                        + ChatFormatting.RED + "Invalid mode! Available modes: Chat, ActionBar, TitleBar");
                }
            }
        }
    }

    private void sendHelp() {
        mod.sendChat(ChatFormatting.LIGHT_PURPLE + "MythicalClient Notifications Help");
        mod.sendChat(ChatFormatting.GRAY + "Commands:");
        mod.sendChat(ChatFormatting.GRAY + "/notify mode - Cycle through notification modes");
        mod.sendChat(ChatFormatting.GRAY + "/notify mode <Chat|ActionBar|TitleBar> - Set specific mode");
        mod.sendChat(ChatFormatting.GRAY + "/notify test - Test current notification mode");
        mod.sendChat(ChatFormatting.GRAY + "Current mode: " + ChatFormatting.GREEN + config.getNotifyMode(config.notifyMode));
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return true;
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        if (args.length == 1) {
            return Arrays.asList("mode", "test", "help");
        } else if (args.length == 2 && args[0].equals("mode")) {
            return Arrays.asList("Chat", "ActionBar", "TitleBar");
        }
        return new ArrayList<>();
    }

    @Override
    public boolean isUsernameIndex(String[] args, int index) {
        return false;
    }

    @Override
    public int compareTo(ICommand other) {
        return this.getCommandName().compareTo(other.getCommandName());
    }
} 