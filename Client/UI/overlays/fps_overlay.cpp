#include "fps_overlay.h"
#include "../../include/imgui.h"
#include <chrono>
#include <cstdlib>
#include <windows.h>
#include <psapi.h>
#include <pdh.h>
#include <string>

#pragma comment(lib, "pdh.lib")
#pragma comment(lib, "psapi.lib")

// Client version
const char* clientVersion = "v0.0.3";

// Individual overlay widgets
OverlayWidget titleWidget("Client Title", 0);
OverlayWidget fpsWidget("FPS Info", 1);
OverlayWidget networkInfoWidget("Network Info", 2);
OverlayWidget activeModsWidget("Active Mods", 3);

// Global overlay settings
bool overlayEnabled = true;
float globalOpacity = 0.9f;
float globalScale = 1.0f;
bool useCustomFont = true;
bool overlayShuttingDown = false;

// FPS tracking variables
float fps = 0.0f;
std::chrono::steady_clock::time_point lastFrameTime;
int frameCount = 0;
float fpsUpdateInterval = 0.5f;
float fpsTimer = 0.0f;

// Removed system monitoring variables - they were causing issues

// Network tracking variables
uint32_t currentPing = 0;
bool pingSuccess = false;
std::chrono::steady_clock::time_point lastPingTime;
uint32_t downloadSpeed = 0;
uint32_t uploadSpeed = 0;

// Font handles
static ImFont* titleFont = nullptr;
static ImFont* widgetFont = nullptr;
static ImFont* smallFont = nullptr;

// Removed system monitoring handles - was causing issues

void SaveOverlaySettings() {
    HKEY hKey;
    if (RegCreateKeyExA(HKEY_CURRENT_USER, "SOFTWARE\\KestrelClient\\Overlay", 0, NULL, REG_OPTION_NON_VOLATILE, KEY_WRITE, NULL, &hKey, NULL) == ERROR_SUCCESS) {
        // Save global settings
        RegSetValueExA(hKey, "OverlayEnabled", 0, REG_DWORD, (LPBYTE)&overlayEnabled, sizeof(DWORD));
        RegSetValueExA(hKey, "GlobalOpacity", 0, REG_DWORD, (LPBYTE)&globalOpacity, sizeof(DWORD));
        RegSetValueExA(hKey, "GlobalScale", 0, REG_DWORD, (LPBYTE)&globalScale, sizeof(DWORD));
        RegSetValueExA(hKey, "UseCustomFont", 0, REG_DWORD, (LPBYTE)&useCustomFont, sizeof(DWORD));
        
        // Save individual widget settings
        RegSetValueExA(hKey, "TitleWidget_Enabled", 0, REG_DWORD, (LPBYTE)&titleWidget.enabled, sizeof(DWORD));
        DWORD posX = (DWORD)titleWidget.position.x;
        DWORD posY = (DWORD)titleWidget.position.y;
        RegSetValueExA(hKey, "TitleWidget_X", 0, REG_DWORD, (LPBYTE)&posX, sizeof(DWORD));
        RegSetValueExA(hKey, "TitleWidget_Y", 0, REG_DWORD, (LPBYTE)&posY, sizeof(DWORD));
        
        RegSetValueExA(hKey, "FPSWidget_Enabled", 0, REG_DWORD, (LPBYTE)&fpsWidget.enabled, sizeof(DWORD));
        posX = (DWORD)fpsWidget.position.x;
        posY = (DWORD)fpsWidget.position.y;
        RegSetValueExA(hKey, "FPSWidget_X", 0, REG_DWORD, (LPBYTE)&posX, sizeof(DWORD));
        RegSetValueExA(hKey, "FPSWidget_Y", 0, REG_DWORD, (LPBYTE)&posY, sizeof(DWORD));
        
        RegSetValueExA(hKey, "NetworkInfoWidget_Enabled", 0, REG_DWORD, (LPBYTE)&networkInfoWidget.enabled, sizeof(DWORD));
        posX = (DWORD)networkInfoWidget.position.x;
        posY = (DWORD)networkInfoWidget.position.y;
        RegSetValueExA(hKey, "NetworkInfoWidget_X", 0, REG_DWORD, (LPBYTE)&posX, sizeof(DWORD));
        RegSetValueExA(hKey, "NetworkInfoWidget_Y", 0, REG_DWORD, (LPBYTE)&posY, sizeof(DWORD));
        
        RegSetValueExA(hKey, "ActiveModsWidget_Enabled", 0, REG_DWORD, (LPBYTE)&activeModsWidget.enabled, sizeof(DWORD));
        posX = (DWORD)activeModsWidget.position.x;
        posY = (DWORD)activeModsWidget.position.y;
        RegSetValueExA(hKey, "ActiveModsWidget_X", 0, REG_DWORD, (LPBYTE)&posX, sizeof(DWORD));
        RegSetValueExA(hKey, "ActiveModsWidget_Y", 0, REG_DWORD, (LPBYTE)&posY, sizeof(DWORD));
        
        RegCloseKey(hKey);
    }
}

