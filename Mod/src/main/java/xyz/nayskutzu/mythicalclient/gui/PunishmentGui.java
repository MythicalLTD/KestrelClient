package xyz.nayskutzu.mythicalclient.gui;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import java.awt.Color;
import net.minecraft.client.Minecraft;
import java.io.IOException;

public class PunishmentGui extends GuiScreen {
    private final GuiScreen parentScreen;
    private GuiTextField playerField;
    private GuiTextField reasonField;
    private String selectedPunishment = "ban";
    private int duration = 30; // Default 30 days
    private boolean permanent = true;
    
    public PunishmentGui(GuiScreen parentScreen) {
        this.parentScreen = parentScreen;
    }
    
    @Override
    public void initGui() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        
        // Player input field
        playerField = new GuiTextField(0, fontRendererObj, centerX - 100, centerY - 60, 200, 20);
        playerField.setMaxStringLength(16);
        playerField.setFocused(true);
        
        // Reason input field
        reasonField = new GuiTextField(1, fontRendererObj, centerX - 100, centerY - 20, 200, 20);
        reasonField.setMaxStringLength(100);
        
        // Punishment type buttons
        this.buttonList.add(new ColorButton(1, centerX - 152, centerY + 20, 75, 20, "§c§lBan", 0xFF5555));
        this.buttonList.add(new ColorButton(2, centerX - 76, centerY + 20, 75, 20, "§e§lMute", 0xFFAA00));
        this.buttonList.add(new ColorButton(3, centerX, centerY + 20, 75, 20, "§6§lKick", 0xFF5500));
        this.buttonList.add(new ColorButton(4, centerX + 76, centerY + 20, 75, 20, "§a§lWarn", 0x55FF55));
        
        // Duration buttons
        this.buttonList.add(new ColorButton(5, centerX - 152, centerY + 45, 75, 20, "§71 Day", 0x555555));
        this.buttonList.add(new ColorButton(6, centerX - 76, centerY + 45, 75, 20, "§77 Days", 0x555555));
        this.buttonList.add(new ColorButton(7, centerX, centerY + 45, 75, 20, "§730 Days", 0x555555));
        this.buttonList.add(new ColorButton(8, centerX + 76, centerY + 45, 75, 20, "§4Permanent", 0xAA0000));
        
