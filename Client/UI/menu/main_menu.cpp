#include "main_menu.h"
#include "../overlays/fps_overlay.h"
#include "../components/lua_executor.h"
#include "../components/hack_engine.h"
#include "../components/notifications.h"
#include "../../include/imgui.h"
#include "../../script_engine.h"
#include "../../imgui_render.h"

// Main menu variables - these are now defined in imgui_render.cpp for backwards compatibility

void RenderMainMenu() {
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
	ImGui::Text("• Current FPS: %.1f", GetCurrentFPS());
	ImGui::Text("• Current Ping: %ums", GetCurrentPing());
	ImGui::Unindent();
	
	ImGui::Separator();
	
	// Features Section
	ImGui::Text("Features:");
	ImGui::Indent();
	ImGui::Checkbox("Enable Overlays", &overlayEnabled);
	ImGui::SameLine();
	if (overlayEnabled) {
		ImGui::PushStyleColor(ImGuiCol_Text, ImVec4(0.0f, 1.0f, 0.0f, 1.0f));
		ImGui::Text("●");
		ImGui::PopStyleColor();
	}
	
	// Overlay configuration (only show if overlays are enabled)
	if (overlayEnabled) {
		ImGui::Indent();
		ImGui::Text("Overlay Widgets:");
		ImGui::Checkbox("Client Title", &titleWidget.enabled);
		ImGui::Checkbox("FPS Info", &fpsWidget.enabled);
		ImGui::Checkbox("Network Info", &networkInfoWidget.enabled);
		ImGui::Checkbox("Active Mods", &activeModsWidget.enabled);
		
		ImGui::Text("Instructions:");
		ImGui::Text("• Drag widgets to reposition them");
		ImGui::Text("• Settings auto-save when changed");
		ImGui::Text("• Each widget can be enabled/disabled");
		
		// Auto-save settings when values change
		static bool lastTitleEnabled = titleWidget.enabled;
		static bool lastFPSEnabled = fpsWidget.enabled;
		static bool lastNetworkEnabled = networkInfoWidget.enabled;
		static bool lastActiveModsEnabled = activeModsWidget.enabled;
		
		if (lastTitleEnabled != titleWidget.enabled ||
		    lastFPSEnabled != fpsWidget.enabled || lastNetworkEnabled != networkInfoWidget.enabled ||
		    lastActiveModsEnabled != activeModsWidget.enabled) {
			SaveOverlaySettings();
			lastTitleEnabled = titleWidget.enabled;
			lastFPSEnabled = fpsWidget.enabled;
			lastNetworkEnabled = networkInfoWidget.enabled;
			lastActiveModsEnabled = activeModsWidget.enabled;
		}
		
		ImGui::Unindent();
	}
	
	ImGui::Checkbox("Lua Script Executor", &showExecutor);
	ImGui::SameLine();
	if (showExecutor) {
		ImGui::PushStyleColor(ImGuiCol_Text, ImVec4(0.0f, 1.0f, 0.0f, 1.0f));
		ImGui::Text("●");
		ImGui::PopStyleColor();
	}
	
	ImGui::Checkbox("Hack Engine", &showHackEngine);
	ImGui::SameLine();
	if (showHackEngine) {
		ImGui::PushStyleColor(ImGuiCol_Text, ImVec4(0.0f, 1.0f, 0.0f, 1.0f));
		ImGui::Text("●");
		ImGui::PopStyleColor();
	}
	
	// Console toggle
	bool consoleEnabled = IsConsoleEnabled();
	if (ImGui::Checkbox("Show Console", &consoleEnabled)) {
		ToggleConsole();
	}
	ImGui::SameLine();
	if (consoleEnabled) {
		ImGui::PushStyleColor(ImGuiCol_Text, ImVec4(0.0f, 1.0f, 0.0f, 1.0f));
		ImGui::Text("●");
		ImGui::PopStyleColor();
	}

	
	ImGui::Separator();
	
	// Notification test section
	ImGui::Text("Notification System:");
	ImGui::Indent();
	ImGui::Columns(2, "NotificationTests", false);
	
	if (ImGui::Button("Test Info", ImVec2(-1, 0))) {
		ShowInfoNotification("This is an info notification!");
	}
	if (ImGui::Button("Test Success", ImVec2(-1, 0))) {
		ShowSuccessNotification("Operation completed successfully!");
	}
	
	ImGui::NextColumn();
	
	if (ImGui::Button("Test Warning", ImVec2(-1, 0))) {
		ShowWarningNotification("This is a warning message!");
	}
	if (ImGui::Button("Test Alert", ImVec2(-1, 0))) {
		ShowAlertNotification("Critical error detected!");
	}
	
	ImGui::Columns(1);
	ImGui::Unindent();
	
	ImGui::Separator();
	
	// EJECT button section
	ImGui::Spacing();
	ImGui::PushStyleColor(ImGuiCol_Text, ImVec4(1.0f, 0.8f, 0.0f, 1.0f)); // Orange text
	ImGui::Text("Client Management:");
	ImGui::PopStyleColor();
	
	ImGui::Spacing();
	
	// Center the EJECT button
	float ejectButtonWidth = 150.0f;
	float windowWidth = ImGui::GetWindowWidth();
	ImGui::SetCursorPosX((windowWidth - ejectButtonWidth) * 0.5f);
	
	// EJECT button with warning styling
	ImGui::PushStyleColor(ImGuiCol_Button, ImVec4(0.8f, 0.4f, 0.0f, 1.0f)); // Orange button
	ImGui::PushStyleColor(ImGuiCol_ButtonHovered, ImVec4(1.0f, 0.5f, 0.0f, 1.0f)); // Brighter orange on hover
	ImGui::PushStyleColor(ImGuiCol_ButtonActive, ImVec4(0.6f, 0.3f, 0.0f, 1.0f)); // Darker orange when pressed
	ImGui::PushStyleColor(ImGuiCol_Text, ImVec4(1.0f, 1.0f, 1.0f, 1.0f)); // White text
	
	if (ImGui::Button("EJECT CLIENT", ImVec2(ejectButtonWidth, 35))) {
		shouldExit = true;
	}
	
	ImGui::PopStyleColor(4);
	
	// EJECT button description
	ImGui::PushStyleColor(ImGuiCol_Text, ImVec4(0.7f, 0.7f, 0.7f, 1.0f));
	ImGui::Text("Safely unload the client from memory");
	ImGui::PopStyleColor();
	
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
		ImGui::Text("Exit Confirmation");
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
