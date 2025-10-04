package xyz.nayskutzu.mythicalclient.gui;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import xyz.nayskutzu.mythicalclient.gui.components.ColorButton;
import xyz.nayskutzu.mythicalclient.commands.FakeStaffCommand;
import net.minecraft.util.ChatComponentText;
import org.lwjgl.input.Keyboard;
import java.io.IOException;


public class GamsterDashboard extends GuiScreen {
    private float animationProgress = 0;
    private long openTime;
    private static final int ANIMATION_DURATION = 200;
    private GuiTextField playerNameField;
    private GuiTextField reasonField;
    private GuiTextField durationField;
    private String selectedPunishment = null;
    private static final String[] PUNISHMENT_TYPES = {"Ban", "Mute", "Kick", "Warn"};
    private FakeStaffCommand fakeStaffCommand;
    
    public GamsterDashboard() {
        this.openTime = System.currentTimeMillis();
        this.fakeStaffCommand = new FakeStaffCommand();
    }
    
    @Override
    public void initGui() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        
        // Player name input field
        playerNameField = new GuiTextField(0, fontRendererObj, centerX - 100, centerY - 60, 200, 20);
        playerNameField.setMaxStringLength(16);
        playerNameField.setFocused(true);

        // Duration field (for ban/mute)
        durationField = new GuiTextField(1, fontRendererObj, centerX - 100, centerY - 30, 200, 20);
        durationField.setMaxStringLength(20);

        // Reason field
        reasonField = new GuiTextField(2, fontRendererObj, centerX - 100, centerY, 200, 20);
        reasonField.setMaxStringLength(100);
        
        // Punishment buttons
        int buttonWidth = 75;
        int spacing = 10;
        int totalWidth = (buttonWidth * PUNISHMENT_TYPES.length) + (spacing * (PUNISHMENT_TYPES.length - 1));
        int startX = centerX - (totalWidth / 2);
        
        for (int i = 0; i < PUNISHMENT_TYPES.length; i++) {
            int x = startX + (i * (buttonWidth + spacing));
            String type = PUNISHMENT_TYPES[i];
            int color;
            switch(type) {
                case "Ban":
                    color = 0xFF5555; // Red
                    break;
                case "Mute": 
                    color = 0xFFAA00; // Orange
                    break;
                case "Kick":
                    color = 0xFF55FF; // Pink
                    break;
                case "Warn":
                    color = 0xFFFF55; // Yellow
                    break;
                default:
                    color = 0xAAAAAA; // Gray
            }
            this.buttonList.add(new ColorButton(i, x, centerY + 30, buttonWidth, 20, "§l" + type, color));
        }
        
        // Quick Actions
        this.buttonList.add(new ColorButton(8, centerX - 100, centerY + 60, 95, 20, "§c§lDisconnect", 0xFF5555));
        this.buttonList.add(new ColorButton(9, centerX + 5, centerY + 60, 95, 20, "§6§lToggle GUI", 0xFFAA00));
    }
    
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        // Calculate animation
        animationProgress = Math.min(1, (System.currentTimeMillis() - openTime) / (float)ANIMATION_DURATION);
        
        // Draw background with blur
        drawDefaultBackground();
        
        // Draw title
        GlStateManager.pushMatrix();
        float scale = 2.0f * animationProgress;
        GlStateManager.translate(width / 2, 30, 0);
        GlStateManager.scale(scale, scale, 1);
        GlStateManager.translate(-width / 2, -30, 0);
        drawCenteredString(fontRendererObj, "§c§lGamster Dashboard", width / 2, 30, 0xFFFFFF);
        GlStateManager.popMatrix();

        // Draw field labels
        drawCenteredString(fontRendererObj, "§7Enter Player Name:", width / 2, height - 75, 0xFFFFFF);
        playerNameField.drawTextBox();
        
        drawCenteredString(fontRendererObj, "§7Duration (e.g. 30d, perm):", width / 2, height - 45, 0xFFFFFF);
        durationField.drawTextBox();

        drawCenteredString(fontRendererObj, "§7Reason:", width / 2, height - 15, 0xFFFFFF);
        reasonField.drawTextBox();
        
        // Draw selected punishment info
        if (selectedPunishment != null) {
            drawCenteredString(fontRendererObj, 
                String.format("§7Selected: §f%s", selectedPunishment),
                width / 2, height - 50, 0xFFFFFF);
        }
        
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == 1 || keyCode == Keyboard.KEY_RSHIFT) {  // ESC or Right Shift
            mc.displayGuiScreen(null);
            return;
        }
        
        if (playerNameField.isFocused()) {
            playerNameField.textboxKeyTyped(typedChar, keyCode);
        } else if (reasonField.isFocused()) {
            reasonField.textboxKeyTyped(typedChar, keyCode);
        } else if (durationField.isFocused()) {
            durationField.textboxKeyTyped(typedChar, keyCode);
        }
        
        // Tab between fields
        if (keyCode == Keyboard.KEY_TAB) {
            if (playerNameField.isFocused()) {
                playerNameField.setFocused(false);
                durationField.setFocused(true);
            } else if (durationField.isFocused()) {
                durationField.setFocused(false);
                reasonField.setFocused(true);
            } else if (reasonField.isFocused()) {
                reasonField.setFocused(false);
                playerNameField.setFocused(true);
            } else {
                playerNameField.setFocused(true);
            }
        }
        
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        playerNameField.mouseClicked(mouseX, mouseY, mouseButton);
        reasonField.mouseClicked(mouseX, mouseY, mouseButton);
        durationField.mouseClicked(mouseX, mouseY, mouseButton);
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }
    
    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id < PUNISHMENT_TYPES.length) {
            String playerName = playerNameField.getText().trim();
            String reason = reasonField.getText().trim();
            String duration = durationField.getText().trim();
            
            if (!playerName.isEmpty()) {
                String punishmentType = PUNISHMENT_TYPES[button.id].toLowerCase();
                selectedPunishment = PUNISHMENT_TYPES[button.id];
                
                // Build command arguments
                String[] args;
                if (punishmentType.equals("kick") || punishmentType.equals("warn")) {
                    args = reason.isEmpty() ? 
                        new String[]{punishmentType, playerName} :
                        new String[]{punishmentType, playerName, reason};
                } else {
                    // For ban and mute, include duration if specified
                    if (!duration.isEmpty() && !reason.isEmpty()) {
                        args = new String[]{punishmentType, playerName, duration, reason};
                    } else if (!reason.isEmpty()) {
                        args = new String[]{punishmentType, playerName, reason};
                    } else {
                        args = new String[]{punishmentType, playerName};
                    }
                }
                
                try {
                    fakeStaffCommand.processCommand(mc.thePlayer, args);
                } catch (Exception e) {
                    mc.thePlayer.addChatMessage(new ChatComponentText("§cError executing punishment command."));
                }
            } else {
                mc.thePlayer.addChatMessage(new ChatComponentText("§cPlease enter a player name."));
            }
        } else if (button.id == 8) { // Disconnect button
            mc.displayGuiScreen(new DisconnectConfirmGui(this));
        } else if (button.id == 9) { // Toggle GUI button
            mc.displayGuiScreen(new ToggleGui(this));
        }
    }
    
    @Override
    public void updateScreen() {
        playerNameField.updateCursorCounter();
        reasonField.updateCursorCounter();
        durationField.updateCursorCounter();
        super.updateScreen();
    }
    
    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
} 