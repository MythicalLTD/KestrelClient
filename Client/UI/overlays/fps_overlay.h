#pragma once
#include <chrono>
#include <cstdint>
#include <string>
#include <vector>

// Include ImGui for ImVec2
#include "../../include/imgui.h"

// Client version
extern const char* clientVersion;

// Widget structure for modular overlays
struct OverlayWidget {
    bool enabled;
    bool draggable;
    ImVec2 position;
    ImVec2 size;
    float opacity;
    std::string name;
    int id;
    
    OverlayWidget(const std::string& widgetName, int widgetId) 
        : enabled(false), draggable(true), position(ImVec2(0, 0)), 
          size(ImVec2(200, 100)), opacity(0.9f), name(widgetName), id(widgetId) {}
};

// Individual overlay widgets
extern OverlayWidget titleWidget;
extern OverlayWidget fpsWidget;
extern OverlayWidget networkInfoWidget;
extern OverlayWidget activeModsWidget;

// Global overlay settings
extern bool overlayEnabled;
extern float globalOpacity;
extern float globalScale;
extern bool useCustomFont;
extern bool overlayShuttingDown;

// FPS tracking variables
extern float fps;
extern std::chrono::steady_clock::time_point lastFrameTime;
extern int frameCount;
extern float fpsUpdateInterval;
extern float fpsTimer;

// Removed system monitoring variables - they were causing issues

// Network tracking variables
extern uint32_t currentPing;
extern bool pingSuccess;
extern std::chrono::steady_clock::time_point lastPingTime;
extern uint32_t downloadSpeed;
extern uint32_t uploadSpeed;

// Function declarations
void UpdateFPS();
void UpdatePing();
void RenderAllOverlays();
void RenderWidget(OverlayWidget& widget);
void RenderTitleWidget(OverlayWidget& widget);
void RenderFPSWidget(OverlayWidget& widget);
void RenderNetworkInfoWidget(OverlayWidget& widget);
void RenderActiveModsWidget(OverlayWidget& widget);
void InitializeOverlay();
void InitializeFPS();
float GetCurrentFPS();
uint32_t GetCurrentPing();
void LoadCustomFonts();
void SaveOverlaySettings();
void LoadOverlaySettings();
void HandleWidgetDragging(OverlayWidget& widget);
void ShutdownOverlay();
