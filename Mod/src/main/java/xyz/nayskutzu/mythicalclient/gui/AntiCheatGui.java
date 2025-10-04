package xyz.nayskutzu.mythicalclient.gui;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import xyz.nayskutzu.mythicalclient.gui.components.ColorButton;
import xyz.nayskutzu.mythicalclient.data.MockDataManager;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AntiCheatGui extends GuiScreen {
    private final GuiScreen parentScreen;
    private List<Alert> recentAlerts = new ArrayList<>();
    private boolean alertsEnabled = false;
    private float scrollOffset = 0;
    private static final int ALERT_HEIGHT = 40;
    
    public AntiCheatGui(GuiScreen parentScreen) {
        this.parentScreen = parentScreen;
        this.recentAlerts = MockDataManager.getRecentAlerts().stream()
            .map(alert -> new Alert(alert.player, alert.hack, alert.violations, alert.timestamp))
            .collect(Collectors.toList());
    }
    
    @Override
    public void initGui() {
        int centerX = this.width / 2;
        
        // Toggle Alerts button
        this.buttonList.add(new ColorButton(1, 
            centerX - 100, 40, 200, 20, 
            alertsEnabled ? "§c§lDisable Alerts" : "§a§lEnable Alerts",
            alertsEnabled ? 0xFF5555 : 0x55FF55));
            
        // Filter buttons
        this.buttonList.add(new ColorButton(2, centerX - 152, 70, 75, 20, "§e§lCombat", 0xFFAA00));
        this.buttonList.add(new ColorButton(3, centerX - 76, 70, 75, 20, "§b§lMovement", 0x55FFFF));
        this.buttonList.add(new ColorButton(4, centerX, 70, 75, 20, "§d§lOther", 0xFF55FF));
        this.buttonList.add(new ColorButton(5, centerX + 76, 70, 75, 20, "§7§lAll", 0x555555));
        
        // Settings button
        this.buttonList.add(new ColorButton(6, this.width - 85, 10, 75, 20, "§7§lSettings", 0x555555));
        
        // Back button
        this.buttonList.add(new ColorButton(7, centerX - 100, this.height - 30, 200, 20, "§7§lBack", 0x555555));
    }
    
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        
        // Draw title
        drawCenteredString(fontRendererObj, "§c§lAnti-Cheat Dashboard", width / 2, 15, 0xFFFFFF);
        
        // Draw alerts section
        int startY = 100;
        int alertX = width / 2 - 150;
        
        // Draw alerts background
        drawRect(alertX, startY, alertX + 300, height - 40, new Color(0, 0, 0, 100).getRGB());
        
        // Draw alerts
        GlStateManager.pushMatrix();
        GlStateManager.translate(0, -scrollOffset, 0);
        
        int y = startY + 5;
        for (Alert alert : recentAlerts) {
            if (y + ALERT_HEIGHT > startY && y < height - 40) {
                drawAlert(alert, alertX + 5, y, 290);
            }
            y += ALERT_HEIGHT + 5;
        }
        
        GlStateManager.popMatrix();
        
        // Draw stats
        String stats = String.format("§7Alerts: §c%d §8| §7Violations: §c%d §8| §7Players: §c%d",
            recentAlerts.size(), getTotalViolations(), getUniquePlayerCount());
        drawCenteredString(fontRendererObj, stats, width / 2, height - 45, 0xFFFFFF);
        
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
    
    private void drawAlert(Alert alert, int x, int y, int width) {
        // Alert background
        drawRect(x, y, x + width, y + ALERT_HEIGHT - 2, new Color(40, 40, 40, 200).getRGB());
        
        // Alert content
        String timeAgo = getTimeAgo(alert.timestamp);
        drawString(fontRendererObj, "§c" + alert.player, x + 5, y + 5, 0xFFFFFF);
        drawString(fontRendererObj, "§7" + alert.hack + " §8(x" + alert.violations + ")", x + 5, y + 20, 0xFFFFFF);
        drawString(fontRendererObj, "§8" + timeAgo, x + width - fontRendererObj.getStringWidth(timeAgo) - 5, y + 5, 0xFFFFFF);
        
        // Violation level indicator
        int barWidth = 50;
        int barX = x + width - barWidth - 5;
        int barY = y + 20;
        drawRect(barX, barY, barX + barWidth, barY + 5, new Color(20, 20, 20).getRGB());
        drawRect(barX, barY, barX + (int)(barWidth * (alert.violations / 10f)), barY + 5, 
            new Color(255, (int)(255 * (1 - alert.violations / 10f)), 0).getRGB());
    }
    
    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }
    
    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        
        int scroll = org.lwjgl.input.Mouse.getEventDWheel();
        if (scroll != 0) {
            scrollOffset += (scroll > 0) ? -20 : 20;
            scrollOffset = Math.max(0, Math.min(scrollOffset, 
                Math.max(0, recentAlerts.size() * (ALERT_HEIGHT + 5) - (height - 140))));
        }
    }
    
    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        switch (button.id) {
            case 1: // Toggle Alerts
                alertsEnabled = !alertsEnabled;
                button.displayString = alertsEnabled ? "§c§lDisable Alerts" : "§a§lEnable Alerts";
                ((ColorButton)button).setColor(alertsEnabled ? 0xFF5555 : 0x55FF55);
                break;
            case 7: // Back
                mc.displayGuiScreen(parentScreen);
                break;
        }
    }
    
    private int getTotalViolations() {
        return recentAlerts.stream().mapToInt(a -> a.violations).sum();
    }
    
    private int getUniquePlayerCount() {
        return (int) recentAlerts.stream().map(a -> a.player).distinct().count();
    }
    
    private String getTimeAgo(long timestamp) {
        long seconds = (System.currentTimeMillis() - timestamp) / 1000;
        if (seconds < 60) return seconds + "s ago";
        if (seconds < 3600) return (seconds / 60) + "m ago";
        return (seconds / 3600) + "h ago";
    }
    
    private static class Alert {
        String player;
        String hack;
        int violations;
        long timestamp;
        
        Alert(String player, String hack, int violations, long timestamp) {
            this.player = player;
            this.hack = hack;
            this.violations = violations;
            this.timestamp = timestamp;
        }
    }
} 