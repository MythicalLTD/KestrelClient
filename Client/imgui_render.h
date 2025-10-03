#pragma once
#include <cstdint>

// Main UI functions
void RenderImGui();
void InitializeUI();

// Console functions
void ToggleConsole();
bool IsConsoleEnabled();

// Legacy function declarations for backwards compatibility
void ShowExitConfirmationDialog();
bool ShouldExit();
void SetExitConfirmationVisible(bool visible);
void ResetExitState();
void InitializeFPS();
void RenderNetworkOverlay();
float GetCurrentFPS();
uint32_t GetCurrentPing();
void DisableAllMods();