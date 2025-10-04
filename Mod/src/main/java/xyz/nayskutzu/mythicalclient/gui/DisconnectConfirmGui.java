package xyz.nayskutzu.mythicalclient.gui;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import xyz.nayskutzu.mythicalclient.gui.components.ColorButton;
import java.awt.Color;
import java.io.IOException;
import net.minecraft.client.gui.GuiMainMenu;

public class DisconnectConfirmGui extends GuiScreen {
    private final GuiScreen parentScreen;
    private float animationProgress = 0;
    private static final int ANIMATION_DURATION = 200; // milliseconds
    private long openTime;
    
    public DisconnectConfirmGui(GuiScreen parentScreen) {
        this.parentScreen = parentScreen;
        this.openTime = System.currentTimeMillis();
    }
    
    @Override
    public void initGui() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        
        // Action buttons
        this.buttonList.add(new ColorButton(0, 
            centerX - 105, centerY + 10, 
            100, 20, 
            "§c§lDisconnect", 
            0xFF5555
        ));
        
        this.buttonList.add(new ColorButton(1,
            centerX + 5, centerY + 10,
            100, 20,
            "§7§lCancel",
            0x555555
        ));
    }
    
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        // Draw darkened background
        drawDefaultBackground();
        
        // Calculate animation progress
        animationProgress = Math.min(1, (System.currentTimeMillis() - openTime) / (float)ANIMATION_DURATION);
        float scale = 0.7f + (0.3f * animationProgress);
        float alpha = animationProgress;
        
        // Draw popup background with animation
        GlStateManager.pushMatrix();
        GlStateManager.translate(width / 2, height / 2, 0);
        GlStateManager.scale(scale, scale, 1);
        GlStateManager.translate(-width / 2, -height / 2, 0);
        
        int popupWidth = 300;
        int popupHeight = 100;
        int left = width / 2 - popupWidth / 2;
        int top = height / 2 - popupHeight / 2;
        
        // Draw popup background
        drawRect(left, top, left + popupWidth, top + popupHeight, 
                new Color(30, 30, 30, (int)(200 * alpha)).getRGB());
        
        // Draw red accent line at top
        drawRect(left, top, left + popupWidth, top + 2, 
                new Color(255, 0, 0, (int)(255 * alpha)).getRGB());
        
        // Draw warning text
        GlStateManager.enableBlend();
        float textAlpha = alpha;
        int textColor = new Color(1f, 1f, 1f, textAlpha).getRGB();
        
        drawCenteredString(fontRendererObj, "§c§lDisconnect from Server?", 
            width / 2, height / 2 - 20, textColor);
        drawCenteredString(fontRendererObj, "§7Are you sure you want to disconnect?", 
            width / 2, height / 2 - 5, textColor);
        
        GlStateManager.popMatrix();
        
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
    
    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        switch (button.id) {
            case 0: // Disconnect
                mc.theWorld.sendQuittingDisconnectingPacket();
                mc.loadWorld(null);
                mc.displayGuiScreen(new GuiMainMenu());
                break;
            case 1: // Cancel
                mc.displayGuiScreen(parentScreen);
                break;
        }
    }
    
    @Override
    public void updateScreen() {
        super.updateScreen();
    }
    
    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == 1) { // ESC key
            mc.displayGuiScreen(parentScreen);
        }
    }
    
    @Override
    public boolean doesGuiPauseGame() {
        return true;
    }
} 