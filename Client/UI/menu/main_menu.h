#pragma once

// Main menu variables
extern bool showMenu;
extern bool showExitDialog;
extern bool shouldExit;

// Function declarations
void RenderMainMenu();
void ShowExitConfirmationDialog();
void ResetExitState();
bool ShouldExit();
void SetExitConfirmationVisible(bool visible);
