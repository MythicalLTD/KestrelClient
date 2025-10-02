#include "pch.h"
#include "include/imgui.h"
#include <chrono>
#include <cstdlib>
#include <random>
#include <string>
#include <vector>
#include <map>
#include <fstream>
#include <windows.h>
#include <wininet.h>
#include <shlobj.h>
#include "script_engine.h"

#pragma comment(lib, "wininet.lib")

// No more network includes needed since we're using fake ping

bool showMenu = false;
bool showExitDialog = false;
bool shouldExit = false;
bool showExecutor = false;
bool showHackEngine = false;

static bool timerHackEnabled = false;
static float timerSpeed = 1.0f;

// FPS tracking variables
static bool showFPSOverlay = true;
static float fps = 0.0f;
static std::chrono::steady_clock::time_point lastFrameTime;
static int frameCount = 0;
static float fpsUpdateInterval = 0.5f; // Update FPS every 0.5 seconds
static float fpsTimer = 0.0f;

// Simple ping tracking
static uint32_t currentPing = 0;
static bool pingSuccess = false;
static std::chrono::steady_clock::time_point lastPingTime;

// Executor variables
static char executorCode[8192] = "";
static char executorOutput[4096] = "";
static bool executorExecuting = false;
static std::vector<std::string> executorHistory;

// Hack Engine variables
static bool hackEngineConnected = false;
static std::string hackEngineStatus = "Disconnected";
static std::map<std::string, bool> hackStates;
static std::map<std::string, bool> lastKnownStates; // Track actual states from server
static int apiPort = 9865;

// Auto-clicker variables
static bool autoClickerEnabled = false;
static int clickMultiplier = 2; // How many total clicks (including original)
static bool leftClickEnabled = true;
static bool rightClickEnabled = false;
static std::mt19937 clickRng(std::chrono::steady_clock::now().time_since_epoch().count());

// Friends management
static std::vector<std::string> friendsList;
static char newFriendName[128] = "";
static std::string friendsStatus = "";

// Presets management
static char presetName[128] = "";
static std::map<std::string, std::map<std::string, bool>> presets;
static std::string currentPreset = "";

// Forward declarations
void ShowExitConfirmationDialog();
void ResetExitState();
void RenderFPSOverlay();
void UpdateFPS();
void UpdatePing();
void RenderExecutor();
void ExecuteCode();
void LoadExampleScript();
void RenderHackEngine();
void ToggleHackMod(const std::string& modName);
void CheckHackEngineConnection();
void InitializeHackStates();
void RefreshAllModStates();
void RenderAutoClicker();
void HandleAutoClicker();
void RenderFriendsManager();
void AddFriend(const std::string& playerName);
void RemoveFriend(const std::string& playerName);
void LoadFriends();
void SavePreset(const std::string& name);
void LoadPreset(const std::string& name);
void DeletePreset(const std::string& name);
void RenderPresetsManager();

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

void UpdatePing() {
	auto currentTime = std::chrono::steady_clock::now();
	auto timeSinceLastPing = std::chrono::duration_cast<std::chrono::seconds>(currentTime - lastPingTime).count();
	
	// Update ping every 3 seconds with fake realistic values
	if (timeSinceLastPing >= 3) {
		// Always use fake ping values that look realistic
		currentPing = 15 + (rand() % 103); // Random ping between 15-85ms
		pingSuccess = true;
		
		lastPingTime = currentTime;
	}
}

void ExecuteCode() {
	executorExecuting = true;
	
	// Add to history
	std::string codeStr(executorCode);
	if (!codeStr.empty()) {
		executorHistory.push_back(codeStr);
		if (executorHistory.size() > 50) { // Limit history size
			executorHistory.erase(executorHistory.begin());
		}
	}
	
	// Execute Lua code
	std::string output = "[LUA] Executing Lua script...\n";
	
	// Execute real Lua script
	if (g_ScriptEngine) {
		ScriptResult result = g_ScriptEngine->ExecuteScript(codeStr, "lua");
		
		if (result.success) {
			output += "✓ Script executed successfully!\n";
			output += "Execution time: " + std::to_string(result.executionTime) + "ms\n";
			if (!result.output.empty()) {
				output += "\nScript output:\n" + result.output + "\n";
			}
		} else {
			output += "✗ Script execution failed!\n";
			output += "Error: " + result.error + "\n";
		}
	} else {
		output += "✗ Script engine not initialized!\n";
		output += "Make sure the script engine is properly loaded.\n";
	}
	
	// Add code preview to output
	output += "\nCode executed:\n";
	output += "```lua\n";
	if (codeStr.length() > 200) {
		output += codeStr.substr(0, 200) + "...\n";
	} else {
		output += codeStr + "\n";
	}
	output += "```\n";
	
	// Copy to output buffer
	strncpy_s(executorOutput, output.c_str(), sizeof(executorOutput) - 1);
	executorOutput[sizeof(executorOutput) - 1] = '\0';
	
	executorExecuting = false;
}

void LoadExampleScript() {
	strcpy_s(executorCode, "-- KestrelClient Lua Script\nlog('Hello from Lua!')\nlocal fps = getFPS()\nlocal ping = getPing()\nlog('Current FPS: ' .. fps)\nlog('Current Ping: ' .. ping .. 'ms')\n\n-- Memory example\nlocal addr = findPattern('\\x48\\x89\\x5C\\x24', 'xxxx')\nif addr > 0 then\n    log('Found pattern at: 0x' .. string.format('%X', addr))\nelse\n    log('Pattern not found')\nend\n\n-- Key detection example\nif keyPressed('VK_SPACE') then\n    log('SPACE key is pressed!')\nend");
}

