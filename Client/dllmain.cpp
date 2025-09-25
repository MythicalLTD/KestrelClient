// dllmain.cpp : Defines the entry point for the DLL application.
#include "pch.h"
#include <cstdio>
#include <iostream>
#include <thread>
#include <chrono>
#include "hooks.h"
#include "imgui_render.h"

DWORD WINAPI MainThread(HMODULE hModule) {
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

    HookSwapBuffers();
    HookSetCursorPos();

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

    // Clean up: close the console and free resources
    fclose(stdout);
    fclose(stderr);
    fclose(stdin);
    FreeConsole();
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