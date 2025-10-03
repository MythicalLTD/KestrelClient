// dllmain.cpp : Defines the entry point for the DLL application.
#include "pch.h"
#include <cstdio>
#include <iostream>
#include <thread>
#include <chrono>
#include "hooks.h"
#include "imgui_render.h"
#include "script_engine.h"

#pragma comment(lib, "advapi32.lib")

DWORD WINAPI MainThread(HMODULE hModule) {
    // Check if console should be shown (check registry setting)
    bool showConsole = true;
    
    // Check registry for console setting
    HKEY hKey;
    DWORD dwValue = 1;
    DWORD dwSize = sizeof(DWORD);
    
    if (RegOpenKeyExA(HKEY_CURRENT_USER, "SOFTWARE\\KestrelClient", 0, KEY_READ, &hKey) == ERROR_SUCCESS) {
        if (RegQueryValueExA(hKey, "ShowConsole", NULL, NULL, (LPBYTE)&dwValue, &dwSize) == ERROR_SUCCESS) {
            showConsole = (dwValue != 0);
        }
        RegCloseKey(hKey);
    } else {
        // Create registry key with default value
        RegCreateKeyExA(HKEY_CURRENT_USER, "SOFTWARE\\KestrelClient", 0, NULL, REG_OPTION_NON_VOLATILE, KEY_WRITE, NULL, &hKey, NULL);
        RegSetValueExA(hKey, "ShowConsole", 0, REG_DWORD, (LPBYTE)&dwValue, sizeof(DWORD));
        RegCloseKey(hKey);
    }
    
    if (showConsole) {
        // Allocate a new console for output
        AllocConsole();

        // Redirect stdout, stderr, and stdin to the new console
        FILE* fDummy;
        freopen_s(&fDummy, "CONOUT$", "w", stdout);
        freopen_s(&fDummy, "CONOUT$", "w", stderr);
        freopen_s(&fDummy, "CONIN$", "r", stdin);

        // Set the standard handles to the new console
        SetStdHandle(STD_OUTPUT_HANDLE, GetStdHandle(STD_OUTPUT_HANDLE));
        SetStdHandle(STD_ERROR_HANDLE, GetStdHandle(STD_ERROR_HANDLE));
        SetStdHandle(STD_INPUT_HANDLE, GetStdHandle(STD_INPUT_HANDLE));

        // Optional: clear error state for each stream
        std::wcout.clear();
        std::cout.clear();
        std::wcerr.clear();
        std::cerr.clear();
        std::wcin.clear();
        std::cin.clear();
    }

    HookSwapBuffers();
    HookSetCursorPos();
    InitializeFPS();
    InitializeUI(); // Initialize UI and load primary preset

    // Initialize script engine
    g_ScriptEngine = new ScriptEngine();
    if (g_ScriptEngine->Initialize()) {
        printf("Script engine initialized successfully!\n");
        
        // Setup game API callbacks
        g_ScriptEngine->SetLogCallback([](const std::string& msg) {
            printf("[LUA] %s\n", msg.c_str());
        });
        
        g_ScriptEngine->SetKeyPressedCallback([](const std::string& key) -> bool {
            if (key == "VK_SPACE") return (GetAsyncKeyState(VK_SPACE) & 0x8000) != 0;
            if (key == "VK_RETURN") return (GetAsyncKeyState(VK_RETURN) & 0x8000) != 0;
            if (key == "VK_ESCAPE") return (GetAsyncKeyState(VK_ESCAPE) & 0x8000) != 0;
            return false;
        });
        
    } else {
        printf("Failed to initialize script engine!\n");
        delete g_ScriptEngine;
        g_ScriptEngine = nullptr;
    }

    // Print a message to verify console output is working
    printf("Console attached! Press F8 to show exit confirmation...\n");

    bool exitRequested = false;
    bool f8Pressed = false;
    
    while (!exitRequested) {
        // Check for F8 key press to show exit confirmation
        if (GetAsyncKeyState(VK_F8) & 0x8000) {
            if (!f8Pressed) {
                f8Pressed = true;
                printf("Exit confirmation requested. Showing dialog...\n");
                SetExitConfirmationVisible(true);
            }
        } else {
            f8Pressed = false;
        }
        
        // Check if user confirmed exit
        if (ShouldExit()) {
            printf("User confirmed exit. Cleaning up...\n");
            exitRequested = true;
        }
        
        std::this_thread::sleep_for(std::chrono::milliseconds(100));
    }

    UnhookSwapBuffers();
    ResetExitState();
    
    // Cleanup hack mods - disable all active mods
    printf("Disabling all active hack mods...\n");
    DisableAllMods();
    
    // Cleanup script engine
    if (g_ScriptEngine) {
        g_ScriptEngine->Shutdown();
        delete g_ScriptEngine;
        g_ScriptEngine = nullptr;
        printf("Script engine cleaned up.\n");
    }

    

    // Clean up: close the console and free resources (only if console was opened)
    if (showConsole) {
        fclose(stdout);
        fclose(stderr);
        fclose(stdin);
        FreeConsole();
    }
    FreeLibraryAndExitThread(hModule, 0);
    return 0;
}

BOOL APIENTRY DllMain( HMODULE hModule,DWORD  ul_reason_for_call,LPVOID lpReserved)
{
    switch (ul_reason_for_call)
    {
    case DLL_PROCESS_ATTACH:
		DisableThreadLibraryCalls(hModule);
		CreateThread(nullptr, 0, (LPTHREAD_START_ROUTINE)MainThread, hModule, 0, nullptr);
    case DLL_THREAD_ATTACH:
    case DLL_THREAD_DETACH:
    case DLL_PROCESS_DETACH:
        break;
    }
    return TRUE;
}