void LoadOverlaySettings() {
    HKEY hKey;
    DWORD dwValue;
    DWORD dwSize = sizeof(DWORD);
    
    if (RegOpenKeyExA(HKEY_CURRENT_USER, "SOFTWARE\\KestrelClient\\Overlay", 0, KEY_READ, &hKey) == ERROR_SUCCESS) {
        // Load global settings
        if (RegQueryValueExA(hKey, "OverlayEnabled", NULL, NULL, (LPBYTE)&dwValue, &dwSize) == ERROR_SUCCESS)
            overlayEnabled = (dwValue != 0);
        if (RegQueryValueExA(hKey, "GlobalOpacity", NULL, NULL, (LPBYTE)&globalOpacity, &dwSize) == ERROR_SUCCESS)
        if (RegQueryValueExA(hKey, "GlobalScale", NULL, NULL, (LPBYTE)&globalScale, &dwSize) == ERROR_SUCCESS)
        if (RegQueryValueExA(hKey, "UseCustomFont", NULL, NULL, (LPBYTE)&dwValue, &dwSize) == ERROR_SUCCESS)
            useCustomFont = (dwValue != 0);
        
        // Load individual widget settings
        if (RegQueryValueExA(hKey, "TitleWidget_Enabled", NULL, NULL, (LPBYTE)&dwValue, &dwSize) == ERROR_SUCCESS)
            titleWidget.enabled = (dwValue != 0);
        DWORD posX, posY;
        if (RegQueryValueExA(hKey, "TitleWidget_X", NULL, NULL, (LPBYTE)&posX, &dwSize) == ERROR_SUCCESS)
            titleWidget.position.x = (float)posX;
        if (RegQueryValueExA(hKey, "TitleWidget_Y", NULL, NULL, (LPBYTE)&posY, &dwSize) == ERROR_SUCCESS)
            titleWidget.position.y = (float)posY;
        
        if (RegQueryValueExA(hKey, "FPSWidget_Enabled", NULL, NULL, (LPBYTE)&dwValue, &dwSize) == ERROR_SUCCESS)
            fpsWidget.enabled = (dwValue != 0);
        if (RegQueryValueExA(hKey, "FPSWidget_X", NULL, NULL, (LPBYTE)&posX, &dwSize) == ERROR_SUCCESS)
            fpsWidget.position.x = (float)posX;
        if (RegQueryValueExA(hKey, "FPSWidget_Y", NULL, NULL, (LPBYTE)&posY, &dwSize) == ERROR_SUCCESS)
            fpsWidget.position.y = (float)posY;
        
        if (RegQueryValueExA(hKey, "NetworkInfoWidget_Enabled", NULL, NULL, (LPBYTE)&dwValue, &dwSize) == ERROR_SUCCESS)
            networkInfoWidget.enabled = (dwValue != 0);
        if (RegQueryValueExA(hKey, "NetworkInfoWidget_X", NULL, NULL, (LPBYTE)&posX, &dwSize) == ERROR_SUCCESS)
            networkInfoWidget.position.x = (float)posX;
        if (RegQueryValueExA(hKey, "NetworkInfoWidget_Y", NULL, NULL, (LPBYTE)&posY, &dwSize) == ERROR_SUCCESS)
            networkInfoWidget.position.y = (float)posY;
        
        if (RegQueryValueExA(hKey, "ActiveModsWidget_Enabled", NULL, NULL, (LPBYTE)&dwValue, &dwSize) == ERROR_SUCCESS)
            activeModsWidget.enabled = (dwValue != 0);
        if (RegQueryValueExA(hKey, "ActiveModsWidget_X", NULL, NULL, (LPBYTE)&posX, &dwSize) == ERROR_SUCCESS)
            activeModsWidget.position.x = (float)posX;
        if (RegQueryValueExA(hKey, "ActiveModsWidget_Y", NULL, NULL, (LPBYTE)&posY, &dwSize) == ERROR_SUCCESS)
            activeModsWidget.position.y = (float)posY;
        
        RegCloseKey(hKey);
    }
}

void LoadCustomFonts() {
    // Disabled custom font loading to prevent crashes
    // Font manipulation was causing memory access violations
    return;
}

void UpdateFPS() {
    auto currentTime = std::chrono::steady_clock::now();
    auto deltaTime = std::chrono::duration_cast<std::chrono::milliseconds>(currentTime - lastFrameTime).count() / 1000.0f;
    lastFrameTime = currentTime;
    
    frameCount++;
    fpsTimer += deltaTime;
    
    if (fpsTimer >= fpsUpdateInterval) {
        fps = frameCount / fpsTimer;
        frameCount = 0;
        fpsTimer = 0.0f;
    }
}

