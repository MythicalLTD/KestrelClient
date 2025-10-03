#pragma once
#include <chrono>
#include <vector>
#include <random>

// Auto-clicker variables
extern bool autoClickerEnabled;
extern int clickMultiplier;
extern bool leftClickEnabled;
extern bool rightClickEnabled;
extern std::mt19937 clickRng;

// Keybind variables
extern int autoClickerToggleKey;
extern bool keybindEnabled;
extern bool isListeningForKey;

// Function declarations
void RenderAutoClicker();
void HandleAutoClicker();
void ToggleAutoClicker();
bool IsAutoClickerEnabled();
void CheckAutoClickerKeybind();
void StartListeningForKey();
std::string GetKeyName(int keyCode);