        // Action buttons
        this.buttonList.add(new ColorButton(9, centerX - 100, centerY + 80, 95, 20, "§c§lPunish", 0xFF0000));
        this.buttonList.add(new ColorButton(10, centerX + 5, centerY + 80, 95, 20, "§7§lCancel", 0x555555));
    }
    
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        
        // Draw title
        drawCenteredString(fontRendererObj, "§c§lPunishment Menu", width / 2, 20, 0xFFFFFF);
        
        // Draw input labels
        drawString(fontRendererObj, "§7Player Name:", width / 2 - 100, height / 2 - 72, 0xFFFFFF);
        drawString(fontRendererObj, "§7Reason:", width / 2 - 100, height / 2 - 32, 0xFFFFFF);
        
        // Draw selected options
        String info = String.format("§8Selected: §c%s §8| §7Duration: §c%s", 
            selectedPunishment.toUpperCase(),
            permanent ? "PERMANENT" : duration + " days"
        );
        drawCenteredString(fontRendererObj, info, width / 2, height / 2 + 5, 0xFFFFFF);
        
        // Draw text fields
        playerField.drawTextBox();
        reasonField.drawTextBox();
        
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
    
    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == 1) {
            mc.displayGuiScreen(parentScreen);
            return;
        }
        
        playerField.textboxKeyTyped(typedChar, keyCode);
        reasonField.textboxKeyTyped(typedChar, keyCode);
        
        super.keyTyped(typedChar, keyCode);
    }
    
    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        playerField.mouseClicked(mouseX, mouseY, mouseButton);
        reasonField.mouseClicked(mouseX, mouseY, mouseButton);
        
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }
    
    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        switch (button.id) {
            case 1: // Ban
                selectedPunishment = "ban";
                updateButtonColors();
                break;
            case 2: // Mute
                selectedPunishment = "mute";
                updateButtonColors();
                break;
            case 3: // Kick
                selectedPunishment = "kick";
                updateButtonColors();
                break;
            case 4: // Warn
                selectedPunishment = "warn";
                updateButtonColors();
                break;
            case 5: // 1 Day
                duration = 1;
                permanent = false;
                updateDurationButtons();
                break;
            case 6: // 7 Days
                duration = 7;
                permanent = false;
                updateDurationButtons();
                break;
            case 7: // 30 Days
                duration = 30;
                permanent = false;
                updateDurationButtons();
                break;
            case 8: // Permanent
                permanent = true;
                updateDurationButtons();
                break;
            case 9: // Execute Punishment
                executePunishment();
                break;
            case 10: // Cancel
                mc.displayGuiScreen(parentScreen);
                break;
        }
    }
    
    private void updateButtonColors() {
        for (GuiButton button : this.buttonList) {
            if (button.id <= 4) {
                ColorButton cb = (ColorButton)button;
                boolean selected = (button.id == 1 && selectedPunishment.equals("ban")) ||
                                 (button.id == 2 && selectedPunishment.equals("mute")) ||
                                 (button.id == 3 && selectedPunishment.equals("kick")) ||
                                 (button.id == 4 && selectedPunishment.equals("warn"));
                cb.setSelected(selected);
            }
        }
    }
    
    private void updateDurationButtons() {
        for (GuiButton button : this.buttonList) {
            if (button.id >= 5 && button.id <= 8) {
                ColorButton cb = (ColorButton)button;
                boolean selected = (permanent && button.id == 8) ||
                                 (!permanent && ((button.id == 5 && duration == 1) ||
                                              (button.id == 6 && duration == 7) ||
                                              (button.id == 7 && duration == 30)));
                cb.setSelected(selected);
            }
        }
    }
    
    private void executePunishment() {
        String player = playerField.getText();
        String reason = reasonField.getText();
        
        if (player.isEmpty()) {
            return;
        }
        
        if (reason.isEmpty()) {
            reason = "No reason specified";
        }
        
        String command = String.format("/gpunish %s %s %s%s", 
            selectedPunishment,
            player,
            reason,
            permanent ? " -p" : " -t " + duration
        );

        
        
        mc.thePlayer.sendChatMessage(command);
        mc.displayGuiScreen(null);
    }
    
    private class ColorButton extends GuiButton {
        private final int baseColor;
        private boolean selected;
        private float hoverTime = 0;
        
        public ColorButton(int buttonId, int x, int y, int width, int height, String text, int color) {
            super(buttonId, x, y, width, height, text);
            this.baseColor = color;
        }
        
        public void setSelected(boolean selected) {
            this.selected = selected;
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
                
                int color = selected ? baseColor : new Color(40, 40, 40).getRGB();
                int hoverColor = selected ? new Color(baseColor).brighter().getRGB() 
                                       : new Color(60, 60, 60).getRGB();
                
                int finalColor = interpolateColor(color, hoverColor, hoverTime);
                
                drawRect(this.xPosition, this.yPosition,
                        this.xPosition + this.width, this.yPosition + this.height,
                        finalColor);
                
                this.drawCenteredString(mc.fontRendererObj, this.displayString,
                    this.xPosition + this.width / 2,
                    this.yPosition + (this.height - 8) / 2,
                    0xFFFFFF);
            }
        }
        
        private int interpolateColor(int color1, int color2, float factor) {
            int r1 = (color1 >> 16) & 0xFF;
            int g1 = (color1 >> 8) & 0xFF;
            int b1 = color1 & 0xFF;
            
            int r2 = (color2 >> 16) & 0xFF;
            int g2 = (color2 >> 8) & 0xFF;
            int b2 = color2 & 0xFF;
            
            int r = (int)(r1 + (r2 - r1) * factor);
            int g = (int)(g1 + (g2 - g1) * factor);
            int b = (int)(b1 + (b2 - b1) * factor);
            
            return (0xFF << 24) | (r << 16) | (g << 8) | b;
        }
    }
} 