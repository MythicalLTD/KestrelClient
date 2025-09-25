// dllmain.cpp : Defines the entry point for the DLL application.
#include "pch.h"
#include <cstdio>
#include <thread>
#include <chrono>
#include "hooks.h"

DWORD WINAPI MainThread(HMODULE hModule) {
    //AllocConsole();
    FILE* f;
	freopen_s(&f, "CONOUT$", "w", stdout);

    HookSwapBuffers();
	HookSetCursorPos();

    while (!GetAsyncKeyState(VK_RSHIFT)) {
		std::this_thread::sleep_for(std::chrono::milliseconds(100));
    }

	UnhookSwapBuffers();
    fclose(stdout);
    //FreeConsole();
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