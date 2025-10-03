#include "UI/menu/main_menu.h"
#include <windows.h>

#pragma comment(lib, "advapi32.lib")

// Legacy variables for backwards compatibility
bool showMenu = false;
bool showExitDialog = false;
bool shouldExit = false;

// Console functions
void ToggleConsole() {
    HKEY hKey;
    DWORD dwValue = 0;
    DWORD dwSize = sizeof(DWORD);
    
    // Read current value
    if (RegOpenKeyExA(HKEY_CURRENT_USER, "SOFTWARE\\KestrelClient", 0, KEY_READ, &hKey) == ERROR_SUCCESS) {
        RegQueryValueExA(hKey, "ShowConsole", NULL, NULL, (LPBYTE)&dwValue, &dwSize);
        RegCloseKey(hKey);
    }
    
    // Toggle the value
    dwValue = (dwValue == 0) ? 1 : 0;
    
    // Write new value
    if (RegOpenKeyExA(HKEY_CURRENT_USER, "SOFTWARE\\KestrelClient", 0, KEY_WRITE, &hKey) == ERROR_SUCCESS) {
        RegSetValueExA(hKey, "ShowConsole", 0, REG_DWORD, (LPBYTE)&dwValue, sizeof(DWORD));
        RegCloseKey(hKey);
    }
}

bool IsConsoleEnabled() {
    HKEY hKey;
    DWORD dwValue = 1;
    DWORD dwSize = sizeof(DWORD);
    
    if (RegOpenKeyExA(HKEY_CURRENT_USER, "SOFTWARE\\KestrelClient", 0, KEY_READ, &hKey) == ERROR_SUCCESS) {
        if (RegQueryValueExA(hKey, "ShowConsole", NULL, NULL, (LPBYTE)&dwValue, &dwSize) == ERROR_SUCCESS) {
            RegCloseKey(hKey);
            return (dwValue != 0);
        }
        RegCloseKey(hKey);
    }
    return true; // Default to enabled
}