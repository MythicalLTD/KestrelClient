#include "presets_manager.h"
#include "auto_clicker.h"
#include "hack_engine.h"
#include "../../include/imgui.h"
#include <string>
#include <map>
#include <vector>
#include <fstream>
#include <windows.h>
#include <shlobj.h>

// Presets management variables
char presetName[128] = "";
std::map<std::string, std::map<std::string, bool>> presets;
std::string currentPreset = "";
std::map<std::string, bool> lastKnownStates;
std::string primaryPreset = "";
bool autoLoadPrimaryPreset = true;

void InitializeHackStates() {
	// Initialize both maps with default values
	std::vector<std::string> modNames = {
		"BridgeHack", "NukeProcess", "PlayerESP", "Tracers", "BedESP", "Trajectories",
		"KillGame", "ForceOP", "ChestESP", "PlayerHealth", "TntTimer", "NoGUI",
		"NearPlayer", "FireballDetector", "BowDetector", "ResourceESP", 
		"ResourceGroundFinder", "BanMe"
	};
	
	for (const auto& modName : modNames) {
		lastKnownStates[modName] = false;
	}
}

// Preset management functions
std::string GetPresetsDirectory() {
	char appDataPath[MAX_PATH];
	if (SHGetFolderPathA(NULL, CSIDL_APPDATA, NULL, 0, appDataPath) == S_OK) {
		std::string kestrelDir = std::string(appDataPath) + "\\.kestrelclient";
		std::string presetsDir = kestrelDir + "\\presets";

		// Create .kestrelclient directory if it doesn't exist
		if (GetFileAttributesA(kestrelDir.c_str()) == INVALID_FILE_ATTRIBUTES) {
			CreateDirectoryA(kestrelDir.c_str(), NULL);
		}
		
		// Create presets subdirectory if it doesn't exist
		if (GetFileAttributesA(presetsDir.c_str()) == INVALID_FILE_ATTRIBUTES) {
			CreateDirectoryA(presetsDir.c_str(), NULL);
		}

		return presetsDir;
	}
	return ".";
}

void SavePresetToDisk(const std::string& presetName, const std::map<std::string, bool>& states) {
	std::string presetsDir = GetPresetsDirectory();
	std::string filePath = presetsDir + "\\" + presetName + ".json";
	
	// Debug: Print the file path
	printf("Saving preset to: %s\n", filePath.c_str());
	
	std::ofstream file(filePath);
	if (file.is_open()) {
		file << "{\n";
		file << "  \"name\": \"" << presetName << "\",\n";
		file << "  \"mods\": {\n";
		
		bool first = true;
		for (const auto& pair : states) {
			if (!first) file << ",\n";
			file << "    \"" << pair.first << "\": " << (pair.second ? "true" : "false");
			first = false;
		}
		
		file << "\n  }\n";
		file << "}\n";
		file.close();
		printf("Preset saved successfully: %s\n", presetName.c_str());
	} else {
		printf("ERROR: Failed to save preset to: %s\n", filePath.c_str());
	}
}

void LoadPresetFromDisk(const std::string& presetName, std::map<std::string, bool>& states) {
	std::string presetsDir = GetPresetsDirectory();
	std::string filePath = presetsDir + "\\" + presetName + ".json";
	
	printf("Loading preset from: %s\n", filePath.c_str());
	
	std::ifstream file(filePath);
	if (file.is_open()) {
		// Simple JSON parsing for our specific format
		std::string content((std::istreambuf_iterator<char>(file)), std::istreambuf_iterator<char>());
		file.close();
		
		printf("Preset file content: %s\n", content.c_str());
		
		// Reset states first
		InitializeHackStates();
		
		// Parse mod states (simple string matching approach)
		for (const auto& pair : lastKnownStates) {
			std::string searchPattern = "\"" + pair.first + "\": true";
			if (content.find(searchPattern) != std::string::npos) {
				states[pair.first] = true;
				printf("Loaded mod state: %s = true\n", pair.first.c_str());
			}
		}
		printf("Preset loaded successfully: %s\n", presetName.c_str());
	} else {
		printf("ERROR: Failed to load preset from: %s\n", filePath.c_str());
	}
}

