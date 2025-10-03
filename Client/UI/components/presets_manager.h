#pragma once
#include <string>
#include <map>
#include <vector>

// Presets management variables
extern char presetName[128];
extern std::map<std::string, std::map<std::string, bool>> presets;
extern std::string currentPreset;
extern std::map<std::string, bool> lastKnownStates;
extern std::string primaryPreset;
extern bool autoLoadPrimaryPreset;

// Function declarations
void RenderPresetsManager();
void SavePreset(const std::string& name);
void LoadPreset(const std::string& name);
void DeletePreset(const std::string& name);
std::string GetPresetsDirectory();
void SavePresetToDisk(const std::string& presetName, const std::map<std::string, bool>& states);
void LoadPresetFromDisk(const std::string& presetName, std::map<std::string, bool>& states);
std::vector<std::string> GetAvailablePresets();
void InitializeHackStates();
void SaveAutoClickerState();
void LoadAutoClickerState();
void SetPrimaryPreset(const std::string& name);
void LoadPrimaryPreset();
void LoadPrimaryPresetWhenReady();
void SaveSettingsToDisk();
void LoadSettingsFromDisk();
