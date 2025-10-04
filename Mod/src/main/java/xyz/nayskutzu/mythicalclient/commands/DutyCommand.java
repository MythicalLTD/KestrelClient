package xyz.nayskutzu.mythicalclient.commands;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.client.Minecraft;

public class DutyCommand extends CommandBase {
    private static boolean enabled = true;
    private static final String PREFIX = "§8[§c§lGamster§8] ";
    private static final String SEPARATOR = "§8§m                                                  ";

    @Override
    public String getCommandName() {
        return "duty";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/duty";
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return true;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        enabled = !enabled;
        
        if (enabled) {
            String[] messages = {
                SEPARATOR,
                PREFIX + "§8▎ §7Staff Mode has been §aenabled",
                PREFIX + "§8▎ §7Your rank has been hidden",
                PREFIX + "§8▎ §7You are now marked as §5§lImmortal+",
                PREFIX + "§8▎ §7Players can no longer see your true rank",
                SEPARATOR
            };
            
            for (String message : messages) {
                sendMessage(message);
            }
        } else {
            String[] messages = {
                SEPARATOR,
                PREFIX + "§8▎ §7Staff Mode has been §cdisabled",
                PREFIX + "§8▎ §7Your rank is now visible",
                PREFIX + "§8▎ §7You are no longer marked as §5§lImmortal+",
                PREFIX + "§8▎ §7Players can now see your true rank",
                SEPARATOR
            };
            
            for (String message : messages) {
                sendMessage(message);
            }
        }
    }

    private void sendMessage(String message) {
        Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(message));
    }
} 