std::vector<std::string> GetAvailablePresets() {
	std::vector<std::string> presetList;
	std::string presetsDir = GetPresetsDirectory();
	
	// Simple directory listing using Win32 API
	WIN32_FIND_DATAA findData;
	std::string searchPath = presetsDir + "\\*.json";
	HANDLE hFind = FindFirstFileA(searchPath.c_str(), &findData);
	
	if (hFind != INVALID_HANDLE_VALUE) {
		do {
			if (!(findData.dwFileAttributes & FILE_ATTRIBUTE_DIRECTORY)) {
				std::string filename = findData.cFileName;
				// Remove .json extension
				if (filename.length() > 5 && filename.substr(filename.length() - 5) == ".json") {
					filename = filename.substr(0, filename.length() - 5);
					presetList.push_back(filename);
				}
			}
		} while (FindNextFileA(hFind, &findData));
		FindClose(hFind);
	}
	
	return presetList;
}

void SavePreset(const std::string& name) {
	if (name.empty()) return;
	
	// Save current hack states as a preset (both in memory and to disk)
	presets[name] = lastKnownStates;
	currentPreset = name;
	SaveAutoClickerState(); // Save auto-clicker state to preset
	SavePresetToDisk(name, presets[name]);
}

void LoadPreset(const std::string& name) {
	if (name.empty()) return;
	
	printf("Loading preset: %s\n", name.c_str());
	
	// Load preset from disk
	LoadPresetFromDisk(name, lastKnownStates);
	
	// Apply the loaded states by toggling mods as needed
	currentPreset = name;
	
	// Enable/disable mods based on preset
	int modsToggled = 0;
	for (const auto& mod : lastKnownStates) {
		const std::string& modName = mod.first;
		bool shouldBeEnabled = mod.second;
		
		// Only toggle if the current state is different from desired state
		if (hackStates[modName] != shouldBeEnabled) {
			printf("Toggling mod: %s to %s\n", modName.c_str(), shouldBeEnabled ? "ON" : "OFF");
			ToggleHackMod(modName);
			modsToggled++;
		}
	}
	
	printf("Preset '%s' loaded - %d mods toggled\n", name.c_str(), modsToggled);
	
	LoadAutoClickerState(); // Load auto-clicker state from preset
}

void DeletePreset(const std::string& name) {
	if (name.empty()) return;
	
	printf("Deleting preset: %s\n", name.c_str());
	
	// Delete from memory
	presets.erase(name);
	printf("Removed from memory\n");
	
	// Delete from disk
	std::string presetsDir = GetPresetsDirectory();
	std::string filePath = presetsDir + "\\" + name + ".json";
	printf("Deleting file: %s\n", filePath.c_str());
	
	if (DeleteFileA(filePath.c_str())) {
		printf("File deleted successfully\n");
	} else {
		DWORD error = GetLastError();
		printf("Failed to delete file. Error code: %d\n", error);
	}
	
	if (currentPreset == name) {
		currentPreset = "";
		printf("Cleared current preset\n");
	}
	
	// If the deleted preset was the primary preset, clear it
	if (primaryPreset == name) {
		primaryPreset = "";
		SaveSettingsToDisk(); // Save the cleared primary preset
		printf("Cleared primary preset\n");
	}
	
	printf("Preset deletion completed\n");
}

