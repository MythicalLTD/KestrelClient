#include <imgui.h>
#include "timer.h"

bool showMenu = false;
bool showExitDialog = false;
bool shouldExit = false;

static bool timerHackEnabled = false;
static float timerSpeed = 1.0f;

// Forward declarations
void ShowExitConfirmationDialog();
void ResetExitState();

void RenderImGui() {
	// Show exit confirmation dialog if needed (regardless of menu state)
	if (showExitDialog) {
		ShowExitConfirmationDialog();
		return; // Don't show other UI when exit dialog is open
	}

	if (!showMenu) return;

	ImGui::Begin("KestrelClient v0.1");

	if (ImGui::Checkbox("Enable Timer Hack", &timerHackEnabled)) {
		if (timerHackEnabled) {
			InitTimerHack();
			SetTimerSpeed(timerSpeed);
			StartTimerHackThread();
		}
		else {
			StopTimerHackThread();
		}
	}

	if (ImGui::SliderFloat("Timer Speed", &timerSpeed, 0.1f, 10.0f)) {
		if (timerHackEnabled) {
			SetTimerSpeed(timerSpeed);
		}
	}
	
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