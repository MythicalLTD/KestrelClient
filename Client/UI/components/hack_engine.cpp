#include "hack_engine.h"
#include "friends_manager.h"
#include "presets_manager.h"
#include "auto_clicker.h"
#include "notifications.h"
#include "../../include/imgui.h"
#include <string>
#include <map>
#include <windows.h>
#include <wininet.h>

#pragma comment(lib, "wininet.lib")

// Hack Engine variables
bool showHackEngine = false;
bool hackEngineConnected = false;
std::string hackEngineStatus = "Disconnected";
std::map<std::string, bool> hackStates;
int apiPort = 9865;

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
	
	if (HttpSendRequestA(hRequest, headers, -1, (LPVOID)jsonData.c_str(), static_cast<DWORD>(jsonData.length()))) {
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
				
				// Show notification
				if (hackStates[modName]) {
					ShowSuccessNotification(modName + " enabled successfully!");
				} else {
					ShowInfoNotification(modName + " disabled successfully!");
				}
			} else {
				hackEngineStatus = "FAILED: " + std::string(buffer);
				ShowAlertNotification("Failed to toggle " + modName + ": " + std::string(buffer));
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
			ShowSuccessNotification("Connected to MythicalClient API successfully!");
		} else {
			hackEngineConnected = true;
			hackEngineStatus = "SUCCESS: Connected to MythicalClient API on port " + std::to_string(apiPort) + " (no response body)";
			ShowSuccessNotification("Connected to MythicalClient API successfully!");
		}
		
		// After successful connection, refresh all mod states
		RefreshAllModStates();
	} else {
		hackEngineConnected = false;
		DWORD error = GetLastError();
		hackEngineStatus = "ERROR: Connection test failed with code: " + std::to_string(error);
		ShowWarningNotification("Failed to connect to MythicalClient API");
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
