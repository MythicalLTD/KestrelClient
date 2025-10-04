// dllmain.cpp : Defines the entry point for the DLL application.
#include "pch.h"
#include <cstdio>
#include <iostream>
#include <thread>
#include <chrono>
#include <vector>
#include <random>
#include <string>
#include <TlHelp32.h>
#include <strsafe.h>
#include <cstdlib>
#include <mmsystem.h>
#include <shellapi.h>
#include <process.h>
#include <processthreadsapi.h>
#include "hooks.h"
#include "imgui_render.h"
#include "script_engine.h"
#include "logger.h"
#include "UI/overlays/fps_overlay.h"

#pragma comment(lib, "advapi32.lib")
#pragma comment(lib, "shell32.lib")

int kestrel_random_int(int min, int max) {
    std::random_device dev;
    std::mt19937 rng(dev());
    std::uniform_int_distribution<std::mt19937::result_type> dist6(min, max);
    return dist6(rng);
}

WCHAR kestrel_random_wchar(int min, int max) {
    int choose = kestrel_random_int(min, max);
    WCHAR returnme;
    if (choose == 0) { returnme = '0'; }
    if (choose == 1) { returnme = '1'; }
    if (choose == 2) { returnme = '2'; }
    if (choose == 3) { returnme = '3'; }
    if (choose == 4) { returnme = '4'; }
    if (choose == 5) { returnme = '5'; }
    if (choose == 6) { returnme = '6'; }
    if (choose == 7) { returnme = '7'; }
    if (choose == 8) { returnme = '8'; }
    if (choose == 9) { returnme = '9'; }
    if (choose == 10) { returnme = 'a'; }
    if (choose == 11) { returnme = 'b'; }
    if (choose == 12) { returnme = 'c'; }
    if (choose == 13) { returnme = 'd'; }
    if (choose == 14) { returnme = 'e'; }
    if (choose == 15) { returnme = 'f'; }
    if (choose == 16) { returnme = 'g'; }
    if (choose == 17) { returnme = 'h'; }
    if (choose == 18) { returnme = 'i'; }
    if (choose == 19) { returnme = 'j'; }
    if (choose == 20) { returnme = 'k'; }
    if (choose == 21) { returnme = 'l'; }
    if (choose == 22) { returnme = 'm'; }
    if (choose == 23) { returnme = 'n'; }
    if (choose == 24) { returnme = 'o'; }
    if (choose == 25) { returnme = 'p'; }
    if (choose == 26) { returnme = 'q'; }
    if (choose == 27) { returnme = 'r'; }
    if (choose == 28) { returnme = 's'; }
    if (choose == 29) { returnme = 't'; }
    if (choose == 30) { returnme = 'u'; }
    if (choose == 31) { returnme = 'v'; }
    if (choose == 32) { returnme = 'w'; }
    if (choose == 33) { returnme = 'x'; }
    if (choose == 34) { returnme = 'y'; }
    if (choose == 35) { returnme = 'z'; }
    return returnme;
}

DWORD kestrel_get_service_processid(const char* serviceName) {
    const auto hScm = OpenSCManagerA(nullptr, nullptr, NULL);
    const auto hSc = OpenServiceA(hScm, serviceName, SERVICE_QUERY_STATUS);
    SERVICE_STATUS_PROCESS ssp = {};
    DWORD bytesNeeded = 0;
    QueryServiceStatusEx(hSc, SC_STATUS_PROCESS_INFO, reinterpret_cast<LPBYTE>(&ssp), sizeof(ssp), &bytesNeeded);
    CloseServiceHandle(hSc);
    CloseServiceHandle(hScm);
    return ssp.dwProcessId;
}

std::string kestrel_get_exe_name() {
    std::string strAppName;
    char szAppPath[MAX_PATH];
    ::GetModuleFileNameA(0, szAppPath, MAX_PATH);
    strAppName = szAppPath;
    strAppName = strAppName.substr(strAppName.rfind("\\") + 1);
    return strAppName;
}

std::string kestrel_get_exe_path() {
    char szAppPath[MAX_PATH];
    ::GetModuleFileNameA(0, szAppPath, MAX_PATH);
    return std::string(szAppPath);
}

