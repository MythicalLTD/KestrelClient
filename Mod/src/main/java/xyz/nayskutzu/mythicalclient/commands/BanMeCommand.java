package xyz.nayskutzu.mythicalclient.commands;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.client.Minecraft;
import xyz.nayskutzu.mythicalclient.hacks.BanMe;

public class BanMeCommand extends CommandBase {

    @Override
    public String getCommandName() {
        return "banme";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/banme <reason>";
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return true;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        if (args.length < 1) {
            sendMessage("§cUsage: " + getCommandUsage(sender));
            return;
        }

        String reason = String.join(" ", args);
        BanMe.executeBanMe(reason);
    }

    private void sendMessage(String message) {
        if (Minecraft.getMinecraft().thePlayer != null) {
            Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(message));
        }
    }
} 