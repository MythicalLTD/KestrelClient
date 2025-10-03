#include "ui_manager.h"
#include "overlays/fps_overlay.h"
#include "components/lua_executor.h"
#include "components/hack_engine.h"
#include "components/auto_clicker.h"
#include "components/presets_manager.h"
#include "components/notifications.h"
#include "menu/main_menu.h"

void InitializeUI() {
	InitializeFPS();
	LoadSettingsFromDisk(); // Load settings and primary preset
	LoadPrimaryPreset(); // Auto-load primary preset on startup
}

void RenderImGui() {
	// Update FPS tracking
	UpdateFPS();
	
	// Update ping tracking
	UpdatePing();
	
	// Render FPS overlay (always visible if enabled, regardless of menu state)
	RenderFPSOverlay();
	
	// Render notifications (always on top)
	RenderNotifications();
	
	// Initialize hack engine and load primary preset automatically (always runs in background)
	static bool hackEngineInitialized = false;
	if (!hackEngineInitialized) {
		// Only initialize states if they haven't been initialized yet
		if (lastKnownStates.empty()) {
			InitializeHackStates(); // Initialize states only once
		}
		CheckHackEngineConnection(); // Check server state to update lastKnownStates
		LoadPrimaryPresetWhenReady(); // Load primary preset when hack engine is ready
		hackEngineInitialized = true;
	}
	
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
		// Even when menu is closed, we still render the FPS overlay and notifications above
		return;
	}

	// Auto-open hack engine and executor when main menu opens
	if (showMenu) {
		if (!showHackEngine) {
			showHackEngine = true;
		}
		if (!showExecutor) {
			showExecutor = true;
		}
	}

	// Render the main menu
	RenderMainMenu();
}

// Notification functions are available directly from notifications.h
