package xyz.nayskutzu.mythicalclient.commands;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import xyz.nayskutzu.mythicalclient.systems.StatsManager;

public class ShowStatsCommand extends CommandBase {
    @Override
    public String getCommandName() {
        return "showstats";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/showstats";
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return true;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        StatsManager.getInstance().toggle();
    }
} 