void RenderPresetsManager() {
	ImGui::Text("Presets Management");
	ImGui::Separator();
	
	// Current preset
	if (!currentPreset.empty()) {
		ImGui::Text("Current Preset: %s", currentPreset.c_str());
	} else {
		ImGui::Text("No preset loaded");
	}
	ImGui::Separator();
	
	// Save preset section
	ImGui::Text("Save Current State as Preset:");
	ImGui::InputText("##PresetName", presetName, sizeof(presetName));
	ImGui::SameLine();
	if (ImGui::Button("Save")) {
		if (strlen(presetName) > 0) {
			SavePreset(std::string(presetName));
			memset(presetName, 0, sizeof(presetName));
		}
	}
	
	ImGui::Separator();
	
	// Load presets from disk (refresh every frame to show changes)
	auto diskPresets = GetAvailablePresets();
	for (const auto& presetName : diskPresets) {
		// Only load if not already in memory
		if (presets.find(presetName) == presets.end()) {
			std::map<std::string, bool> tempStates;
			LoadPresetFromDisk(presetName, tempStates);
			presets[presetName] = tempStates;
		}
	}
	
	// Remove presets that no longer exist on disk
	auto it = presets.begin();
	while (it != presets.end()) {
		if (std::find(diskPresets.begin(), diskPresets.end(), it->first) == diskPresets.end()) {
			it = presets.erase(it);
		} else {
			++it;
		}
	}
	
	// Presets list
	ImGui::Text("Saved Presets (%zu presets):", presets.size());
	if (presets.empty()) {
		ImGui::Text("No presets saved yet");
	} else {
		ImGui::BeginChild("PresetsList", ImVec2(0, 200), true);
		for (const auto& preset : presets) {
			const std::string& presetName = preset.first;
			ImGui::PushID(presetName.c_str());
			
			// Count active hacks in preset
			int activeCount = 0;
			for (const auto& hack : preset.second) {
				if (hack.second) activeCount++;
			}
			
			ImGui::Text("📁 %s (%d active)", presetName.c_str(), activeCount);
			ImGui::SameLine();
			if (ImGui::Button("Load")) {
				LoadPreset(presetName);
			}
			ImGui::SameLine();
			if (ImGui::Button("Set Primary")) {
				SetPrimaryPreset(presetName);
			}
			ImGui::SameLine();
			if (ImGui::Button("Delete")) {
				DeletePreset(presetName);
			}
			
			ImGui::PopID();
		}
		ImGui::EndChild();
	}
	
	// Primary preset settings
	ImGui::Separator();
	ImGui::Text("Primary Preset Settings:");
	ImGui::Checkbox("Auto-load primary preset on startup", &autoLoadPrimaryPreset);
	
	if (autoLoadPrimaryPreset) {
		ImGui::Indent();
		ImGui::Text("Primary Preset: %s", primaryPreset.empty() ? "None" : primaryPreset.c_str());
		
		if (ImGui::Button("Set as Primary")) {
			if (!currentPreset.empty()) {
				SetPrimaryPreset(currentPreset);
			}
		}
		ImGui::SameLine();
		if (ImGui::Button("Clear Primary")) {
			SetPrimaryPreset("");
		}
		ImGui::Unindent();
	}
}

// Save auto-clicker state to current preset
void SaveAutoClickerState() {
	if (currentPreset.empty()) return;
	
	// Save auto-clicker settings
	presets[currentPreset]["autoClickerEnabled"] = autoClickerEnabled;
	presets[currentPreset]["leftClickEnabled"] = leftClickEnabled;
	presets[currentPreset]["rightClickEnabled"] = rightClickEnabled;
	presets[currentPreset]["keybindEnabled"] = keybindEnabled;
	
	// Save keybind key (convert to bool for storage)
	presets[currentPreset]["autoClickerToggleKey"] = static_cast<bool>(autoClickerToggleKey);
	// Store the actual key code in a separate entry
	presets[currentPreset]["autoClickerToggleKeyCode"] = static_cast<bool>(autoClickerToggleKey);
	
	// Save click multiplier (convert to bool for storage)
	presets[currentPreset]["clickMultiplier2"] = (clickMultiplier == 2);
	presets[currentPreset]["clickMultiplier3"] = (clickMultiplier == 3);
	presets[currentPreset]["clickMultiplier4"] = (clickMultiplier == 4);
	presets[currentPreset]["clickMultiplier5"] = (clickMultiplier == 5);
}

