package xyz.nayskutzu.mythicalclient.utils;

import java.io.File;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

public class Config {
    public static Config instance = new Config();
    public boolean chat = true;
    public boolean click = true;
    public boolean fall = false;
    public boolean jump = false;
    public int mode = 1;
    public int notifyMode = 1; // 1 = Chat, 2 = Action Bar, 3 = Title Bar
    private Configuration config;
    private File configFile;
    private String[] modes = new String[] { "Sneak", "Breezily", "Scaffold" };
    private String[] notifyModes = new String[] { "Chat", "ActionBar", "TitleBar" };

    public void changeMode() {
        ++this.mode;
        if (this.mode > this.modes.length) {
            this.mode = 1;
        }
    }

    public void changeMode(String newmode) {
        for (int i = 0; i < this.modes.length; ++i) {
            if (!this.modes[i].equals(newmode))
                continue;
            this.mode = i + 1;
        }
    }

    public String getMode(int index) {
        return this.getModes()[index - 1];
    }

    public String[] getModes() {
        return this.modes;
    }

    public void changeNotifyMode() {
        ++this.notifyMode;
        if (this.notifyMode > this.notifyModes.length) {
            this.notifyMode = 1;
        }
    }

    public void changeNotifyMode(String newMode) {
        for (int i = 0; i < this.notifyModes.length; ++i) {
            if (!this.notifyModes[i].equals(newMode))
                continue;
            this.notifyMode = i + 1;
        }
    }

    public String getNotifyMode(int index) {
        return this.getNotifyModes()[index - 1];
    }

    public String[] getNotifyModes() {
        return this.notifyModes;
    }

    public void reloadConfig() {
        this.updateConfig(this.configFile, true);
    }

    public void saveConfig() {
        this.updateConfig(this.configFile, false);
    }

    public void updateConfig(File cfgFile, boolean isLoading) {
        if (isLoading) {
            this.config = new Configuration(cfgFile);
            this.config.load();
            this.configFile = cfgFile;
        }
        Property property = this.config.get("Safewalk", "mode", this.mode);
        property.comment = "{1-" + this.modes.length + "} 1 - Sneak, 2 - Breezily, 3 - Scaffold";
        if (isLoading) {
            this.mode = property.getInt(1);
        } else {
            property.set(this.mode);
        }
        property = this.config.get("Safewalk", "autoclick", this.click);
        property.comment = "{true/false} Automatically click when you are over the edge (Sneak Mode) and aiming at the side of the block below you.";
        if (isLoading) {
            this.click = property.getBoolean(true);
        } else {
            property.set(this.click);
        }
        property = this.config.get("Safewalk", "chatmessages", this.chat);
        property.comment = "{true/false} Show messages from the mod in the chat.";
        if (isLoading) {
            this.chat = property.getBoolean(true);
        } else {
            property.set(this.chat);
        }
        property = this.config.get("Safewalk", "disableonfall", this.fall);
        property.comment = "{true/false} Automatically disable the mod when you fall off a block or a block below you is broken.";
        if (isLoading) {
            this.fall = property.getBoolean(true);
        } else {
            property.set(this.fall);
        }
        property = this.config.get("Safewalk", "disableonjump", this.jump);
        property.comment = "{true/false} Automatically disable the mod when you jump.";
        if (isLoading) {
            this.jump = property.getBoolean(true);
        } else {
            property.set(this.jump);
        }
        property = this.config.get("Notifications", "notifymode", this.notifyMode);
        property.comment = "{1-" + this.notifyModes.length + "} 1 - Chat, 2 - Action Bar, 3 - Title Bar";
        if (isLoading) {
            this.notifyMode = property.getInt(1);
        } else {
            property.set(this.notifyMode);
        }
        this.config.save();
    }
}
