package xyz.nayskutzu.mythicalclient.gui;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import xyz.nayskutzu.mythicalclient.gui.components.ColorButton;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SettingsGui extends GuiScreen {
    private final GuiScreen parentScreen;
    private List<Setting> settings = new ArrayList<>();
    private boolean changed = false;
    
    public SettingsGui(GuiScreen parentScreen) {
        this.parentScreen = parentScreen;
        initSettings();
    }
    
    private void initSettings() {
        settings.add(new Setting("Staff Mode", "Toggle staff mode visibility", true));
        settings.add(new Setting("Auto-Accept Reports", "Automatically accept incoming reports", false));
        settings.add(new Setting("Alert Sounds", "Play sounds for important alerts", true));
        settings.add(new Setting("Quick Commands", "Enable quick command shortcuts", true));
        settings.add(new Setting("Chat Filter", "Filter inappropriate messages", true));
        settings.add(new Setting("Auto Vanish", "Automatically vanish on join", false));
        settings.add(new Setting("Staff Chat", "Enable staff chat messages", true));
        settings.add(new Setting("Report Notifications", "Show report notifications", true));
    }
    
    @Override
    public void initGui() {
        int centerX = this.width / 2;
        
        // Category buttons at the top
        this.buttonList.add(new ColorButton(1, centerX - 190, 20, 120, 20, "§b§lGeneral", 0x55FFFF));
        this.buttonList.add(new ColorButton(2, centerX - 60, 20, 120, 20, "§e§lStaff", 0xFFAA00));
        this.buttonList.add(new ColorButton(3, centerX + 70, 20, 120, 20, "§d§lVisuals", 0xFF55FF));
        
        // Settings buttons
        int y = 60;
        int id = 10;
        for (Setting setting : settings) {
            this.buttonList.add(new ToggleButton(
                id++,
                centerX - 150,
                y,
                300,
                25,
                "§f" + setting.name,
                setting.description,
                setting.enabled
            ));
            y += 30;
        }
        
        // Action buttons at the bottom
        this.buttonList.add(new ColorButton(98, centerX - 105, this.height - 40, 100, 20, "§a§lSave", 0x55FF55));
        this.buttonList.add(new ColorButton(99, centerX + 5, this.height - 40, 100, 20, "§c§lCancel", 0xFF5555));
    }
    
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        
        // Draw title
        drawCenteredString(fontRendererObj, "§b§lSettings", width / 2, 5, 0xFFFFFF);
        
        // Draw settings background
        drawRect(width / 2 - 160, 50, width / 2 + 160, height - 50, new Color(0, 0, 0, 100).getRGB());
        
        // Draw hover descriptions
        for (GuiButton button : this.buttonList) {
            if (button instanceof ToggleButton) {
                ToggleButton tb = (ToggleButton) button;
                if (tb.isMouseOver(mouseX, mouseY)) {
                    List<String> lines = new ArrayList<>();
                    lines.add(tb.description);
                    drawHoveringText(lines, mouseX, mouseY);
                }
            }
        }
        
        super.drawScreen(mouseX, mouseY, partialTicks);
        
        // Draw warning if settings changed
        if (changed) {
            drawCenteredString(fontRendererObj, "§e§lUnsaved Changes", width / 2, height - 60, 0xFFFFFF);
        }
    }
    
    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id >= 10 && button.id < 50) {
            ToggleButton tb = (ToggleButton) button;
            tb.toggle();
            changed = true;
            return;
        }
        
        switch (button.id) {
            case 98: // Save
                saveSettings();
                mc.displayGuiScreen(parentScreen);
                break;
            case 99: // Cancel
                mc.displayGuiScreen(parentScreen);
                break;
        }
    }
    
    private void saveSettings() {
        // Save settings logic here
        changed = false;
    }
    
    private static class Setting {
        String name;
        String description;
        boolean enabled;
        
        Setting(String name, String description, boolean enabled) {
            this.name = name;
            this.description = description;
            this.enabled = enabled;
        }
    }
    
    private class ToggleButton extends GuiButton {
        private final String description;
        private boolean enabled;
        private float hoverTime = 0;
        
        public ToggleButton(int id, int x, int y, int width, int height, String text, String description, boolean enabled) {
            super(id, x, y, width, height, text);
            this.description = description;
            this.enabled = enabled;
        }
        
        public void toggle() {
            this.enabled = !this.enabled;
        }
        
        @Override
        public void drawButton(Minecraft mc, int mouseX, int mouseY) {
            if (this.visible) {
                boolean hovered = mouseX >= this.xPosition && mouseY >= this.yPosition && 
                                mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
                
                // Smooth hover animation
                if (hovered && hoverTime < 1) {
                    hoverTime += 0.1f;
                } else if (!hovered && hoverTime > 0) {
                    hoverTime -= 0.1f;
                }
                hoverTime = Math.max(0, Math.min(1, hoverTime));
                
                // Draw background
                drawRect(this.xPosition, this.yPosition,
                        this.xPosition + this.width, this.yPosition + this.height,
                        new Color(40, 40, 40, 200).getRGB());
                
                // Draw toggle indicator
                int toggleWidth = 40;
                int toggleHeight = 14;
                int toggleX = this.xPosition + this.width - toggleWidth - 5;
                int toggleY = this.yPosition + (this.height - toggleHeight) / 2;
                
                // Background track
                drawRect(toggleX, toggleY,
                        toggleX + toggleWidth, toggleY + toggleHeight,
                        new Color(20, 20, 20).getRGB());
                
                // Sliding button
                int buttonSize = toggleHeight - 2;
                int buttonX = toggleX + 1 + (enabled ? toggleWidth - buttonSize - 1 : 0);
                
                drawRect(buttonX, toggleY + 1,
                        buttonX + buttonSize, toggleY + buttonSize + 1,
                        enabled ? 0x55FF55 : 0xFF5555);
                
                // Draw text
                this.drawString(mc.fontRendererObj, this.displayString,
                    this.xPosition + 5,
                    this.yPosition + (this.height - 8) / 2,
                    enabled ? 0xFFFFFF : 0xAAAAAA);
            }
        }
        
        public boolean isMouseOver(int mouseX, int mouseY) {
            return mouseX >= this.xPosition && mouseY >= this.yPosition && 
                   mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
        }
    }
} 