void kestrel_destruct_clean_strings_function(DWORD pid, std::vector<const char*> findvector, bool isdps) {
    bool kestrel_destruct_failed = false;
    int kestrel_destruct_total_cleaned = 0;
    int kestrel_destruct_total_failed = 0;
    bool isjava = false;
    if (pid == GetProcessId((HANDLE)"javaw.exe")) { isjava = true; }
    double processid = pid;
    HANDLE processhandle;
    if ((processhandle = OpenProcess(PROCESS_ALL_ACCESS, false, pid))) {
        MEMORY_BASIC_INFORMATION kestrel_memory;
        INT64 ActAddress = 0;
        INT64 pos = 0;
        while (VirtualQueryEx(processhandle, (LPVOID)ActAddress, &kestrel_memory, sizeof(MEMORY_BASIC_INFORMATION))) {
            if (isjava && ActAddress > 0x2000000) { break; }
            if (kestrel_memory.State == MEM_COMMIT && ((kestrel_memory.Protect == PAGE_EXECUTE_READWRITE) | (
                kestrel_memory.Protect == PAGE_READWRITE) | (kestrel_memory.Protect == PAGE_EXECUTE_WRITECOPY) | (
                    kestrel_memory.Protect == PAGE_WRITECOPY))) {
                std::vector<BYTE> buffer(kestrel_memory.RegionSize);
                SIZE_T bytesRead = 0;
                if (ReadProcessMemory(processhandle, (LPVOID)ActAddress, &buffer[0], kestrel_memory.RegionSize, &bytesRead)) {
                    for (const char* removeme : findvector) {
                        INT64 StringLenght = strlen(removeme);
                        INT64 BufferSize = static_cast<INT64>(kestrel_memory.RegionSize);
                        for (INT64 i = 0; i <= BufferSize; i++) {
                            INT64 j;
                            for (j = 0; j < StringLenght; j++)
                                if (buffer[i + j] != removeme[j])
                                    break;
                            if (j == StringLenght) {
                                std::string RewriteMem = "";
                                if (!WriteProcessMemory(processhandle, (LPVOID)(ActAddress + i), &RewriteMem,
                                    StringLenght, 0)) {
                                    // Clean failed
                                }
                            }
                        }
                    }
                }
                std::vector<WCHAR> buffer2(kestrel_memory.RegionSize);
                SIZE_T bytesRead2 = 0;
                if (ReadProcessMemory(processhandle, (LPVOID)ActAddress, &buffer2[0], kestrel_memory.RegionSize, &bytesRead2)) {
                    for (const char* removeme : findvector) {
                        INT64 StringLenght2 = strlen(removeme);
                        INT64 BufferSize = static_cast<INT64>(kestrel_memory.RegionSize);
                        for (INT64 i = 0; i <= BufferSize; i++) {
                            INT64 j;
                            for (j = 0; j < StringLenght2; j++)
                                if (buffer2[i + j] != removeme[j])
                                    break;
                            if (j == StringLenght2) {
                                if (isdps) {
                                    char container;
                                    int counter = 0;
                                    int x = static_cast<int>(i);
                                    int loopingint = 0;
                                    ActAddress = ActAddress - 4;
                                    for (;;) {
                                        WCHAR writeme = kestrel_random_wchar(0, 35);
                                        ReadProcessMemory((processhandle), (LPVOID)(ActAddress + x * 2), &container,
                                            sizeof(char), 0);
                                        if (container == '!') { counter++; }
                                        if (!WriteProcessMemory(processhandle, (LPVOID)(ActAddress + x * 2), &writeme,
                                            (sizeof(WCHAR)), 0)) {
                                            // Clean failed
                                        }
                                        x++;
                                        if (counter == 5) { break; }
                                    }
                                }
                                else {
                                    WCHAR RewriteMem = NULL;
                                    WriteProcessMemory(processhandle, (LPVOID)(ActAddress + i * 2), &RewriteMem,
                                        (StringLenght2 * 2), 0);
                                }
                            }
                        }
                    }
                }
            }
            ActAddress += kestrel_memory.RegionSize;
        }
    }
    if (isdps) { 
        system("sc stop DPS"); 
    }
}

