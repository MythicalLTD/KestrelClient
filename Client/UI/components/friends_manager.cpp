#include "friends_manager.h"
#include "../../include/imgui.h"
#include <string>
#include <vector>
#include <algorithm>
#include <windows.h>
#include <wininet.h>

#pragma comment(lib, "wininet.lib")

// Friends management variables
std::vector<std::string> friendsList;
char newFriendName[128] = "";
std::string friendsStatus = "";

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
	
	if (HttpSendRequestA(hRequest, headers, -1, (LPVOID)jsonData.c_str(), static_cast<DWORD>(jsonData.length()))) {
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
	
	if (HttpSendRequestA(hRequest, headers, -1, (LPVOID)jsonData.c_str(), static_cast<DWORD>(jsonData.length()))) {
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
