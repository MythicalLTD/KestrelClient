package xyz.nayskutzu.mythicalclient.gui;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import xyz.nayskutzu.mythicalclient.gui.components.ColorButton;
import xyz.nayskutzu.mythicalclient.hacks.*;
import xyz.nayskutzu.mythicalclient.MythicalClientMod;
import xyz.nayskutzu.mythicalclient.utils.FriendlyPlayers;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ToggleGui extends GuiScreen {
    private final GuiScreen parentScreen;
    private GuiTextField searchField;
    private GuiTextField friendField;
    private String searchQuery = "";
    private String friendQuery = "";
    private List<ToggleFeature> features = new ArrayList<>();
    private List<ToggleFeature> filteredFeatures = new ArrayList<>();
    private List<String> friends = new ArrayList<>();
    private List<String> filteredFriends = new ArrayList<>();
    private int scrollOffset = 0;
    private int friendScrollOffset = 0;
    private static final int ITEMS_PER_PAGE = 4; // Further reduced to prevent overlapping
    private static final int FRIENDS_PER_PAGE = 5; // Further reduced to prevent overlapping
    private static final int BUTTON_HEIGHT = 30;
    private static final int BUTTON_SPACING = 10; // Increased spacing
    private static final int FRIEND_BUTTON_HEIGHT = 25;
    private static final int FRIEND_BUTTON_SPACING = 8; // Increased spacing

    private enum Tab {
        FEATURES,
        FRIENDS,
        PRESETS
    }

    private Tab currentTab = Tab.FEATURES;
    private long openTime;
    private static final int ANIMATION_DURATION = 300;

    public ToggleGui(GuiScreen parentScreen) {
        this.parentScreen = parentScreen;
        this.openTime = System.currentTimeMillis();
        initFeatures();
        loadFriends();
    }

    private void loadFriends() {
        try {
            friends.clear();
            List<String> friendsList = FriendlyPlayers.getFriends();
            if (friendsList != null) {
                friends.addAll(friendsList);
            }
            updateFilteredFriends();
        } catch (Exception e) {
            MythicalClientMod.sendMessageToChat("§cError loading friends: " + e.getMessage(), false);
            friends = new ArrayList<>();
        }
    }

    private void updateFilteredFriends() {
        try {
            filteredFriends.clear();
            for (String friend : friends) {
                if (friend != null && (friendQuery.isEmpty() ||
                        friend.toLowerCase().contains(friendQuery.toLowerCase()))) {
                    filteredFriends.add(friend);
                }
            }
        } catch (Exception e) {
            MythicalClientMod.sendMessageToChat("§cError filtering friends: " + e.getMessage(), false);
            filteredFriends = new ArrayList<>();
        }
    }

    private void initFeatures() {
        features.clear();

        // ESP Features
        features.add(new ToggleFeature("Player ESP", "See players through walls",
                () -> PlayerESP.main(), () -> PlayerESP.enabled, 0x55FF55, "ESP"));
        // Player ESP Overlay removed; use web overlay instead
        features.add(new ToggleFeature("Bed ESP", "Highlight bed locations",
                () -> BedESP.main(), () -> BedESP.enabled, 0xFFAA00, "ESP"));
        features.add(new ToggleFeature("Chest ESP", "Highlight chest locations",
                () -> ChestESP.main(), () -> ChestESP.enabled, 0xFFAA55, "ESP"));
        features.add(new ToggleFeature("Resource ESP", "Highlight resources",
                () -> ResourceESP.render(null), () -> ResourceESP.enabled, 0x55AAFF, "ESP"));

        // Utility Features
        features.add(new ToggleFeature("Tracers", "Draw lines to players",
                () -> Tracers.main(), () -> Tracers.enabled, 0xFF55FF, "Utility"));
        // Tracers Overlay removed; use web overlay instead
        features.add(new ToggleFeature("Trajectories", "Show projectile paths",
                () -> Trajectories.main(), () -> Trajectories.enabled, 0xFFFF55, "Utility"));
        features.add(new ToggleFeature("Player Health", "Show player health bars",
                () -> PlayerHealth.main(), () -> PlayerHealth.enabled, 0xFF5555, "Utility"));
        features.add(new ToggleFeature("TNT Timer", "Show TNT countdown",
                () -> TntTimer.main(), () -> TntTimer.enabled, 0xFF0000, "Utility"));

        // Detection Features
        features.add(new ToggleFeature("Bow Detector", "Detect bow shots",
                () -> BowDetector.main(), () -> BowDetector.enabled, 0xAA55AA, "Detection"));
        features.add(new ToggleFeature("Fireball Detector", "Detect fireballs",
                () -> FireballDetector.main(), () -> FireballDetector.enabled, 0xFF5500, "Detection"));
        features.add(new ToggleFeature("Near Player", "Alert when players are nearby",
                () -> NearPlayer.main(), () -> NearPlayer.enabled, 0x55FFAA, "Detection"));

        // Bridge Features
        features.add(new ToggleFeature("Bridge Hack", "Bridge assistance",
                () -> BridgeHack.main(), () -> BridgeHack.enabled, 0x55AA55, "Bridge"));

        // System Features
        features.add(new ToggleFeature("No GUI", "Prevent GUI from opening",
                () -> NoGUI.main(), () -> NoGUI.enabled, 0xAA0000, "System"));
        features.add(new ToggleFeature("Nuke Process", "Kill game process",
                () -> NukeProcess.main(), () -> false, 0xFF0000, "System"));

        // Resource Features
        features.add(new ToggleFeature("Resource Ground Finder", "Find resources on ground",
                () -> ResourceGroundFinder.main(), () -> ResourceGroundFinder.enabled, 0xAAFF55, "Resource"));

        updateFilteredFeatures();
    }

    private void updateFilteredFeatures() {
        filteredFeatures.clear();
        for (ToggleFeature feature : features) {
            if (searchQuery.isEmpty() ||
                    feature.getName().toLowerCase().contains(searchQuery.toLowerCase()) ||
                    feature.getDescription().toLowerCase().contains(searchQuery.toLowerCase()) ||
                    feature.getCategory().toLowerCase().contains(searchQuery.toLowerCase())) {
                filteredFeatures.add(feature);
            }
        }
    }

    @Override
    public void initGui() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        // Clear existing buttons
        this.buttonList.clear();

        // Tab buttons - positioned at the top
        this.buttonList.add(new ColorButton(1, centerX - 200, centerY - 140, 80, 25, "§6§lFeatures", 0xFFAA00));
        this.buttonList.add(new ColorButton(2, centerX - 120, centerY - 140, 80, 25, "§b§lFriends", 0x55AAFF));
        this.buttonList.add(new ColorButton(3, centerX - 40, centerY - 140, 80, 25, "§d§lPresets", 0xFF55FF));

        if (currentTab == Tab.FEATURES) {
            // Search field - positioned below tabs
            searchField = new GuiTextField(0, fontRendererObj, centerX - 150, centerY - 100, 300, 20);
            searchField.setMaxStringLength(50);
            searchField.setText(searchQuery);

            // Navigation buttons - positioned at the bottom
            this.buttonList.add(new ColorButton(10, centerX - 200, centerY + 120, 80, 20, "§7Previous", 0x555555));
            this.buttonList.add(new ColorButton(11, centerX + 120, centerY + 120, 80, 20, "§7Next", 0x555555));

            // Add toggle buttons for visible features - positioned in the middle
            int startY = centerY - 60;
            int buttonId = 20;

            for (int i = 0; i < Math.min(ITEMS_PER_PAGE, filteredFeatures.size() - scrollOffset); i++) {
                ToggleFeature feature = filteredFeatures.get(i + scrollOffset);
                int y = startY + (i * (BUTTON_HEIGHT + BUTTON_SPACING));

                // Ensure buttons don't overlap by checking spacing
                if (i > 0) {
                    int prevY = startY + ((i - 1) * (BUTTON_HEIGHT + BUTTON_SPACING));
                    if (y <= prevY + BUTTON_HEIGHT) {
                        y = prevY + BUTTON_HEIGHT + BUTTON_SPACING;
                    }
                }

                this.buttonList.add(new ColorButton(buttonId + i, centerX - 150, y, 300, BUTTON_HEIGHT,
                        feature.getName(), feature.getColor()));
            }
        } else if (currentTab == Tab.FRIENDS) {
            try {
                // Friend management - positioned below tabs
                friendField = new GuiTextField(0, fontRendererObj, centerX - 150, centerY - 100, 200, 20);
                friendField.setMaxStringLength(16);
                friendField.setText(friendQuery);

                // Add friend button - positioned next to field
                this.buttonList.add(new ColorButton(30, centerX + 60, centerY - 100, 80, 20, "§a§lAdd", 0x55FF55));

                // Friend navigation - positioned at the bottom
                this.buttonList.add(new ColorButton(31, centerX - 200, centerY + 120, 80, 20, "§7Previous", 0x555555));
                this.buttonList.add(new ColorButton(32, centerX + 120, centerY + 120, 80, 20, "§7Next", 0x555555));

                // Friend list buttons - positioned in the middle
                int startY = centerY - 60;
                int buttonId = 40;

                for (int i = 0; i < Math.min(FRIENDS_PER_PAGE, filteredFriends.size() - friendScrollOffset); i++) {
                    String friend = filteredFriends.get(i + friendScrollOffset);
                    if (friend != null) {
                        int y = startY + (i * (FRIEND_BUTTON_HEIGHT + FRIEND_BUTTON_SPACING));

                        // Ensure buttons don't overlap by checking spacing
                        if (i > 0) {
                            int prevY = startY + ((i - 1) * (FRIEND_BUTTON_HEIGHT + FRIEND_BUTTON_SPACING));
                            if (y <= prevY + FRIEND_BUTTON_HEIGHT) {
                                y = prevY + FRIEND_BUTTON_HEIGHT + FRIEND_BUTTON_SPACING;
                            }
                        }

                        this.buttonList.add(new ColorButton(buttonId + i, centerX - 150, y, 240, FRIEND_BUTTON_HEIGHT,
                                "§b" + friend, 0x55AAFF));
                        this.buttonList
                                .add(new ColorButton(buttonId + i + 1000, centerX + 90, y, 60, FRIEND_BUTTON_HEIGHT,
                                        "§cRemove", 0xFF5555));
                    }
                }
            } catch (Exception e) {
                MythicalClientMod.sendMessageToChat("§cError initializing friends tab: " + e.getMessage(), false);
                currentTab = Tab.FEATURES; // Fallback to features tab
                initGui();
                return;
            }
        } else if (currentTab == Tab.PRESETS) {
            // Preset buttons
            this.buttonList
                    .add(new ColorButton(50, centerX - 150, centerY - 60, 300, 30, "§6§lNaysKutzu Preset", 0xFFAA00));
            this.buttonList
                    .add(new ColorButton(51, centerX - 150, centerY - 20, 300, 30, "§b§lMaria Preset", 0x55AAFF));
            this.buttonList.add(new ColorButton(52, centerX - 150, centerY + 20, 300, 30, "§e§lDutu Preset", 0xFFFF55));
        }

        // Close button - positioned at the very bottom, separate from other elements
        this.buttonList.add(new ColorButton(999, centerX - 50, centerY + 160, 100, 20, "§cClose", 0xFF5555));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        try {
            // Calculate animation
            float animationProgress = Math.min(1, (System.currentTimeMillis() - openTime) / (float) ANIMATION_DURATION);

            drawDefaultBackground();

            // Draw animated title
            GlStateManager.pushMatrix();
            float scale = 1.5f * animationProgress;
            GlStateManager.translate(width / 2, 30, 0);
            GlStateManager.scale(scale, scale, 1);
            GlStateManager.translate(-width / 2, -30, 0);
            drawCenteredString(fontRendererObj, "§6§lMythical Client - Control Panel", width / 2, 30, 0xFFFFFF);
            GlStateManager.popMatrix();

            int centerX = this.width / 2;
            int centerY = this.height / 2;

            if (currentTab == Tab.FEATURES) {
                // Draw search label
                drawString(fontRendererObj, "§7Search Features:", centerX - 150, centerY - 115, 0xFFFFFF);

                // Draw page info
                int totalPages = (int) Math.ceil((double) filteredFeatures.size() / ITEMS_PER_PAGE);
                int currentPage = (scrollOffset / ITEMS_PER_PAGE) + 1;
                String pageInfo = String.format("§7Page %d/%d (%d features)", currentPage, totalPages,
                        filteredFeatures.size());
                drawCenteredString(fontRendererObj, pageInfo, centerX, centerY + 100, 0xFFFFFF);

                // Draw search field
                searchField.drawTextBox();
            } else if (currentTab == Tab.FRIENDS) {
                try {
                    // Draw friend management
                    drawString(fontRendererObj, "§7Add Friend:", centerX - 150, centerY - 115, 0xFFFFFF);

                    // Draw page info
                    int totalPages = (int) Math.ceil((double) filteredFriends.size() / FRIENDS_PER_PAGE);
                    int currentPage = (friendScrollOffset / FRIENDS_PER_PAGE) + 1;
                    String pageInfo = String.format("§7Page %d/%d (%d friends)", currentPage, totalPages,
                            filteredFriends.size());
                    drawCenteredString(fontRendererObj, pageInfo, centerX, centerY + 100, 0xFFFFFF);

                    // Draw friend field
                    friendField.drawTextBox();
                } catch (Exception e) {
                    drawCenteredString(fontRendererObj, "§cError loading friends", centerX, centerY, 0xFFFFFF);
                }
            } else if (currentTab == Tab.PRESETS) {
                // Draw presets info
                drawCenteredString(fontRendererObj, "§d§lQuick Presets", centerX, centerY - 100, 0xFFFFFF);
                drawCenteredString(fontRendererObj, "§7Click a preset to apply it instantly", centerX, centerY - 80,
                        0xFFFFFF);
            }

            super.drawScreen(mouseX, mouseY, partialTicks);

            // Draw tooltips on top of everything
            if (currentTab == Tab.FEATURES) {
                drawFeatureTooltips(mouseX, mouseY, centerX, centerY);
            }
        } catch (Exception e) {
            MythicalClientMod.sendMessageToChat("§cError drawing GUI: " + e.getMessage(), false);
            mc.displayGuiScreen(parentScreen);
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == 1) { // ESC
            mc.displayGuiScreen(parentScreen);
            return;
        }

        try {
            if (currentTab == Tab.FEATURES && searchField != null && searchField.isFocused()) {
                searchField.textboxKeyTyped(typedChar, keyCode);
                searchQuery = searchField.getText();
                updateFilteredFeatures();
                scrollOffset = 0;
                initGui();
            } else if (currentTab == Tab.FRIENDS && friendField != null && friendField.isFocused()) {
                friendField.textboxKeyTyped(typedChar, keyCode);
                friendQuery = friendField.getText();
                updateFilteredFriends();
                friendScrollOffset = 0;
                initGui();
            }
        } catch (Exception e) {
            MythicalClientMod.sendMessageToChat("§cError handling key input: " + e.getMessage(), false);
        }

        super.keyTyped(typedChar, keyCode);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        try {
            if (currentTab == Tab.FEATURES && searchField != null) {
                searchField.mouseClicked(mouseX, mouseY, mouseButton);
            } else if (currentTab == Tab.FRIENDS && friendField != null) {
                friendField.mouseClicked(mouseX, mouseY, mouseButton);
            }
        } catch (Exception e) {
            MythicalClientMod.sendMessageToChat("§cError handling mouse input: " + e.getMessage(), false);
        }

        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        try {
            switch (button.id) {
                case 1: // Features tab
                    currentTab = Tab.FEATURES;
                    initGui();
                    break;
                case 2: // Friends tab
                    currentTab = Tab.FRIENDS;
                    initGui();
                    break;
                case 3: // Presets tab
                    currentTab = Tab.PRESETS;
                    initGui();
                    break;
                case 10: // Previous (features)
                    if (scrollOffset > 0) {
                        scrollOffset = Math.max(0, scrollOffset - ITEMS_PER_PAGE);
                        initGui();
                    }
                    break;
                case 11: // Next (features)
                    if (scrollOffset + ITEMS_PER_PAGE < filteredFeatures.size()) {
                        scrollOffset += ITEMS_PER_PAGE;
                        initGui();
                    }
                    break;
                case 30: // Add friend
                    String newFriend = friendField.getText().trim();
                    if (!newFriend.isEmpty() && !friends.contains(newFriend)) {
                        friends.add(newFriend);
                        try {
                            FriendlyPlayers.addFriend(newFriend);
                        } catch (Exception e) {
                            MythicalClientMod.sendMessageToChat("§cError adding friend to system: " + e.getMessage(),
                                    false);
                        }
                        friendField.setText("");
                        friendQuery = "";
                        updateFilteredFriends();
                        friendScrollOffset = 0;
                        initGui();
                        MythicalClientMod.sendMessageToChat("§aAdded friend: §f" + newFriend, false);
                    }
                    break;
                case 31: // Previous (friends)
                    if (friendScrollOffset > 0) {
                        friendScrollOffset = Math.max(0, friendScrollOffset - FRIENDS_PER_PAGE);
                        initGui();
                    }
                    break;
                case 32: // Next (friends)
                    if (friendScrollOffset + FRIENDS_PER_PAGE < filteredFriends.size()) {
                        friendScrollOffset += FRIENDS_PER_PAGE;
                        initGui();
                    }
                    break;
                case 50: // NaysKutzu Preset
                    applyNaysKutzuPreset();
                    break;
                case 51: // Maria Preset
                    applyMariaPreset();
                    break;
                case 52: // Dutu Preset
                    applyDutuPreset();
                    break;
                case 999: // Close
                    mc.displayGuiScreen(parentScreen);
                    break;
                default:
                    // Handle feature toggles
                    if (button.id >= 20 && button.id < 100) {
                        int featureIndex = button.id - 20;
                        if (featureIndex >= 0
                                && featureIndex < Math.min(ITEMS_PER_PAGE, filteredFeatures.size() - scrollOffset)) {
                            ToggleFeature feature = filteredFeatures.get(featureIndex + scrollOffset);
                            try {
                                feature.toggle();
                                MythicalClientMod.sendMessageToChat("§7" + feature.getName() + " toggled!", false);
                            } catch (Exception e) {
                                MythicalClientMod.sendMessageToChat(
                                        "§cError toggling " + feature.getName() + ": " + e.getMessage(), false);
                            }
                        }
                    }
                    // Handle friend removal
                    else if (button.id >= 1040 && button.id < 2000) {
                        int friendIndex = button.id - 1040;
                        if (friendIndex >= 0 && friendIndex < Math.min(FRIENDS_PER_PAGE,
                                filteredFriends.size() - friendScrollOffset)) {
                            String friendToRemove = filteredFriends.get(friendIndex + friendScrollOffset);
                            friends.remove(friendToRemove);
                            try {
                                FriendlyPlayers.removeFriend(friendToRemove);
                            } catch (Exception e) {
                                MythicalClientMod.sendMessageToChat(
                                        "§cError removing friend from system: " + e.getMessage(), false);
                            }
                            updateFilteredFriends();
                            initGui();
                            MythicalClientMod.sendMessageToChat("§cRemoved friend: §f" + friendToRemove, false);
                        }
                    }
                    break;
            }
        } catch (Exception e) {
            MythicalClientMod.sendMessageToChat("§cError handling button action: " + e.getMessage(), false);
        }
    }

    private void applyNaysKutzuPreset() {
        try {
            // Disable all features first
            disableAllFeatures();

            // Enable NaysKutzu's preset features
            enableFeatureByName("Fireball Detector");
            enableFeatureByName("Player ESP");
            enableFeatureByName("Tracers");
            enableFeatureByName("Bow Detector");
            enableFeatureByName("Player Health");
            enableFeatureByName("Bridge Hack");
            enableFeatureByName("Trajectories");

            MythicalClientMod.sendMessageToChat("§aApplied NaysKutzu preset!", false);
        } catch (Exception e) {
            MythicalClientMod.sendMessageToChat("§cError applying NaysKutzu preset: " + e.getMessage(), false);
        }
    }

    private void applyMariaPreset() {
        try {
            // Disable all features first
            disableAllFeatures();

            // Enable Maria's preset features
            enableFeatureByName("Fireball Detector");
            enableFeatureByName("Tracers");
            enableFeatureByName("Bow Detector");
            enableFeatureByName("Player Health");
            enableFeatureByName("Bridge Hack");
            enableFeatureByName("Trajectories");

            MythicalClientMod.sendMessageToChat("§aApplied Maria preset!", false);
        } catch (Exception e) {
            MythicalClientMod.sendMessageToChat("§cError applying Maria preset: " + e.getMessage(), false);
        }
    }

    private void applyDutuPreset() {
        try {
            // Disable all features first
            disableAllFeatures();

            // Enable Dutu's preset features (same as Maria)
            enableFeatureByName("Fireball Detector");
            enableFeatureByName("Tracers");
            enableFeatureByName("Bow Detector");
            enableFeatureByName("Player Health");
            enableFeatureByName("Bridge Hack");
            enableFeatureByName("Trajectories");

            MythicalClientMod.sendMessageToChat("§aApplied Dutu preset!", false);
        } catch (Exception e) {
            MythicalClientMod.sendMessageToChat("§cError applying Dutu preset: " + e.getMessage(), false);
        }
    }

    private void disableAllFeatures() {
        for (ToggleFeature feature : features) {
            if (feature.isEnabled()) {
                feature.toggle();
            }
        }
    }

    private void enableFeatureByName(String featureName) {
        for (ToggleFeature feature : features) {
            if (feature.getName().equals(featureName) && !feature.isEnabled()) {
                feature.toggle();
                break;
            }
        }
    }

    private void drawFeatureTooltips(int mouseX, int mouseY, int centerX, int centerY) {
        try {
            // Draw feature descriptions for hovered buttons
            for (int i = 0; i < Math.min(ITEMS_PER_PAGE, filteredFeatures.size() - scrollOffset); i++) {
                ToggleFeature feature = filteredFeatures.get(i + scrollOffset);
                int y = centerY - 60 + (i * (BUTTON_HEIGHT + BUTTON_SPACING));

                // Check if mouse is hovering over this button
                if (mouseX >= centerX - 150 && mouseX <= centerX + 150 &&
                        mouseY >= y && mouseY <= y + BUTTON_HEIGHT) {

                    // Draw description tooltip
                    List<String> tooltip = new ArrayList<>();
                    tooltip.add("§6" + feature.getName());
                    tooltip.add("§7" + feature.getDescription());
                    tooltip.add("§8Category: §7" + feature.getCategory());
                    tooltip.add(feature.isEnabled() ? "§aEnabled" : "§cDisabled");

                    drawHoveringText(tooltip, mouseX, mouseY);
                    break;
                }
            }
        } catch (Exception e) {
            // Silently handle tooltip errors
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return true;
    }

    // Inner class to represent toggleable features
    private static class ToggleFeature {
        private final String name;
        private final String description;
        private final Runnable toggleAction;
        private final java.util.function.BooleanSupplier isEnabledSupplier;
        private final int color;
        private final String category;

        public ToggleFeature(String name, String description, Runnable toggleAction,
                java.util.function.BooleanSupplier isEnabledSupplier, int color, String category) {
            this.name = name;
            this.description = description;
            this.toggleAction = toggleAction;
            this.isEnabledSupplier = isEnabledSupplier;
            this.color = color;
            this.category = category;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public String getCategory() {
            return category;
        }

        public void toggle() {
            toggleAction.run();
        }

        public boolean isEnabled() {
            try {
                return isEnabledSupplier.getAsBoolean();
            } catch (Exception e) {
                return false;
            }
        }

        public int getColor() {
            return isEnabled() ? color : 0x555555;
        }
    }
}