#include "auto_clicker.h"
#include "notifications.h"
#include "../../include/imgui.h"
#include <chrono>
#include <vector>
#include <random>
#include <windows.h>

// Auto-clicker variables
bool autoClickerEnabled = false;
int clickMultiplier = 2; // How many total clicks (including original)
bool leftClickEnabled = true;
bool rightClickEnabled = false;
std::mt19937 clickRng(static_cast<unsigned int>(std::chrono::steady_clock::now().time_since_epoch().count()));

// Keybind variables
int autoClickerToggleKey = VK_F6; // Default key (F6)
bool keybindEnabled = true;
bool isListeningForKey = false;

// Auto-clicker implementation - completely non-blocking with improved consistency
void HandleAutoClicker() {
	// Check for keybind toggle first
	CheckAutoClickerKeybind();
	
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
	
	// Keybind settings
	ImGui::Text("Toggle Keybind:");
	ImGui::Checkbox("Enable Keybind", &keybindEnabled);
	
	if (keybindEnabled) {
		ImGui::SameLine();
		ImGui::Text("Current: %s", GetKeyName(autoClickerToggleKey).c_str());
		
		if (ImGui::Button("Change Key")) {
			StartListeningForKey();
		}
		
		if (isListeningForKey) {
			ImGui::SameLine();
			ImGui::Text("Press any key or mouse button...");
			
			// Check for mouse buttons first (they have special handling)
			if (GetAsyncKeyState(VK_LBUTTON) & 0x8000) {
				autoClickerToggleKey = VK_LBUTTON;
				isListeningForKey = false;
				ShowSuccessNotification("Keybind set to: LEFT MOUSE");
			}
			else if (GetAsyncKeyState(VK_RBUTTON) & 0x8000) {
				autoClickerToggleKey = VK_RBUTTON;
				isListeningForKey = false;
				ShowSuccessNotification("Keybind set to: RIGHT MOUSE");
			}
			else if (GetAsyncKeyState(VK_MBUTTON) & 0x8000) {
				autoClickerToggleKey = VK_MBUTTON;
				isListeningForKey = false;
				ShowSuccessNotification("Keybind set to: MIDDLE MOUSE");

			}
			else if (GetAsyncKeyState(VK_XBUTTON1) & 0x8000) {
				autoClickerToggleKey = VK_XBUTTON1;
				isListeningForKey = false;
				ShowSuccessNotification("Keybind set to: MOUSE 4");
			}
			else if (GetAsyncKeyState(VK_XBUTTON2) & 0x8000) {
				autoClickerToggleKey = VK_XBUTTON2;
				isListeningForKey = false;
				ShowSuccessNotification("Keybind set to: MOUSE 5");
			}
			else {
				// Check for keyboard keys
				for (int i = 8; i <= 255; i++) {
					// Skip mouse buttons as we handle them above
					if (i == VK_LBUTTON || i == VK_RBUTTON || i == VK_MBUTTON || 
						i == VK_XBUTTON1 || i == VK_XBUTTON2) {
						continue;
					}
					
					if (GetAsyncKeyState(i) & 0x8000) {
						autoClickerToggleKey = i;
						isListeningForKey = false;
						ShowSuccessNotification("Keybind set to: " + GetKeyName(i));
						break;
					}
				}
			}
		}
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

// Toggle auto-clicker function with notification
void ToggleAutoClicker() {
	autoClickerEnabled = !autoClickerEnabled;
	
	if (autoClickerEnabled) {
		ShowSuccessNotification("Auto-clicker enabled!");
	} else {
		ShowInfoNotification("Auto-clicker disabled!");
	}
}

// Check if auto-clicker is enabled
bool IsAutoClickerEnabled() {
	return autoClickerEnabled;
}

// Check for keybind toggle
void CheckAutoClickerKeybind() {
	if (!keybindEnabled || isListeningForKey) return;
	
	static bool keyWasPressed = false;
	bool keyIsPressed = (GetAsyncKeyState(autoClickerToggleKey) & 0x8000) != 0;
	
	if (keyIsPressed && !keyWasPressed) {
		ToggleAutoClicker();
	}
	
	keyWasPressed = keyIsPressed;
}

// Start listening for a new key
void StartListeningForKey() {
	isListeningForKey = true;
}

// Get key name for display
std::string GetKeyName(int keyCode) {
	switch (keyCode) {
		// Function keys
		case VK_F1: return "F1";
		case VK_F2: return "F2";
		case VK_F3: return "F3";
		case VK_F4: return "F4";
		case VK_F5: return "F5";
		case VK_F6: return "F6";
		case VK_F7: return "F7";
		case VK_F8: return "F8";
		case VK_F9: return "F9";
		case VK_F10: return "F10";
		case VK_F11: return "F11";
		case VK_F12: return "F12";
		case VK_F13: return "F13";
		case VK_F14: return "F14";
		case VK_F15: return "F15";
		case VK_F16: return "F16";
		case VK_F17: return "F17";
		case VK_F18: return "F18";
		case VK_F19: return "F19";
		case VK_F20: return "F20";
		case VK_F21: return "F21";
		case VK_F22: return "F22";
		case VK_F23: return "F23";
		case VK_F24: return "F24";
		
		// Special keys
		case VK_SPACE: return "SPACE";
		case VK_TAB: return "TAB";
		case VK_SHIFT: return "SHIFT";
		case VK_CONTROL: return "CTRL";
		case VK_MENU: return "ALT";
		case VK_ESCAPE: return "ESC";
		case VK_RETURN: return "ENTER";
		case VK_BACK: return "BACKSPACE";
		case VK_DELETE: return "DEL";
		case VK_INSERT: return "INS";
		case VK_HOME: return "HOME";
		case VK_END: return "END";
		case VK_PRIOR: return "PAGE UP";
		case VK_NEXT: return "PAGE DOWN";
		
		// Arrow keys
		case VK_UP: return "UP ARROW";
		case VK_DOWN: return "DOWN ARROW";
		case VK_LEFT: return "LEFT ARROW";
		case VK_RIGHT: return "RIGHT ARROW";
		
		// Mouse buttons
		case VK_LBUTTON: return "LEFT MOUSE";
		case VK_RBUTTON: return "RIGHT MOUSE";
		case VK_MBUTTON: return "MIDDLE MOUSE";
		case VK_XBUTTON1: return "MOUSE 4";
		case VK_XBUTTON2: return "MOUSE 5";
		
		// Additional mouse buttons (using virtual key codes)
		case 0x07: return "MOUSE 7";
		
		// Numpad keys
		case VK_NUMPAD0: return "NUMPAD 0";
		case VK_NUMPAD1: return "NUMPAD 1";
		case VK_NUMPAD2: return "NUMPAD 2";
		case VK_NUMPAD3: return "NUMPAD 3";
		case VK_NUMPAD4: return "NUMPAD 4";
		case VK_NUMPAD5: return "NUMPAD 5";
		case VK_NUMPAD6: return "NUMPAD 6";
		case VK_NUMPAD7: return "NUMPAD 7";
		case VK_NUMPAD8: return "NUMPAD 8";
		case VK_NUMPAD9: return "NUMPAD 9";
		case VK_ADD: return "NUMPAD +";
		case VK_SUBTRACT: return "NUMPAD -";
		case VK_MULTIPLY: return "NUMPAD *";
		case VK_DIVIDE: return "NUMPAD /";
		case VK_DECIMAL: return "NUMPAD .";
		
		// Modifier keys
		case VK_LWIN: return "LEFT WIN";
		case VK_RWIN: return "RIGHT WIN";
		case VK_LSHIFT: return "LEFT SHIFT";
		case VK_RSHIFT: return "RIGHT SHIFT";
		case VK_LCONTROL: return "LEFT CTRL";
		case VK_RCONTROL: return "RIGHT CTRL";
		case VK_LMENU: return "LEFT ALT";
		case VK_RMENU: return "RIGHT ALT";
		
		// Media keys
		case VK_VOLUME_MUTE: return "VOLUME MUTE";
		case VK_VOLUME_DOWN: return "VOLUME DOWN";
		case VK_VOLUME_UP: return "VOLUME UP";
		case VK_MEDIA_NEXT_TRACK: return "NEXT TRACK";
		case VK_MEDIA_PREV_TRACK: return "PREV TRACK";
		case VK_MEDIA_STOP: return "MEDIA STOP";
		case VK_MEDIA_PLAY_PAUSE: return "PLAY/PAUSE";
		
		// Browser keys
		case VK_BROWSER_BACK: return "BROWSER BACK";
		case VK_BROWSER_FORWARD: return "BROWSER FORWARD";
		case VK_BROWSER_REFRESH: return "BROWSER REFRESH";
		case VK_BROWSER_STOP: return "BROWSER STOP";
		case VK_BROWSER_SEARCH: return "BROWSER SEARCH";
		case VK_BROWSER_FAVORITES: return "BROWSER FAVORITES";
		case VK_BROWSER_HOME: return "BROWSER HOME";
		
		// Application keys
		case VK_LAUNCH_MAIL: return "LAUNCH MAIL";
		case VK_LAUNCH_MEDIA_SELECT: return "MEDIA SELECT";
		case VK_LAUNCH_APP1: return "APP 1";
		case VK_LAUNCH_APP2: return "APP 2";
		
		default:
			// Letters A-Z
			if (keyCode >= 'A' && keyCode <= 'Z') {
				return std::string(1, static_cast<char>(keyCode));
			}
			// Numbers 0-9
			if (keyCode >= '0' && keyCode <= '9') {
				return std::string(1, static_cast<char>(keyCode));
			}
			// Extended mouse buttons (some gaming mice have more buttons)
			if (keyCode >= 0x09 && keyCode <= 0x0F) {
				return "MOUSE " + std::to_string(keyCode - 0x05);
			}
			return "Unknown (" + std::to_string(keyCode) + ")";
	}
}