// Load auto-clicker state from current preset
void LoadAutoClickerState() {
	if (currentPreset.empty()) return;
	
	auto& preset = presets[currentPreset];
	
	// Load auto-clicker settings
	if (preset.find("autoClickerEnabled") != preset.end()) {
		autoClickerEnabled = preset["autoClickerEnabled"];
	}
	if (preset.find("leftClickEnabled") != preset.end()) {
		leftClickEnabled = preset["leftClickEnabled"];
	}
	if (preset.find("rightClickEnabled") != preset.end()) {
		rightClickEnabled = preset["rightClickEnabled"];
	}
	if (preset.find("keybindEnabled") != preset.end()) {
		keybindEnabled = preset["keybindEnabled"];
	}
	
	// Load click multiplier
	clickMultiplier = 2; // default
	if (preset.find("clickMultiplier3") != preset.end() && preset["clickMultiplier3"]) {
		clickMultiplier = 3;
	}
	else if (preset.find("clickMultiplier4") != preset.end() && preset["clickMultiplier4"]) {
		clickMultiplier = 4;
	}
	else if (preset.find("clickMultiplier5") != preset.end() && preset["clickMultiplier5"]) {
		clickMultiplier = 5;
	}
}

// Set primary preset
void SetPrimaryPreset(const std::string& name) {
	primaryPreset = name;
	SaveSettingsToDisk();
}

// Load primary preset on startup
void LoadPrimaryPreset() {
	if (autoLoadPrimaryPreset && !primaryPreset.empty()) {
		printf("Loading primary preset: %s\n", primaryPreset.c_str());
		
		// Try to load from memory first
		if (presets.find(primaryPreset) != presets.end()) {
			printf("Primary preset found in memory\n");
			LoadPreset(primaryPreset);
		} else {
			printf("Primary preset not in memory, loading from disk\n");
			// Load from disk if not in memory
			std::map<std::string, bool> tempStates;
			LoadPresetFromDisk(primaryPreset, tempStates);
			if (!tempStates.empty()) {
				presets[primaryPreset] = tempStates;
				LoadPreset(primaryPreset);
			} else {
				printf("Failed to load primary preset from disk\n");
			}
		}
	} else {
		printf("No primary preset to load (autoLoad: %s, preset: '%s')\n", 
			autoLoadPrimaryPreset ? "true" : "false", primaryPreset.c_str());
	}
}

// Load primary preset when hack engine is ready
void LoadPrimaryPresetWhenReady() {
	static bool primaryPresetLoaded = false;
	static int retryCount = 0;
	const int maxRetries = 10; // Try for 10 frames
	
	if (!primaryPresetLoaded && autoLoadPrimaryPreset && !primaryPreset.empty()) {
		if (hackEngineConnected) {
			// Hack engine is connected, load the preset
			LoadPrimaryPreset();
			primaryPresetLoaded = true;
			printf("Primary preset '%s' loaded successfully!\n", primaryPreset.c_str());
		} else if (retryCount < maxRetries) {
			// Hack engine not connected yet, retry next frame
			retryCount++;
			printf("Waiting for hack engine connection... (attempt %d/%d)\n", retryCount, maxRetries);
		} else {
			// Give up after max retries
			printf("Failed to load primary preset '%s' - hack engine not connected after %d attempts\n", primaryPreset.c_str(), maxRetries);
			primaryPresetLoaded = true; // Don't keep trying
		}
	}
}

// Save settings to disk
void SaveSettingsToDisk() {
	std::string settingsPath = GetPresetsDirectory() + "\\settings.txt";
	std::ofstream file(settingsPath);
	if (file.is_open()) {
		file << "primaryPreset=" << primaryPreset << std::endl;
		file << "autoLoadPrimaryPreset=" << (autoLoadPrimaryPreset ? "true" : "false") << std::endl;
		file.close();
	}
}

// Load settings from disk
void LoadSettingsFromDisk() {
	std::string settingsPath = GetPresetsDirectory() + "\\settings.txt";
	std::ifstream file(settingsPath);
	if (file.is_open()) {
		std::string line;
		while (std::getline(file, line)) {
			size_t pos = line.find('=');
			if (pos != std::string::npos) {
				std::string key = line.substr(0, pos);
				std::string value = line.substr(pos + 1);
				
				if (key == "primaryPreset") {
					primaryPreset = value;
				}
				else if (key == "autoLoadPrimaryPreset") {
					autoLoadPrimaryPreset = (value == "true");
				}
			}
		}
		file.close();
	}
}
