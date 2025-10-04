package xyz.nayskutzu.mythicalclient.gui;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import xyz.nayskutzu.mythicalclient.gui.components.ColorButton;
import net.minecraft.client.gui.GuiButton;
import java.io.IOException;
import net.minecraft.client.Minecraft;
import java.util.ArrayList;
import java.util.List;
import xyz.nayskutzu.mythicalclient.data.MockDataManager;

public class PlayerLookupGui extends GuiScreen {
    private final GuiScreen parentScreen;
    private GuiTextField searchField;
    private List<String> recentSearches = new ArrayList<>();
    private String selectedPlayer = null;
    private boolean isLoading = false;
    
    public PlayerLookupGui(GuiScreen parentScreen) {
        this.parentScreen = parentScreen;
        // Add some mock recent searches
        recentSearches.add("NaysKutzu");
        recentSearches.add("Notch");
        recentSearches.add("Dream");
    }
    
    @Override
    public void initGui() {
        int centerX = this.width / 2;
        
        // Search field
        searchField = new GuiTextField(0, fontRendererObj, centerX - 100, 50, 200, 20);
        searchField.setMaxStringLength(16);
        searchField.setFocused(true);
        
        // Search button
        this.buttonList.add(new ColorButton(1, centerX + 105, 50, 60, 20, "§b§lSearch", 0x55FFFF));
        
        // Player info buttons
        if (selectedPlayer != null) {
            int startY = 120;
            this.buttonList.add(new ColorButton(2, centerX - 100, startY, 200, 20, "§e§lView History", 0xFFAA00));
            this.buttonList.add(new ColorButton(3, centerX - 100, startY + 25, 200, 20, "§c§lPunishments", 0xFF5555));
            this.buttonList.add(new ColorButton(4, centerX - 100, startY + 50, 200, 20, "§a§lReports", 0x55FF55));
            this.buttonList.add(new ColorButton(5, centerX - 100, startY + 75, 200, 20, "§b§lNotes", 0x55FFFF));
        }
        
        // Back button
        this.buttonList.add(new ColorButton(6, centerX - 100, this.height - 40, 200, 20, "§7§lBack", 0x555555));
    }
    
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        
        // Draw title
        drawCenteredString(fontRendererObj, "§b§lPlayer Lookup", width / 2, 20, 0xFFFFFF);
        
        // Draw search field
        searchField.drawTextBox();
        
        // Draw recent searches
        if (selectedPlayer == null && !recentSearches.isEmpty()) {
            drawString(fontRendererObj, "§7Recent Searches:", 10, 80, 0xFFFFFF);
            int y = 95;
            for (String player : recentSearches) {
                boolean hover = mouseX >= 10 && mouseX <= 200 && 
                              mouseY >= y && mouseY <= y + 12;
                drawString(fontRendererObj, 
                    (hover ? "§b> " : "§7") + player,
                    10, y, 0xFFFFFF);
                y += 15;
            }
        }
        
        // Draw player info if selected
        if (selectedPlayer != null) {
            drawPlayerInfo();
        }
        
        // Draw loading animation
        if (isLoading) {
            drawCenteredString(fontRendererObj, 
                "§7Loading" + getDots(), 
                width / 2, height / 2, 0xFFFFFF);
        }
        
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
    
    private void drawPlayerInfo() {
        int centerX = width / 2;
        MockDataManager.PlayerData data = new MockDataManager.PlayerData(Minecraft.getMinecraft().thePlayer);
        
        // Draw player head and name
        drawCenteredString(fontRendererObj, 
            "§b" + data.name, 
            centerX, 90, 0xFFFFFF);
            
        // Draw status
        String status = data.isOnline ? "§aOnline" : "§cOffline";
        drawCenteredString(fontRendererObj, 
            "§7Status: " + status + " §7- §e" + data.gameMode, 
            centerX, 105, 0xFFFFFF);
        
        // Draw statistics
        int y = 130;
        drawCenteredString(fontRendererObj, 
            String.format("§7Violations: §c%d §8| §7Reports: §e%d §8| §7Warnings: §6%d",
                data.violations, data.reports, data.warnings),
            centerX, y, 0xFFFFFF);
    }
    
    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        searchField.mouseClicked(mouseX, mouseY, mouseButton);
        
        // Check recent searches clicks
        if (selectedPlayer == null && !recentSearches.isEmpty()) {
            int y = 95;
            for (String player : recentSearches) {
                if (mouseX >= 10 && mouseX <= 200 && 
                    mouseY >= y && mouseY <= y + 12) {
                    selectPlayer(player);
                    break;
                }
                y += 15;
            }
        }
        
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }
    
    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == 1) {
            mc.displayGuiScreen(parentScreen);
            return;
        }
        
        if (keyCode == 28) { // Enter key
            searchPlayer();
        }
        
        searchField.textboxKeyTyped(typedChar, keyCode);
        super.keyTyped(typedChar, keyCode);
    }
    
    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        switch (button.id) {
            case 1: // Search
                searchPlayer();
                break;
            case 2: // History
                // TODO: Show history GUI
                break;
            case 3: // Punishments
                // TODO: Show punishments GUI
                break;
            case 4: // Reports
                // TODO: Show reports GUI
                break;
            case 5: // Notes
                // TODO: Show notes GUI
                break;
            case 6: // Back
                mc.displayGuiScreen(parentScreen);
                break;
        }
    }
    
    private void searchPlayer() {
        String query = searchField.getText().trim();
        if (!query.isEmpty()) {
            isLoading = true;
            selectPlayer(query);
        }
    }
    
    private void selectPlayer(String player) {
        selectedPlayer = player;
        if (!recentSearches.contains(player)) {
            recentSearches.add(0, player);
            if (recentSearches.size() > 5) {
                recentSearches.remove(recentSearches.size() - 1);
            }
        }
        isLoading = false;
        initGui();
    }
    
    private String getDots() {
        int count = (int)((System.currentTimeMillis() / 500) % 4);
        StringBuilder dots = new StringBuilder();
        for (int i = 0; i < count; i++) {
            dots.append(".");
        }
        return dots.toString();
    }
} 