package xyz.nayskutzu.mythicalclient.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.mojang.realmsclient.gui.ChatFormatting;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;
import xyz.nayskutzu.mythicalclient.MythicalClientMod;
import xyz.nayskutzu.mythicalclient.utils.Config;

public class SafeWalkCommand implements ICommand {
    public static SafeWalkCommand instance = new SafeWalkCommand();
    private MythicalClientMod mod = MythicalClientMod.instance;
    private Config config = Config.instance;

    @Override
    public String getCommandName() {
        return "safewalk";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return null;
    }

    @Override
    public List<String> getCommandAliases() {
        return Arrays.asList("sb", "safewalk", "speedbridge", "sw");
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        if (args.length == 0) {
            this.mod.sendHelp();
        } else if (args.length == 1) {
            if (args[0].equals("help")) {
                this.mod.sendHelp();
            } else {
                if (args[0].equals("mode")) {
                    this.config.changeMode();
                    if (this.config.getMode(this.config.mode).equals("Scaffold")) {
                        this.mod.sendChat("[" + ChatFormatting.LIGHT_PURPLE + "Mode" + ChatFormatting.RESET + "] "
                                + ChatFormatting.YELLOW + "set to " + ChatFormatting.RED
                                + this.config.getMode(this.config.mode));
                    } else {
                        this.mod.sendChat("[" + ChatFormatting.LIGHT_PURPLE + "Mode" + ChatFormatting.RESET + "] "
                                + ChatFormatting.YELLOW + "set to " + ChatFormatting.GREEN
                                + this.config.getMode(this.config.mode));
                    }
                } else if (args[0].equals("chat")) {
                    this.config.chat = !this.config.chat;
                    this.mod.sendToggle("Chat Messages", "on", "off", this.config.chat);
                } else if (args[0].equals("click")) {
                    this.config.click = !this.config.click;
                    this.mod.sendToggle("AutoClick", "on", "off", this.config.click);
                } else if (args[0].equals("fall")) {
                    this.config.fall = !this.config.fall;
                    this.mod.sendToggle("Auto-disable on fall", "on", "off", this.config.fall);
                } else if (args[0].equals("jump")) {
                    this.config.jump = !this.config.jump;
                    this.mod.sendToggle("Auto-disable on jump", "on", "off", this.config.jump);
                } else {
                    this.mod.sendChat(ChatFormatting.RED + "Use " + ChatFormatting.GOLD + "/safewalk help"
                            + ChatFormatting.RED + " for help.");
                }
                this.config.saveConfig();
            }
        } else if (args.length == 2) {
            if (args[0].equals("chat")) {
                this.config.chat = !args[1].equals("true") && !args[1].equals("on")
                        ? (!args[1].equals("false") && !args[1].equals("off") ? this.config.chat : false)
                        : true;
                this.mod.sendToggle("Chat Messages", "on", "off", this.config.chat);
            } else if (args[0].equals("click")) {
                this.config.click = !args[1].equals("true") && !args[1].equals("on")
                        ? (!args[1].equals("false") && !args[1].equals("off") ? this.config.click : false)
                        : true;
                this.mod.sendToggle("AutoClick", "on", "off", this.config.click);
            } else if (args[0].equals("fall")) {
                this.config.fall = !args[1].equals("true") && !args[1].equals("on")
                        ? (!args[1].equals("false") && !args[1].equals("off") ? this.config.fall : false)
                        : true;
                this.mod.sendToggle("Auto-disable on fall", "on", "off", this.config.fall);
            } else if (!args[0].equals("jump")) {
                if (args[0].equals("mode") && Arrays.asList(this.config.getModes()).contains(args[1])) {
                    this.config.changeMode(args[1]);
                    if (args[1].equals("Scaffold")) {
                        this.mod.sendChat("[" + ChatFormatting.LIGHT_PURPLE + "Mode" + ChatFormatting.RESET + "] "
                                + ChatFormatting.YELLOW + "set to " + ChatFormatting.RED + args[1]);
                    } else {
                        this.mod.sendChat("[" + ChatFormatting.LIGHT_PURPLE + "Mode" + ChatFormatting.RESET + "] "
                                + ChatFormatting.YELLOW + "set to " + ChatFormatting.GREEN + args[1]);
                    }
                }
            } else {
                this.config.jump = !args[1].equals("true") && !args[1].equals("on")
                        ? (!args[1].equals("false") && !args[1].equals("off") ? this.config.jump : false)
                        : true;
                this.mod.sendToggle("Auto-disable on jump", "on", "off", this.config.jump);
            }
            this.config.saveConfig();
        } else {
            this.mod.sendChat(ChatFormatting.RED + "Use " + ChatFormatting.GOLD + "/safewalk help" + ChatFormatting.RED
                    + " for help.");
        }
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return true;
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        System.out.println(args.length + " | " + Arrays.toString(args));
        String[] argOne = new String[] { "help", "mode", "chat", "click", "fall", "jump" };
        String[] argTwo = new String[] { "true", "false", "on", "off" };
        if (args.length == 1) {
            return CommandBase.getListOfStringsMatchingLastWord(args, argOne);
        }
        if (args.length > 1) {
            if (args[0].equals("mode")) {
                return CommandBase.getListOfStringsMatchingLastWord(args, this.config.getModes());
            }
            return args[0].equals("help") ? new ArrayList<>()
                    : CommandBase.getListOfStringsMatchingLastWord(args, argTwo);
        }
        return new ArrayList<>();
    }

    public boolean isUsernameIndex(String[] args, int index) {
        return false;
    }

    public int compareTo(ICommand o) {
        return 0;
    }

}