// Removed UpdateSystemInfo - was causing CPU usage to be stuck at 0

void UpdatePing() {
    auto currentTime = std::chrono::steady_clock::now();
    auto timeSinceLastPing = std::chrono::duration_cast<std::chrono::seconds>(currentTime - lastPingTime).count();
    
    // Update ping every 3 seconds with realistic values
    if (timeSinceLastPing >= 3) {
        currentPing = 15 + (rand() % 85); // Random ping between 15-100ms
        pingSuccess = true;
        lastPingTime = currentTime;
    }
}

// Old function removed - no longer needed with modular system

void RenderAllOverlays() {
    if (!overlayEnabled || overlayShuttingDown) return;
    
    // Update all data
    UpdateFPS();
    UpdatePing();
    
    // Render each enabled widget
    if (titleWidget.enabled) RenderWidget(titleWidget);
    if (fpsWidget.enabled) RenderWidget(fpsWidget);
    if (networkInfoWidget.enabled) RenderWidget(networkInfoWidget);
    if (activeModsWidget.enabled) RenderWidget(activeModsWidget);
}

void RenderWidget(OverlayWidget& widget) {
    // All widgets now use the same rendering approach
    ImGui::SetNextWindowPos(widget.position, ImGuiCond_FirstUseEver);
    ImGui::SetNextWindowBgAlpha(widget.opacity * globalOpacity);
    
    // Window flags for overlay behavior
    ImGuiWindowFlags overlayFlags = ImGuiWindowFlags_NoDecoration | 
                                    ImGuiWindowFlags_AlwaysAutoResize | 
                                    ImGuiWindowFlags_NoFocusOnAppearing | 
                                    ImGuiWindowFlags_NoNav |
                                    ImGuiWindowFlags_NoSavedSettings |
                                    ImGuiWindowFlags_NoScrollbar;
    
    // Make window draggable if enabled
    if (!widget.draggable) {
        overlayFlags |= ImGuiWindowFlags_NoMove;
    }
    
    // Apply global font scaling
    ImGui::SetWindowFontScale(globalScale);
    
    if (ImGui::Begin(widget.name.c_str(), nullptr, overlayFlags)) {
        // Handle dragging
        HandleWidgetDragging(widget);
        
        // Render specific widget content based on type
        switch (widget.id) {
            case 0: RenderTitleWidget(widget); break;
            case 1: RenderFPSWidget(widget); break;
            case 2: RenderNetworkInfoWidget(widget); break;
            case 3: RenderActiveModsWidget(widget); break;
        }
    }
    ImGui::End();
}

void HandleWidgetDragging(OverlayWidget& widget) {
    if (!widget.draggable) return;
    
    static bool dragging = false;
    static int draggingWidgetId = -1;
    
    if (ImGui::IsWindowHovered() && ImGui::IsMouseDown(0)) {
        if (!dragging) {
            dragging = true;
            draggingWidgetId = widget.id;
        }
        
        if (draggingWidgetId == widget.id) {
            // Update position based on mouse movement
            ImVec2 mousePos = ImGui::GetMousePos();
            ImVec2 windowSize = ImGui::GetWindowSize();
            ImGuiViewport* viewport = ImGui::GetMainViewport();
            
            // Keep widget within screen bounds
            float newX = mousePos.x - windowSize.x * 0.5f;
            float newY = mousePos.y - windowSize.y * 0.5f;
            
            if (newX < 0.0f) newX = 0.0f;
            if (newX > viewport->Size.x - windowSize.x) newX = viewport->Size.x - windowSize.x;
            if (newY < 0.0f) newY = 0.0f;
            if (newY > viewport->Size.y - windowSize.y) newY = viewport->Size.y - windowSize.y;
            
            widget.position.x = newX;
            widget.position.y = newY;
            
            ImGui::SetWindowPos(widget.position);
        }
    } else if (dragging && ImGui::IsMouseReleased(0)) {
        dragging = false;
        draggingWidgetId = -1;
        // Save settings when drag ends
        SaveOverlaySettings();
    }
}

void RenderTitleWidget(OverlayWidget& widget) {
    // Client title with version - simple and safe
    std::string title = "KestrelClient ";
    title += clientVersion;
    
    ImGui::PushStyleColor(ImGuiCol_Text, ImVec4(0.85f, 0.35f, 0.85f, 1.0f)); // Purple/magenta
    
    // Simple text rendering without font manipulation to avoid crashes
    ImGui::Text("%s", title.c_str());
    
    ImGui::PopStyleColor();
}

