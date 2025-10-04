#pragma once
#include <chrono>
#include <cstdint>

// Client version
extern const char* clientVersion;

// FPS tracking variables
extern bool showFPSOverlay;
extern float fps;
extern std::chrono::steady_clock::time_point lastFrameTime;
extern int frameCount;
extern float fpsUpdateInterval;
extern float fpsTimer;

// Ping tracking variables
extern uint32_t currentPing;
extern bool pingSuccess;
extern std::chrono::steady_clock::time_point lastPingTime;

// Function declarations
void UpdateFPS();
void UpdatePing();
void RenderFPSOverlay();
void InitializeFPS();
float GetCurrentFPS();
uint32_t GetCurrentPing();
