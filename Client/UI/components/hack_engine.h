#pragma once
#include <string>
#include <map>

// Hack Engine variables
extern bool showHackEngine;
extern bool hackEngineConnected;
extern std::string hackEngineStatus;
extern std::map<std::string, bool> hackStates;
extern int apiPort;

// Function declarations
void RenderHackEngine();
void ToggleHackMod(const std::string& modName);
void CheckHackEngineConnection();
void RefreshAllModStates();
void DisableAllMods();