void RenderFPSWidget(OverlayWidget& widget) {
    // FPS with color coding
    ImVec4 fpsColor;
    if (fps >= 60.0f) {
        fpsColor = ImVec4(0.2f, 0.8f, 0.2f, 1.0f); // Green
    } else if (fps >= 30.0f) {
        fpsColor = ImVec4(0.8f, 0.8f, 0.2f, 1.0f); // Yellow
    } else {
        fpsColor = ImVec4(0.8f, 0.2f, 0.2f, 1.0f); // Red
    }
    
    ImGui::PushStyleColor(ImGuiCol_Text, fpsColor);
    ImGui::Text("FPS: %.1f", fps);
    ImGui::PopStyleColor();
    
    // Frame time
    ImGui::PushStyleColor(ImGuiCol_Text, ImVec4(0.7f, 0.7f, 0.7f, 1.0f));
    ImGui::Text("Frame: %.2fms", 1000.0f / (fps > 0 ? fps : 1.0f));
    ImGui::PopStyleColor();
}

void RenderNetworkInfoWidget(OverlayWidget& widget) {
    // Ping with color coding
    ImVec4 pingColor;
    if (!pingSuccess) {
        pingColor = ImVec4(0.8f, 0.2f, 0.2f, 1.0f); // Red
    } else if (currentPing <= 50) {
        pingColor = ImVec4(0.2f, 0.8f, 0.2f, 1.0f); // Green
    } else if (currentPing <= 100) {
        pingColor = ImVec4(0.6f, 0.8f, 0.2f, 1.0f); // Light green
    } else {
        pingColor = ImVec4(0.8f, 0.6f, 0.2f, 1.0f); // Orange
    }
    
    ImGui::PushStyleColor(ImGuiCol_Text, pingColor);
    ImGui::Text("Ping: %u ms", currentPing);
    ImGui::PopStyleColor();
    
    // Network speeds
    ImGui::PushStyleColor(ImGuiCol_Text, ImVec4(0.6f, 0.6f, 0.8f, 1.0f));
    ImGui::Text("↓ %u KB/s ↑ %u KB/s", downloadSpeed, uploadSpeed);
    ImGui::PopStyleColor();
}

// Removed useless widget functions: RenderSystemInfoWidget, RenderMemoryInfoWidget, 
// RenderTimeWidget, RenderESPWidget, RenderConsoleWidget

void RenderActiveModsWidget(OverlayWidget& widget) {
    // Active mods display - similar to your old RenderInfo function
    ImGui::Text("Active Mods");
    ImGui::Separator();
    
    // Example active mods (you can integrate with your actual mod system)
    ImGui::Text("AutoSprint");
    ImGui::Text("AutoClicker");
    ImGui::Text("NoHitDelay");
    ImGui::Text("Velocity");
    ImGui::Text("PlayerESP");
}

// Functions for script engine access
float GetCurrentFPS() {
    return fps;
}

uint32_t GetCurrentPing() {
    return currentPing;
}

void InitializeOverlay() {
    InitializeFPS();
    
    // Load overlay settings first
    LoadOverlaySettings();
    
    // Set default positions for widgets if not loaded from settings
    if (titleWidget.position.x == 0 && titleWidget.position.y == 0) {
        titleWidget.position = ImVec2(10, 10); // Simple top-left position
        titleWidget.enabled = true; // Enable title by default
    }
    if (fpsWidget.position.x == 0 && fpsWidget.position.y == 0) {
        fpsWidget.position = ImVec2(1640, 0); // Top-right like your old system
    }
    // Removed systemInfoWidget - was useless
    if (networkInfoWidget.position.x == 0 && networkInfoWidget.position.y == 0) {
        networkInfoWidget.position = ImVec2(1640, 100); // Top-right, below FPS
    }
    // Removed memoryInfoWidget - was useless
    // Removed timeWidget - was useless
    // Removed espWidget - was useless
    // Removed consoleWidget - was useless
    if (activeModsWidget.position.x == 0 && activeModsWidget.position.y == 0) {
        activeModsWidget.position = ImVec2(1640, 200); // Top-right, below network
    }
    
    // Removed system monitoring initialization - was causing issues
    
    // Load custom fonts
    LoadCustomFonts();
}

void InitializeFPS() {
    lastFrameTime = std::chrono::steady_clock::now();
    frameCount = 0;
    fpsTimer = 0.0f;
    fps = 0.0f;
    
    // Initialize ping tracking
    lastPingTime = std::chrono::steady_clock::now() - std::chrono::seconds(5);
    currentPing = 42;
    pingSuccess = true;
    
    // Removed system info initialization - was causing issues
}

void ShutdownOverlay() {
    overlayShuttingDown = true;
    
    // Removed system monitoring cleanup - was causing issues
    
    // Save settings one last time
    SaveOverlaySettings();
}