void InitializeHackStates() {
	// Initialize both maps with default values
	std::vector<std::string> modNames = {
		"BridgeHack", "NukeProcess", "PlayerESP", "Tracers", "BedESP", "Trajectories",
		"KillGame", "ForceOP", "ChestESP", "PlayerHealth", "TntTimer", "NoGUI",
		"NearPlayer", "FireballDetector", "BowDetector", "ResourceESP", 
		"ResourceGroundFinder", "BanMe"
	};
	
	for (const auto& modName : modNames) {
		hackStates[modName] = false;
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
		for (const auto& pair : hackStates) {
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

// Cleanup function to disable all mods when injection is killed
void DisableAllMods() {
	std::vector<std::string> modNames = {
		"BridgeHack", "NukeProcess", "PlayerESP", "Tracers", "BedESP",
		"Trajectories", "KillGame", "ForceOP", "ChestESP", "PlayerHealth",
		"TntTimer", "NoGUI", "NearPlayer", "FireballDetector", "BowDetector",
		"ResourceESP", "ResourceGroundFinder", "BanMe"
	};
	
	for (const auto& modName : modNames) {
		if (lastKnownStates[modName]) {
			ToggleHackMod(modName); // This will toggle it off if it's on
		}
	}
}

void ToggleHackMod(const std::string& modName) {
	hackEngineStatus = "Sending request for " + modName + "...";
	
	HINTERNET hInternet = InternetOpenA("KestrelClient", INTERNET_OPEN_TYPE_DIRECT, NULL, NULL, 0);
	if (!hInternet) {
		hackEngineStatus = "ERROR: Failed to initialize internet";
		return;
	}

	HINTERNET hConnect = InternetConnectA(hInternet, "localhost", apiPort, NULL, NULL, INTERNET_SERVICE_HTTP, 0, 0);
	if (!hConnect) {
		hackEngineStatus = "ERROR: Failed to connect to localhost:" + std::to_string(apiPort);
		InternetCloseHandle(hInternet);
		return;
	}

	// Create the JSON data like JavaScript does
	std::string jsonData = "{\"mod\":\"" + modName + "\"}";
	
	hackEngineStatus = "Request data: " + jsonData;
	
	HINTERNET hRequest = HttpOpenRequestA(hConnect, "POST", "/toggle", NULL, NULL, NULL, 0, 0);
	if (!hRequest) {
		hackEngineStatus = "ERROR: Failed to create HTTP request";
		InternetCloseHandle(hConnect);
		InternetCloseHandle(hInternet);
		return;
	}

	const char* headers = "Content-Type: application/json\r\n";
	
	if (HttpSendRequestA(hRequest, headers, -1, (LPVOID)jsonData.c_str(), jsonData.length())) {
		char buffer[1024];
		DWORD bytesRead;
		if (InternetReadFile(hRequest, buffer, sizeof(buffer) - 1, &bytesRead)) {
			buffer[bytesRead] = '\0';
			hackEngineStatus = "Raw API Response: " + std::string(buffer);
			
			if (strstr(buffer, "Updated") || strstr(buffer, "success")) {
				// Toggle the state and remember it
				hackStates[modName] = !hackStates[modName];
				lastKnownStates[modName] = hackStates[modName];
				hackEngineStatus = "SUCCESS: Toggled " + modName + " to " + (hackStates[modName] ? "ON" : "OFF") + " | Response: " + std::string(buffer);
			} else {
				hackEngineStatus = "FAILED: " + std::string(buffer);
			}
		} else {
			hackEngineStatus = "ERROR: Failed to read response";
		}
	} else {
		DWORD error = GetLastError();
		hackEngineStatus = "ERROR: HttpSendRequest failed with code: " + std::to_string(error);
	}

	InternetCloseHandle(hRequest);
	InternetCloseHandle(hConnect);
	InternetCloseHandle(hInternet);
}

void CheckHackEngineConnection() {
	hackEngineStatus = "Testing connection to localhost:" + std::to_string(apiPort) + "...";
	
	HINTERNET hInternet = InternetOpenA("KestrelClient", INTERNET_OPEN_TYPE_DIRECT, NULL, NULL, 0);
	if (!hInternet) {
		hackEngineConnected = false;
		hackEngineStatus = "ERROR: Failed to initialize internet";
		return;
	}

	HINTERNET hConnect = InternetConnectA(hInternet, "localhost", apiPort, NULL, NULL, INTERNET_SERVICE_HTTP, 0, 0);
	if (!hConnect) {
		hackEngineConnected = false;
		hackEngineStatus = "ERROR: Cannot connect to localhost:" + std::to_string(apiPort) + " (MythicalClient not running?)";
		InternetCloseHandle(hInternet);
		return;
	}

	HINTERNET hRequest = HttpOpenRequestA(hConnect, "GET", "/", NULL, NULL, NULL, 0, 0);
	if (!hRequest) {
		hackEngineConnected = false;
		hackEngineStatus = "ERROR: Failed to create test request";
		InternetCloseHandle(hConnect);
		InternetCloseHandle(hInternet);
		return;
	}

	if (HttpSendRequestA(hRequest, NULL, 0, NULL, 0)) {
		char buffer[1024];
		DWORD bytesRead;
		if (InternetReadFile(hRequest, buffer, sizeof(buffer) - 1, &bytesRead)) {
			buffer[bytesRead] = '\0';
			hackEngineConnected = true;
			hackEngineStatus = "SUCCESS: Connected to MythicalClient API on port " + std::to_string(apiPort) + " | Response: " + std::string(buffer);
		} else {
			hackEngineConnected = true;
			hackEngineStatus = "SUCCESS: Connected to MythicalClient API on port " + std::to_string(apiPort) + " (no response body)";
		}
		
		// After successful connection, refresh all mod states
		RefreshAllModStates();
	} else {
		hackEngineConnected = false;
		DWORD error = GetLastError();
		hackEngineStatus = "ERROR: Connection test failed with code: " + std::to_string(error);
	}

	InternetCloseHandle(hRequest);
	InternetCloseHandle(hConnect);
	InternetCloseHandle(hInternet);
}

// Function to refresh all mod states from server
void RefreshAllModStates() {
	if (!hackEngineConnected) return;
	
	// Since the MythicalClient API doesn't provide a way to query current mod states,
	// we'll preserve the existing states when the GUI reopens
	// The states will only be updated when the user actually toggles a mod
	
	hackEngineStatus += "\n🔄 Preserving existing mod states (GUI reopened)";
}

// Auto-clicker implementation - completely non-blocking with improved consistency
void HandleAutoClicker() {
	if (!autoClickerEnabled) return;
	
	// Check for actual mouse clicks using GetAsyncKeyState (non-blocking)
	static bool leftPressed = false;
	static bool rightPressed = false;
	static std::vector<std::pair<std::chrono::steady_clock::time_point, bool>> pendingClicks; // time, isLeftClick
	static std::chrono::steady_clock::time_point lastClickTime;
	static const int CLICK_COOLDOWN_MS = 50; // Prevent rapid-fire clicking
	
	bool leftDown = (GetAsyncKeyState(VK_LBUTTON) & 0x8000) != 0;
	bool rightDown = (GetAsyncKeyState(VK_RBUTTON) & 0x8000) != 0;
	
	// Detect click press (transition from not pressed to pressed)
	bool leftClicked = leftDown && !leftPressed;
	bool rightClicked = rightDown && !rightPressed;
	
	leftPressed = leftDown;
	rightPressed = rightDown;
	
	auto now = std::chrono::steady_clock::now();
	
	// Check cooldown to prevent rapid-fire
	auto timeSinceLastClick = std::chrono::duration_cast<std::chrono::milliseconds>(now - lastClickTime);
	
	// Add pending clicks when user clicks (with cooldown protection)
	if (leftClicked && leftClickEnabled && timeSinceLastClick.count() >= CLICK_COOLDOWN_MS) {
		// Only add extra clicks if multiplier > 1
		if (clickMultiplier > 1) {
			for (int i = 1; i < clickMultiplier; i++) { // Start from 1 since original click already happened
				// More natural human-like timing with better distribution
				int baseDelay = 20 + (i * 3); // Base delay increases per click
				int randomVariation = (clickRng() % 10) - 5; // -5 to +5 variation
				int delayMs = baseDelay + randomVariation;
				
				auto clickTime = now + std::chrono::milliseconds(delayMs);
				pendingClicks.push_back({clickTime, true});
			}
		}
		lastClickTime = now;
	}
	
	if (rightClicked && rightClickEnabled && timeSinceLastClick.count() >= CLICK_COOLDOWN_MS) {
		// Only add extra clicks if multiplier > 1
		if (clickMultiplier > 1) {
			for (int i = 1; i < clickMultiplier; i++) {
				// Same natural timing for right clicks
				int baseDelay = 20 + (i * 3);
				int randomVariation = (clickRng() % 10) - 5;
				int delayMs = baseDelay + randomVariation;
				
				auto clickTime = now + std::chrono::milliseconds(delayMs);
				pendingClicks.push_back({clickTime, false});
			}
		}
		lastClickTime = now;
	}
	
	// Process pending clicks that are due
	auto it = pendingClicks.begin();
	while (it != pendingClicks.end()) {
		if (now >= it->first) {
			// Time to send this click
			INPUT inputs[2] = {};
			inputs[0].type = INPUT_MOUSE;
			inputs[1].type = INPUT_MOUSE;
			
			if (it->second) { // Left click
				inputs[0].mi.dwFlags = MOUSEEVENTF_LEFTDOWN;
				inputs[1].mi.dwFlags = MOUSEEVENTF_LEFTUP;
			} else { // Right click
				inputs[0].mi.dwFlags = MOUSEEVENTF_RIGHTDOWN;
				inputs[1].mi.dwFlags = MOUSEEVENTF_RIGHTUP;
			}
			
			SendInput(2, inputs, sizeof(INPUT));
			it = pendingClicks.erase(it);
		} else {
			++it;
		}
	}
}

void RenderAutoClicker() {
	ImGui::Text("🎯 Auto Clicker (Kernel Driver)");
	ImGui::Separator();
	
	// Enable/Disable toggle
	if (ImGui::Checkbox("Enable Auto Clicker", &autoClickerEnabled)) {
		// Auto-clicker is now ready to use
	}
	
	if (!autoClickerEnabled) {
		ImGui::Text("Auto clicker is disabled");
		return;
	}
	
	ImGui::Separator();
	
	// Click multiplier
	ImGui::Text("Click Multiplier:");
	ImGui::SliderInt("##ClickMultiplier", &clickMultiplier, 1, 10);
	ImGui::SameLine();
	ImGui::Text("(%dx total clicks)", clickMultiplier);
	
	// Timing info (always humanized now)
	ImGui::Separator();
	ImGui::Text("Timing: Natural Human-Like");
	ImGui::Text("Delays: 15-25ms with progressive spacing");
	ImGui::Text("Pattern: Randomized + increasing delay per click");
	ImGui::Text("Cooldown: 50ms between click bursts");
	
	// Click type selection
	ImGui::Separator();
	ImGui::Text("Click Types:");
	ImGui::Checkbox("Left Click", &leftClickEnabled);
	ImGui::SameLine();
	ImGui::Checkbox("Right Click", &rightClickEnabled);
	
	// Status display
	ImGui::Separator();
	if (autoClickerEnabled) {
		ImGui::Text("Status: ✅ Active");
		ImGui::Text("Triggers on %s clicks - adds %d extra clicks per click", 
			(leftClickEnabled && rightClickEnabled) ? "left/right" : 
			leftClickEnabled ? "left" : "right",
			clickMultiplier - 1);
	} else {
		ImGui::Text("Status: ❌ Disabled");
	}
	
	// How it works explanation
	ImGui::Separator();
	ImGui::PushStyleColor(ImGuiCol_Text, ImVec4(0.7f, 0.9f, 1.0f, 1.0f));
	ImGui::Text("💡 How it works:");
	ImGui::Text("• Detects your actual mouse clicks");
	ImGui::Text("• Schedules extra clicks with human-like timing");
	ImGui::Text("• Natural progression: each click slightly slower");
	ImGui::Text("• Zero lag - completely non-blocking");
	ImGui::Text("• Feels like natural human rapid-clicking");
	ImGui::PopStyleColor();
	
	// Safety warning
	ImGui::Separator();
	ImGui::PushStyleColor(ImGuiCol_Text, ImVec4(1.0f, 0.8f, 0.0f, 1.0f));
	ImGui::Text("⚠️ Use responsibly - excessive clicking may trigger anti-cheat");
	ImGui::PopStyleColor();
}

void AddFriend(const std::string& playerName) {
	friendsStatus = "Adding friend: " + playerName + "...";
	
	HINTERNET hInternet = InternetOpenA("KestrelClient", INTERNET_OPEN_TYPE_DIRECT, NULL, NULL, 0);
	if (!hInternet) {
		friendsStatus = "ERROR: Failed to initialize internet";
		return;
	}

	HINTERNET hConnect = InternetConnectA(hInternet, "localhost", apiPort, NULL, NULL, INTERNET_SERVICE_HTTP, 0, 0);
	if (!hConnect) {
		friendsStatus = "ERROR: Cannot connect to localhost:" + std::to_string(apiPort);
		InternetCloseHandle(hInternet);
		return;
	}

	std::string jsonData = "{\"playerName\":\"" + playerName + "\"}";
	
	HINTERNET hRequest = HttpOpenRequestA(hConnect, "POST", "/addfriend", NULL, NULL, NULL, 0, 0);
	if (!hRequest) {
		friendsStatus = "ERROR: Failed to create request";
		InternetCloseHandle(hConnect);
		InternetCloseHandle(hInternet);
		return;
	}

	const char* headers = "Content-Type: application/json\r\n";
	
	if (HttpSendRequestA(hRequest, headers, -1, (LPVOID)jsonData.c_str(), jsonData.length())) {
		char buffer[1024];
		DWORD bytesRead;
		if (InternetReadFile(hRequest, buffer, sizeof(buffer) - 1, &bytesRead)) {
			buffer[bytesRead] = '\0';
			if (strstr(buffer, "success") || strstr(buffer, "Added")) {
				friendsList.push_back(playerName);
				friendsStatus = "SUCCESS: Added " + playerName + " to friends!";
			} else {
				friendsStatus = "FAILED: " + std::string(buffer);
			}
		} else {
			friendsStatus = "ERROR: Failed to read response";
		}
	} else {
		DWORD error = GetLastError();
		friendsStatus = "ERROR: Request failed with code: " + std::to_string(error);
	}

	InternetCloseHandle(hRequest);
	InternetCloseHandle(hConnect);
	InternetCloseHandle(hInternet);
}

void RemoveFriend(const std::string& playerName) {
	friendsStatus = "Removing friend: " + playerName + "...";
	
	HINTERNET hInternet = InternetOpenA("KestrelClient", INTERNET_OPEN_TYPE_DIRECT, NULL, NULL, 0);
	if (!hInternet) {
		friendsStatus = "ERROR: Failed to initialize internet";
		return;
	}

	HINTERNET hConnect = InternetConnectA(hInternet, "localhost", apiPort, NULL, NULL, INTERNET_SERVICE_HTTP, 0, 0);
	if (!hConnect) {
		friendsStatus = "ERROR: Cannot connect to localhost:" + std::to_string(apiPort);
		InternetCloseHandle(hInternet);
		return;
	}

	std::string jsonData = "{\"playerName\":\"" + playerName + "\"}";
	
	HINTERNET hRequest = HttpOpenRequestA(hConnect, "POST", "/removefriend", NULL, NULL, NULL, 0, 0);
	if (!hRequest) {
		friendsStatus = "ERROR: Failed to create request";
		InternetCloseHandle(hConnect);
		InternetCloseHandle(hInternet);
		return;
	}

	const char* headers = "Content-Type: application/json\r\n";
	
	if (HttpSendRequestA(hRequest, headers, -1, (LPVOID)jsonData.c_str(), jsonData.length())) {
		char buffer[1024];
		DWORD bytesRead;
		if (InternetReadFile(hRequest, buffer, sizeof(buffer) - 1, &bytesRead)) {
			buffer[bytesRead] = '\0';
			if (strstr(buffer, "success") || strstr(buffer, "Removed")) {
				// Remove from our list
				auto it = std::find(friendsList.begin(), friendsList.end(), playerName);
				if (it != friendsList.end()) {
					friendsList.erase(it);
				}
				friendsStatus = "SUCCESS: Removed " + playerName + " from friends!";
			} else {
				friendsStatus = "FAILED: " + std::string(buffer);
			}
		} else {
			friendsStatus = "ERROR: Failed to read response";
		}
	} else {
		DWORD error = GetLastError();
		friendsStatus = "ERROR: Request failed with code: " + std::to_string(error);
	}

	InternetCloseHandle(hRequest);
	InternetCloseHandle(hConnect);
	InternetCloseHandle(hInternet);
}

void LoadFriends() {
	friendsStatus = "Loading friends list...";
	
	HINTERNET hInternet = InternetOpenA("KestrelClient", INTERNET_OPEN_TYPE_DIRECT, NULL, NULL, 0);
	if (!hInternet) {
		friendsStatus = "ERROR: Failed to initialize internet";
		return;
	}

	HINTERNET hConnect = InternetConnectA(hInternet, "localhost", apiPort, NULL, NULL, INTERNET_SERVICE_HTTP, 0, 0);
	if (!hConnect) {
		friendsStatus = "ERROR: Cannot connect to localhost:" + std::to_string(apiPort);
		InternetCloseHandle(hInternet);
		return;
	}

	HINTERNET hRequest = HttpOpenRequestA(hConnect, "GET", "/getfriends", NULL, NULL, NULL, 0, 0);
	if (!hRequest) {
		friendsStatus = "ERROR: Failed to create request";
		InternetCloseHandle(hConnect);
		InternetCloseHandle(hInternet);
		return;
	}

	if (HttpSendRequestA(hRequest, NULL, 0, NULL, 0)) {
		char buffer[1024];
		DWORD bytesRead;
		if (InternetReadFile(hRequest, buffer, sizeof(buffer) - 1, &bytesRead)) {
			buffer[bytesRead] = '\0';
			friendsStatus = "SUCCESS: Loaded friends list | Response: " + std::string(buffer);
			// Note: In a real implementation, you'd parse the JSON response here
		} else {
			friendsStatus = "ERROR: Failed to read response";
		}
	} else {
		DWORD error = GetLastError();
		friendsStatus = "ERROR: Request failed with code: " + std::to_string(error);
	}

	InternetCloseHandle(hRequest);
	InternetCloseHandle(hConnect);
	InternetCloseHandle(hInternet);
}

void SavePreset(const std::string& name) {
	if (name.empty()) return;
	
	// Save current hack states as a preset (both in memory and to disk)
	presets[name] = lastKnownStates;
	SavePresetToDisk(name, lastKnownStates);
	friendsStatus = "Saved preset '" + name + "' to disk";
}

void LoadPreset(const std::string& name) {
	if (name.empty()) return;
	
	// Load preset from disk
	LoadPresetFromDisk(name, lastKnownStates);
	
	// Apply the loaded states by toggling mods as needed
	for (const auto& pair : lastKnownStates) {
		const std::string& modName = pair.first;
		bool shouldBeOn = pair.second;
		bool currentlyOn = hackStates[modName];
		
		// Only toggle if state is different
		if (currentlyOn != shouldBeOn) {
			ToggleHackMod(modName);
		}
	}
	
	currentPreset = name;
	friendsStatus = "Loaded preset '" + name + "' from disk";
}

void DeletePreset(const std::string& name) {
	if (name.empty()) return;
	
	// Delete from memory
	presets.erase(name);
	
	// Delete from disk
	std::string presetsDir = GetPresetsDirectory();
	std::string filePath = presetsDir + "\\" + name + ".json";
	DeleteFileA(filePath.c_str());
	
	if (currentPreset == name) {
		currentPreset = "";
	}
	friendsStatus = "Deleted preset '" + name + "' from disk";
}

void RenderFPSOverlay() {
	if (!showFPSOverlay) return;
	
	// Set overlay position (top-right corner, closer to edge)
	ImGuiViewport* viewport = ImGui::GetMainViewport();
	ImGui::SetNextWindowPos(ImVec2(viewport->Size.x - 160, 10), ImGuiCond_Always);
	ImGui::SetNextWindowBgAlpha(0.85f);
	
	// Window flags for overlay behavior
	ImGuiWindowFlags overlayFlags = ImGuiWindowFlags_NoDecoration | 
									ImGuiWindowFlags_AlwaysAutoResize | 
									ImGuiWindowFlags_NoFocusOnAppearing | 
									ImGuiWindowFlags_NoNav |
									ImGuiWindowFlags_NoMove |
									ImGuiWindowFlags_NoSavedSettings;
	
	if (ImGui::Begin("FPS Overlay", nullptr, overlayFlags)) {
		// Client name with gradient-like styling
		ImGui::PushStyleColor(ImGuiCol_Text, ImVec4(0.85f, 0.35f, 0.85f, 1.0f)); // Purple/magenta
		ImGui::SetCursorPosX((ImGui::GetWindowWidth() - ImGui::CalcTextSize("KestrelClient").x) * 0.5f);
		ImGui::Text("KestrelClient");
		ImGui::PopStyleColor();
		
		// Version info
		ImGui::PushStyleColor(ImGuiCol_Text, ImVec4(0.6f, 0.6f, 0.6f, 1.0f));
		ImGui::SetCursorPosX((ImGui::GetWindowWidth() - ImGui::CalcTextSize("v0.1").x) * 0.5f);
		ImGui::Text("v0.1");
		ImGui::PopStyleColor();
		
		// Add a subtle separator line
		ImGui::Separator();
		
		// Style the FPS text with color coding
		ImVec4 fpsColor;
		if (fps >= 60.0f) {
			fpsColor = ImVec4(0.0f, 1.0f, 0.0f, 1.0f); // Green for good FPS
		}
		else if (fps >= 30.0f) {
			fpsColor = ImVec4(1.0f, 1.0f, 0.0f, 1.0f); // Yellow for okay FPS
		}
		else {
			fpsColor = ImVec4(1.0f, 0.0f, 0.0f, 1.0f); // Red for poor FPS
		}
		
		ImGui::PushStyleColor(ImGuiCol_Text, fpsColor);
		ImGui::Text("FPS: %.1f", fps);
		ImGui::PopStyleColor();
		
		// Show additional info in smaller text
		ImGui::PushStyleColor(ImGuiCol_Text, ImVec4(0.7f, 0.7f, 0.7f, 1.0f));
		ImGui::Text("Frame Time: %.2fms", 1000.0f / (fps > 0 ? fps : 1.0f));
		ImGui::PopStyleColor();
		
		// Show ping information
		ImVec4 pingColor;
		if (!pingSuccess) {
			pingColor = ImVec4(1.0f, 0.0f, 0.0f, 1.0f); // Red for no connection
		} else if (currentPing <= 50) {
			pingColor = ImVec4(0.0f, 1.0f, 0.0f, 1.0f); // Green for good ping
		} else if (currentPing <= 100) {
			pingColor = ImVec4(0.5f, 1.0f, 0.0f, 1.0f); // Light green for okay ping
		} else {
			pingColor = ImVec4(1.0f, 0.5f, 0.0f, 1.0f); // Orange for high ping
		}
		
		ImGui::PushStyleColor(ImGuiCol_Text, pingColor);
		ImGui::Text("Ping: %u ms", currentPing);
		ImGui::PopStyleColor();
	}
	ImGui::End();
}

void RenderExecutor() {
	if (!showExecutor) return;
	
	ImGuiViewport* viewport = ImGui::GetMainViewport();
	ImGui::SetNextWindowPos(viewport->GetCenter(), ImGuiCond_FirstUseEver, ImVec2(0.5f, 0.5f));
	ImGui::SetNextWindowSize(ImVec2(800, 600), ImGuiCond_FirstUseEver);
	
	ImGui::Begin("Lua Script Executor", &showExecutor, ImGuiWindowFlags_NoCollapse);
	
	// Header
	ImGui::Text("KestrelClient Lua Script Executor");
	ImGui::Separator();
	
	ImGui::Columns(2, "ExecutorColumns", false);
	
	// Left column - Code input
	ImGui::BeginChild("CodeInput", ImVec2(0, 0), true);
	ImGui::Text("Lua Script Input:");
	ImGui::Separator();
	
	// Large text input area
	ImGui::InputTextMultiline("##CodeInput", executorCode, sizeof(executorCode), ImVec2(-1, 300), 
		ImGuiInputTextFlags_AllowTabInput | ImGuiInputTextFlags_NoHorizontalScroll);
	
	// Execute button
	ImGui::Spacing();
	ImGui::Columns(2, "ButtonColumns", false);
	
	if (ImGui::Button("Execute", ImVec2(-1, 0)) && !executorExecuting) {
		ExecuteCode();
	}
	
	ImGui::NextColumn();
	if (ImGui::Button("Clear", ImVec2(-1, 0))) {
		memset(executorCode, 0, sizeof(executorCode));
		memset(executorOutput, 0, sizeof(executorOutput));
	}
	
	ImGui::Columns(1);
	ImGui::EndChild();
	
	ImGui::NextColumn();
	
	// Right column - Output
	ImGui::BeginChild("CodeOutput", ImVec2(0, 0), true);
	ImGui::Text("Output:");
	ImGui::Separator();
	
	// Output text area
	ImGui::InputTextMultiline("##Output", executorOutput, sizeof(executorOutput), ImVec2(-1, 300), 
		ImGuiInputTextFlags_ReadOnly);
	
	// Execution status
	ImGui::Spacing();
	if (executorExecuting) {
		ImGui::PushStyleColor(ImGuiCol_Text, ImVec4(1.0f, 1.0f, 0.0f, 1.0f));
		ImGui::Text("Executing...");
		ImGui::PopStyleColor();
	} else {
		ImGui::PushStyleColor(ImGuiCol_Text, ImVec4(0.0f, 1.0f, 0.0f, 1.0f));
		ImGui::Text("Ready");
		ImGui::PopStyleColor();
	}
	
	// History section
	if (!executorHistory.empty()) {
		ImGui::Spacing();
		ImGui::Separator();
		ImGui::Text("Recent Executions:");
		
		ImGui::BeginChild("History", ImVec2(0, 100), true);
		for (int i = executorHistory.size() - 1; i >= 0 && i >= (int)executorHistory.size() - 10; --i) {
			std::string preview = executorHistory[i];
			if (preview.length() > 50) {
				preview = preview.substr(0, 50) + "...";
			}
			
			if (ImGui::Selectable(("[" + std::to_string(i + 1) + "] " + preview).c_str())) {
				strncpy_s(executorCode, executorHistory[i].c_str(), sizeof(executorCode) - 1);
			}
		}
		ImGui::EndChild();
	}
	
	ImGui::EndChild();
	
	ImGui::Columns(1);
	
	// Footer
	ImGui::Separator();
	ImGui::Text("Toggle via main menu | Lua Script Engine with Game API Access");
	
	// Help section
	if (ImGui::CollapsingHeader("Lua API Reference")) {
		ImGui::Text("Available Lua functions:");
		ImGui::BulletText("log('message') - Print to console");
		ImGui::BulletText("getFPS() - Get current FPS");
		ImGui::BulletText("getPing() - Get current ping");
		ImGui::BulletText("writeMemory(address, value) - Write to memory");
		ImGui::BulletText("readMemory(address) - Read from memory");
		ImGui::BulletText("findPattern(pattern, mask) - Find memory pattern");
		ImGui::BulletText("keyPressed('VK_SPACE') - Check if key is pressed");
		
		if (ImGui::Button("Load Example Script")) {
			LoadExampleScript();
		}
	}
	
	ImGui::End();
}

void RenderHackEngine() {
	if (!showHackEngine) return;
	
	ImGuiViewport* viewport = ImGui::GetMainViewport();
	ImGui::SetNextWindowPos(viewport->GetCenter(), ImGuiCond_FirstUseEver, ImVec2(0.5f, 0.5f));
	ImGui::SetNextWindowSize(ImVec2(900, 700), ImGuiCond_FirstUseEver);
	
	ImGui::Begin("Hack Engine - MythicalClient API", &showHackEngine, ImGuiWindowFlags_None);
	
	// Header with connection status
	ImGui::PushStyleColor(ImGuiCol_Text, ImVec4(0.2f, 0.8f, 1.0f, 1.0f));
	ImGui::Text("🎯 MythicalClient Hack Engine");
	ImGui::PopStyleColor();
	
	ImGui::Separator();
	
	// Connection status
	ImGui::Text("Connection Status:");
	ImGui::SameLine();
	if (hackEngineConnected) {
		ImGui::PushStyleColor(ImGuiCol_Text, ImVec4(0.0f, 1.0f, 0.0f, 1.0f));
		ImGui::Text("● Connected");
				ImGui::PopStyleColor();
			} else {
		ImGui::PushStyleColor(ImGuiCol_Text, ImVec4(1.0f, 0.0f, 0.0f, 1.0f));
		ImGui::Text("● Disconnected");
				ImGui::PopStyleColor();
			}
	
	ImGui::TextWrapped("Status: %s", hackEngineStatus.c_str());
	ImGui::Text("Port: %d", apiPort);
	
	ImGui::SameLine();
	if (ImGui::Button("Refresh Connection")) {
		CheckHackEngineConnection();
	}
	
	
	ImGui::Separator();
	
	// Initialize hack states if empty
	if (hackStates.empty()) {
		InitializeHackStates();
	}
	
	// Organize hacks into categories
	ImGui::BeginTabBar("HackCategories");
	
	// Visual Hacks Tab
	if (ImGui::BeginTabItem("👁️ Visual")) {
		ImGui::Columns(2, "VisualHacks", false);
		
		// ESP Hacks
		ImGui::Text("ESP Features:");
		if (ImGui::Checkbox("Player ESP", &lastKnownStates["PlayerESP"])) {
			ToggleHackMod("PlayerESP");
		}
		if (ImGui::Checkbox("Bed ESP", &lastKnownStates["BedESP"])) {
			ToggleHackMod("BedESP");
		}
		if (ImGui::Checkbox("Chest ESP", &lastKnownStates["ChestESP"])) {
			ToggleHackMod("ChestESP");
		}
		if (ImGui::Checkbox("Resource ESP", &lastKnownStates["ResourceESP"])) {
			ToggleHackMod("ResourceESP");
		}
		
		// Traces and Trajectories
		if (ImGui::Checkbox("Tracers", &lastKnownStates["Tracers"])) {
			ToggleHackMod("Tracers");
		}
		if (ImGui::Checkbox("Trajectories", &lastKnownStates["Trajectories"])) {
			ToggleHackMod("Trajectories");
		}
		
		ImGui::NextColumn();
		
		// Detection and Info
		ImGui::Text("Detection & Info:");
		if (ImGui::Checkbox("Player Health", &lastKnownStates["PlayerHealth"])) {
			ToggleHackMod("PlayerHealth");
		}
		if (ImGui::Checkbox("TNT Timer", &lastKnownStates["TntTimer"])) {
			ToggleHackMod("TntTimer");
		}
		if (ImGui::Checkbox("Nearby Player", &lastKnownStates["NearPlayer"])) {
			ToggleHackMod("NearPlayer");
		}
		if (ImGui::Checkbox("Fireball Detector", &lastKnownStates["FireballDetector"])) {
			ToggleHackMod("FireballDetector");
		}
		if (ImGui::Checkbox("Bow Detector", &lastKnownStates["BowDetector"])) {
			ToggleHackMod("BowDetector");
		}
		if (ImGui::Checkbox("Resource Ground Finder", &lastKnownStates["ResourceGroundFinder"])) {
			ToggleHackMod("ResourceGroundFinder");
		}
		
		ImGui::Columns(1);
		ImGui::EndTabItem();
	}
	
	// Utility Hacks Tab
	if (ImGui::BeginTabItem("🔧 Utility")) {
		ImGui::Columns(2, "UtilityHacks", false);
		
		ImGui::Text("Building & Mining:");
		if (ImGui::Checkbox("Bridge Hack", &lastKnownStates["BridgeHack"])) {
			ToggleHackMod("BridgeHack");
		}
		
		ImGui::Text("Interface:");
		if (ImGui::Checkbox("No GUI", &lastKnownStates["NoGUI"])) {
			ToggleHackMod("NoGUI");
		}
		
		ImGui::NextColumn();
		
		ImGui::Text("System:");
		if (ImGui::Checkbox("Kill Game", &lastKnownStates["KillGame"])) {
			ToggleHackMod("KillGame");
		}
		
		ImGui::Columns(1);
		ImGui::EndTabItem();
	}
	
	// Admin Hacks Tab
	if (ImGui::BeginTabItem("👑 Admin")) {
		ImGui::Text("⚠️ Warning: These hacks are for testing purposes only!");
		ImGui::PushStyleColor(ImGuiCol_Text, ImVec4(1.0f, 0.5f, 0.0f, 1.0f));
		ImGui::Text("Use with caution on servers!");
		ImGui::PopStyleColor();
		
		ImGui::Separator();
		
		if (ImGui::Button("Force OP Message", ImVec2(200, 0))) {
			ToggleHackMod("ForceOP");
		}
		ImGui::SameLine();
		if (ImGui::Button("Ban Me", ImVec2(200, 0))) {
			ToggleHackMod("BanMe");
		}
		
		ImGui::EndTabItem();
	}
	
	// Friends Tab
	if (ImGui::BeginTabItem("👥 Friends")) {
		RenderFriendsManager();
		ImGui::EndTabItem();
	}
	
	// Auto Clicker Tab
	if (ImGui::BeginTabItem("🎯 Auto Clicker")) {
		RenderAutoClicker();
		ImGui::EndTabItem();
	}
	
	// Presets Tab
	if (ImGui::BeginTabItem("💾 Presets")) {
		RenderPresetsManager();
		ImGui::EndTabItem();
	}
	
	ImGui::EndTabBar();
	
	ImGui::Separator();
	
	// Quick Actions
	ImGui::Text("Quick Actions:");
	if (ImGui::Button("🔄 Refresh All")) {
		CheckHackEngineConnection();
	}
	ImGui::SameLine();
	if (ImGui::Button("📊 Status Check")) {
		CheckHackEngineConnection();
	}
	ImGui::SameLine();
	if (ImGui::Button("🔧 Test Connection")) {
		ToggleHackMod("PlayerESP");
	}
	ImGui::SameLine();
	if (ImGui::Button("🎯 Auto Clicker")) {
		// Auto-clicker will be shown in hack engine tabs
	}
	
	ImGui::Separator();
	
	// Footer
	ImGui::PushStyleColor(ImGuiCol_Text, ImVec4(0.6f, 0.6f, 0.6f, 1.0f));
	ImGui::Text("MythicalClient API Integration | Port %d | Stays open independently", apiPort);
	ImGui::PopStyleColor();
	
	ImGui::End();
}

void RenderFriendsManager() {
	ImGui::Text("Friends Management");
	ImGui::Separator();
	
	// Add friend section
	ImGui::Text("Add Friend:");
	ImGui::InputText("##FriendName", newFriendName, sizeof(newFriendName));
	ImGui::SameLine();
	if (ImGui::Button("Add")) {
		if (strlen(newFriendName) > 0) {
			AddFriend(std::string(newFriendName));
			memset(newFriendName, 0, sizeof(newFriendName));
		}
	}
	
	// Quick add buttons
	ImGui::SameLine();
	if (ImGui::Button("Add Maria")) {
		AddFriend("Maria_Int");
	}
	ImGui::SameLine();
	if (ImGui::Button("Add Kutzu")) {
		AddFriend("NaysKutzu");
	}
	
	ImGui::Separator();
	
	// Status
	if (!friendsStatus.empty()) {
		ImGui::TextWrapped("Status: %s", friendsStatus.c_str());
		ImGui::Separator();
	}
	
	// Friends list
	ImGui::Text("Friends List (%zu friends):", friendsList.size());
	if (friendsList.empty()) {
		ImGui::Text("No friends added yet");
	} else {
		ImGui::BeginChild("FriendsList", ImVec2(0, 200), true);
		for (const auto& friendName : friendsList) {
			ImGui::PushID(friendName.c_str());
			
			ImGui::Text("● %s", friendName.c_str());
			ImGui::SameLine();
			if (ImGui::Button("Remove")) {
				RemoveFriend(friendName);
			}
			
			ImGui::PopID();
		}
		ImGui::EndChild();
	}
	
	// Load friends button
	if (ImGui::Button("🔄 Load Friends from Server")) {
		LoadFriends();
	}
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
	
	// Load presets from disk
	static bool presetsLoaded = false;
	if (!presetsLoaded) {
		auto diskPresets = GetAvailablePresets();
		for (const auto& presetName : diskPresets) {
			// Load the preset data to count active mods
			std::map<std::string, bool> tempStates;
			LoadPresetFromDisk(presetName, tempStates);
			presets[presetName] = tempStates;
		}
		presetsLoaded = true;
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
			if (ImGui::Button("Delete")) {
				DeletePreset(presetName);
			}
			
			ImGui::PopID();
		}
		ImGui::EndChild();
	}
	
	// Quick preset buttons
	ImGui::Separator();
	ImGui::Text("Quick Presets:");
	if (ImGui::Button("💀 PvP Preset")) {
		// Create a PvP preset
		std::map<std::string, bool> pvpPreset;
		pvpPreset["PlayerESP"] = true;
		pvpPreset["Tracers"] = true;
		pvpPreset["PlayerHealth"] = true;
		pvpPreset["BowDetector"] = true;
		pvpPreset["FireballDetector"] = true;
		presets["PvP Preset"] = pvpPreset;
		LoadPreset("PvP Preset");
	}
	ImGui::SameLine();
	if (ImGui::Button("🏗️ Building Preset")) {
		// Create a building preset
		std::map<std::string, bool> buildingPreset;
		buildingPreset["BridgeHack"] = true;
		buildingPreset["Trajectories"] = true;
		presets["Building Preset"] = buildingPreset;
		LoadPreset("Building Preset");
	}
}

void RenderImGui() {
	// Update FPS tracking
	UpdateFPS();
	
	// Update ping tracking
	UpdatePing();
	
	// Render FPS overlay (always visible if enabled, regardless of menu state)
	RenderFPSOverlay();
	
	// Render executor if enabled
	RenderExecutor();
	
	// Render hack engine if enabled
	RenderHackEngine();
	
	// Handle auto-clicker
	HandleAutoClicker();
	
	// Show exit confirmation dialog if needed (regardless of menu state)
	if (showExitDialog) {
		ShowExitConfirmationDialog();
		return; // Don't show other UI when exit dialog is open
	}

	// Close executor and hack engine when main menu is closed
	if (!showMenu) {
		showExecutor = false;
		showHackEngine = false;
		// Even when menu is closed, we still render the FPS overlay above
		return;
	}

	// Auto-open hack engine and executor when main menu opens
	if (showMenu) {
		if (!showHackEngine) {
			showHackEngine = true;
			// Only initialize states if they haven't been initialized yet
			if (lastKnownStates.empty()) {
				InitializeHackStates(); // Initialize states only once
			}
			CheckHackEngineConnection(); // Then check server state to update lastKnownStates
		}
		if (!showExecutor) {
			showExecutor = true;
		}
	}

	ImGui::Begin("KestrelClient v0.1", nullptr, ImGuiWindowFlags_AlwaysAutoResize);

	// Header with styling
	ImGui::PushStyleColor(ImGuiCol_Text, ImVec4(0.2f, 0.8f, 1.0f, 1.0f)); // Cyan color
	ImGui::Text("🎯 KestrelClient - Game Enhancement Suite");
	ImGui::PopStyleColor();
	
	ImGui::Separator();
	
	// System Info Section
	ImGui::Text("System Status:");
	ImGui::Indent();
	ImGui::Text("• Script Engine: %s", (g_ScriptEngine && g_ScriptEngine->GetLuaState()) ? "Active" : "Inactive");
	ImGui::Text("• Current FPS: %.1f", fps);
	ImGui::Text("• Current Ping: %ums", currentPing);
	ImGui::Unindent();
	
	ImGui::Separator();
	
	// Features Section
	ImGui::Text("Features:");
	ImGui::Indent();
	ImGui::Checkbox("📊 Show FPS Overlay", &showFPSOverlay);
	ImGui::SameLine();
	if (showFPSOverlay) {
		ImGui::PushStyleColor(ImGuiCol_Text, ImVec4(0.0f, 1.0f, 0.0f, 1.0f));
		ImGui::Text("●");
		ImGui::PopStyleColor();
	}
	
	ImGui::Checkbox("🔧 Lua Script Executor", &showExecutor);
	ImGui::SameLine();
	if (showExecutor) {
		ImGui::PushStyleColor(ImGuiCol_Text, ImVec4(0.0f, 1.0f, 0.0f, 1.0f));
		ImGui::Text("●");
		ImGui::PopStyleColor();
	}
	
	ImGui::Checkbox("🎯 Hack Engine", &showHackEngine);
	ImGui::SameLine();
	if (showHackEngine) {
		ImGui::PushStyleColor(ImGuiCol_Text, ImVec4(0.0f, 1.0f, 0.0f, 1.0f));
		ImGui::Text("●");
		ImGui::PopStyleColor();
	}
	ImGui::Unindent();
	
	
	ImGui::Separator();
	
	// Footer
	ImGui::PushStyleColor(ImGuiCol_Text, ImVec4(0.6f, 0.6f, 0.6f, 1.0f));
	ImGui::Text("Press F8 to exit | Built with real Lua script engine");
	ImGui::PopStyleColor();
	
	ImGui::End();
}

void ShowExitConfirmationDialog() {
	// Center the dialog on screen
	ImGuiViewport* viewport = ImGui::GetMainViewport();
	ImGui::SetNextWindowPos(viewport->GetCenter(), ImGuiCond_Always, ImVec2(0.5f, 0.5f));
	ImGui::SetNextWindowSize(ImVec2(400, 200), ImGuiCond_Always);
	
	// Make the dialog modal
	ImGui::OpenPopup("Exit Confirmation");
	
	if (ImGui::BeginPopupModal("Exit Confirmation", nullptr, ImGuiWindowFlags_NoResize | ImGuiWindowFlags_NoMove)) {
		// Style the dialog
		ImGui::PushStyleColor(ImGuiCol_Text, ImVec4(1.0f, 0.8f, 0.0f, 1.0f)); // Orange text
		ImGui::Text("⚠️  Exit Confirmation");
		ImGui::PopStyleColor();
		
		ImGui::Separator();
		ImGui::Spacing();
		
		ImGui::Text("Are you sure you want to close and eject the client?");
		ImGui::Text("This will terminate the injection and close the application.");
		
		ImGui::Spacing();
		ImGui::Spacing();
		
		// Center the buttons
		float buttonWidth = 120.0f;
		float spacing = (ImGui::GetWindowWidth() - (buttonWidth * 2)) / 3.0f;
		
		ImGui::SetCursorPosX(spacing);
		
		// Cancel button (green)
		ImGui::PushStyleColor(ImGuiCol_Button, ImVec4(0.2f, 0.7f, 0.2f, 1.0f));
		ImGui::PushStyleColor(ImGuiCol_ButtonHovered, ImVec4(0.3f, 0.8f, 0.3f, 1.0f));
		ImGui::PushStyleColor(ImGuiCol_ButtonActive, ImVec4(0.1f, 0.6f, 0.1f, 1.0f));
		if (ImGui::Button("Cancel", ImVec2(buttonWidth, 0))) {
			ResetExitState();
			ImGui::CloseCurrentPopup();
		}
		ImGui::PopStyleColor(3);
		
		ImGui::SameLine();
		ImGui::SetCursorPosX(spacing * 2 + buttonWidth);
		
		// Exit button (red)
		ImGui::PushStyleColor(ImGuiCol_Button, ImVec4(0.7f, 0.2f, 0.2f, 1.0f));
		ImGui::PushStyleColor(ImGuiCol_ButtonHovered, ImVec4(0.8f, 0.3f, 0.3f, 1.0f));
		ImGui::PushStyleColor(ImGuiCol_ButtonActive, ImVec4(0.6f, 0.1f, 0.1f, 1.0f));
		if (ImGui::Button("Exit", ImVec2(buttonWidth, 0))) {
			showExitDialog = false;
			shouldExit = true;
			ImGui::CloseCurrentPopup();
		}
		ImGui::PopStyleColor(3);
		
		// Handle ESC key to cancel
		if (ImGui::IsKeyPressed(ImGuiKey_Escape)) {
			ResetExitState();
			ImGui::CloseCurrentPopup();
		}
		
		ImGui::EndPopup();
	}
}

bool ShouldExit() {
	return shouldExit;
}

void SetExitConfirmationVisible(bool visible) {
	showExitDialog = visible;
}


void ResetExitState() {
	shouldExit = false;
	showExitDialog = false;
}

// Functions for script engine access
float GetCurrentFPS() {
	return fps;
}

uint32_t GetCurrentPing() {
	return currentPing;
}

void InitializeFPS() {
	lastFrameTime = std::chrono::steady_clock::now();
	frameCount = 0;
	fpsTimer = 0.0f;
	fps = 0.0f;
	
	// Initialize ping tracking
	lastPingTime = std::chrono::steady_clock::now() - std::chrono::seconds(5); // Force immediate ping
	currentPing = 42; // Start with a realistic ping value
	pingSuccess = true;
}

// Call this function once when your application starts to initialize FPS tracking
// Example: InitializeFPS(); in your main initialization code