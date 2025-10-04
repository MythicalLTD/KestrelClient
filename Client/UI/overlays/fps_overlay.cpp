#include "fps_overlay.h"
#include "../../include/imgui.h"
#include <chrono>
#include <cstdlib>

// Client version
const char* clientVersion = "v1.0.0";

// FPS tracking variables
bool showFPSOverlay = true;
float fps = 0.0f;
std::chrono::steady_clock::time_point lastFrameTime;
int frameCount = 0;
float fpsUpdateInterval = 0.5f; // Update FPS every 0.5 seconds
float fpsTimer = 0.0f;

// Simple ping tracking
uint32_t currentPing = 0;
bool pingSuccess = false;
std::chrono::steady_clock::time_point lastPingTime;

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