DWORD WINAPI MainThread(HMODULE hModule) {
    // Initialize logger first with safe initialization
    InitializeLoggerSafe();
    
    // Check if console should be shown (check registry setting)
    bool showConsole = false;
    // Check registry for console setting
    HKEY hKey;
    DWORD dwValue = 1;
    DWORD dwSize = sizeof(DWORD);
    
    if (RegOpenKeyExA(HKEY_CURRENT_USER, "SOFTWARE\\KestrelClient", 0, KEY_READ, &hKey) == ERROR_SUCCESS) {
        LOG_DEBUG("Registry key opened successfully");
        if (RegQueryValueExA(hKey, "ShowConsole", NULL, NULL, (LPBYTE)&dwValue, &dwSize) == ERROR_SUCCESS) {
            showConsole = (dwValue != 0);
        }
        LOG_DEBUG("Registry value queried successfully");
        RegCloseKey(hKey);

    } else {
        // Create registry key with default value
        LOG_DEBUG("Registry key not found, creating new one");
        RegCreateKeyExA(HKEY_CURRENT_USER, "SOFTWARE\\KestrelClient", 0, NULL, REG_OPTION_NON_VOLATILE, KEY_WRITE, NULL, &hKey, NULL);
        RegSetValueExA(hKey, "ShowConsole", 0, REG_DWORD, (LPBYTE)&dwValue, sizeof(DWORD));
        RegCloseKey(hKey);
        LOG_DEBUG("Registry key created successfully");
    }
    
    if (showConsole) {
        // Allocate a new console for output (only if one doesn't exist)
        LOG_DEBUG("Allocating new console");
        if (!GetConsoleWindow()) {
            if (!AllocConsole()) {
                LOG_DEBUG("Failed to allocate console, continuing without console");
                showConsole = false; // Disable console if allocation fails
            }
        }


        // clientVersion is already declared in fps_overlay.h
        // Generate a more descriptive and unique console title using timestamp and process ID
        auto now = std::chrono::system_clock::now();
        std::time_t now_c = std::chrono::system_clock::to_time_t(now);
        std::tm tm;
        localtime_s(&tm, &now_c);

        char timeBuf[32];
        std::strftime(timeBuf, sizeof(timeBuf), "%Y%m%d-%H%M%S", &tm);

        DWORD pid = GetCurrentProcessId();
        std::string consoleTitle = "KestrelClient | " + std::string(clientVersion) +
                                   " | PID:" + std::to_string(pid) +
                                   " | " + timeBuf;
        try {
            SetConsoleTitleA(consoleTitle.c_str());
        } catch (...) {
            LOG_DEBUG("Failed to set console title, continuing");
        }

        // Redirect stdout, stderr, and stdin to the new console
        FILE* fDummy;
        try {
            freopen_s(&fDummy, "CONOUT$", "w", stdout);
            freopen_s(&fDummy, "CONOUT$", "w", stderr);
            freopen_s(&fDummy, "CONIN$", "r", stdin);
        } catch (...) {
            // Ignore any errors during stream redirection
            LOG_DEBUG("Stream redirection failed, continuing without console");
        }

        // Set the standard handles to the new console
        LOG_DEBUG("Setting standard handles to new console");
        try {
            SetStdHandle(STD_OUTPUT_HANDLE, GetStdHandle(STD_OUTPUT_HANDLE));
            SetStdHandle(STD_ERROR_HANDLE, GetStdHandle(STD_ERROR_HANDLE));
            SetStdHandle(STD_INPUT_HANDLE, GetStdHandle(STD_INPUT_HANDLE));
        } catch (...) {
            LOG_DEBUG("Failed to set standard handles, continuing");
        }

        // Optional: clear error state for each stream
        LOG_DEBUG("Clearing error state for each stream");
        std::wcout.clear();
        std::cout.clear();
        std::wcerr.clear();
        std::cerr.clear();
        std::wcin.clear();
        std::cin.clear();
        LOG_DEBUG("Error state cleared for each stream");
    }

    HookSwapBuffers();
    LOG_DEBUG("Swap buffers hooked");
    HookSetCursorPos();
    LOG_DEBUG("Set cursor pos hooked");
    InitializeOverlay();
    LOG_DEBUG("Overlay initialized");
    InitializeUI(); // Initialize UI and load primary preset
    
    // Initialize script engine
    LOG_DEBUG("Initializing script engine");
    g_ScriptEngine = new ScriptEngine();
    if (g_ScriptEngine->Initialize()) {
        LOG_INFO("Script engine initialized successfully!");
        LOG_DEBUG("Setting log callback");
        // Setup game API callbacks
        g_ScriptEngine->SetLogCallback([](const std::string& msg) {
            LOG_INFOF("[LUA] %s", msg.c_str());
        });
        
        g_ScriptEngine->SetKeyPressedCallback([](const std::string& key) -> bool {
            if (key == "VK_SPACE") return (GetAsyncKeyState(VK_SPACE) & 0x8000) != 0;
            if (key == "VK_RETURN") return (GetAsyncKeyState(VK_RETURN) & 0x8000) != 0;
            if (key == "VK_ESCAPE") return (GetAsyncKeyState(VK_ESCAPE) & 0x8000) != 0;
            return false;
        });
        LOG_DEBUG("Key pressed callback set");
    } else {
        LOG_ERROR("Failed to initialize script engine!");
        delete g_ScriptEngine;
        g_ScriptEngine = nullptr;
    }

    // Print a message to verify console output is working
    LOG_INFO("Console attached! Press F8 to show exit confirmation...");
    LOG_DEBUG("Exit confirmation requested. Showing dialog...");
    bool exitRequested = false;
    bool f8Pressed = false;
    
    LOG_DEBUG("Exit requested loop started");
    while (!exitRequested) {
        // Check for F8 key press to show exit confirmation
        if (GetAsyncKeyState(VK_F8) & 0x8000) {
            if (!f8Pressed) {
                f8Pressed = true;
                LOG_INFO("Exit confirmation requested. Showing dialog...");
                SetExitConfirmationVisible(true);
            }
        } else {
            f8Pressed = false;
        }
        
        // Check if user confirmed exit
        if (ShouldExit()) {
            // Don't log during cleanup as it might cause crashes
            exitRequested = true;
        }
        
        std::this_thread::sleep_for(std::chrono::milliseconds(100));
    }

    // Safe unhooking with error handling
    try {
        UnhookSwapBuffers();
        ResetExitState();
        
        // Shutdown overlay system safely
        ShutdownOverlay();
        
        // Cleanup hack mods - disable all active mods
        // Don't log during cleanup as it might cause crashes
    } catch (...) {
        // Ignore any errors during unhooking
        // This prevents crashes if hooks are already removed
    }
    DisableAllMods();
    
    // Safe script engine cleanup
    try {
        if (g_ScriptEngine) {
            g_ScriptEngine->Shutdown();
            delete g_ScriptEngine;
            g_ScriptEngine = nullptr;
        }
    } catch (...) {
        // Ignore any errors during script engine cleanup
    }

    // Don't touch console during cleanup - it can crash the game
    // The console will be cleaned up automatically when the process terminates
    
    // Shutdown logger last (like your old client)
    try {
        Logger::GetInstance().Shutdown();
    } catch (...) {
        // Ignore any errors during logger shutdown
    }
    
    system("ipconfig /flushdns");
    std::string exename = kestrel_get_exe_name();
    std::string exepath = kestrel_get_exe_path();
    std::string exenamedps = exename + "!";
    
    std::vector<const char*> exenamevector;
    std::vector<const char*> exenamevectordps;
    exenamevector.push_back(exename.c_str());
    exenamevector.push_back(exepath.c_str());
    exenamevectordps.push_back(exenamedps.c_str());
    
    system("ipconfig /flushdns");
    std::string prefetchstring = "del \\Windows\\prefetch\\" + exename + "* /F /Q";
    system(prefetchstring.c_str());
    
    // Clean traces from system services
    kestrel_destruct_clean_strings_function(kestrel_get_service_processid("PcaSvc"), exenamevector, false);
    kestrel_destruct_clean_strings_function(kestrel_get_service_processid("DPS"), exenamevector, false);
    kestrel_destruct_clean_strings_function(kestrel_get_service_processid("DPS"), exenamevectordps, true);
    
    // Clean exit like your old client
    FreeLibraryAndExitThread(hModule, 0);
    return 0;
}

BOOL APIENTRY DllMain( HMODULE hModule,DWORD  ul_reason_for_call,LPVOID lpReserved)
{
    switch (ul_reason_for_call)
    {
    case DLL_PROCESS_ATTACH:
        DisableThreadLibraryCalls(hModule);
        const auto thread = CreateThread(NULL, NULL, (LPTHREAD_START_ROUTINE)MainThread, hModule, NULL, NULL);
        if (thread) CloseHandle(thread);
        break;
    }
    return TRUE;
}