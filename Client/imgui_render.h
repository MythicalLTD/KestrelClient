#pragma once
void RenderImGui();
void ShowExitConfirmationDialog();
bool ShouldExit();
void SetExitConfirmationVisible(bool visible);
void ResetExitState();
void InitializeFPS();
void RenderNetworkOverlay();
float GetCurrentFPS();
uint32_t GetCurrentPing();
void DisableAllMods();