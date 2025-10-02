#include <Windows.h>
#include "include/MinHook.h"
#include <atomic>
#include <string>
#include "include/imgui.h"
#include "include/imgui_impl_win32.h"
#include "include/imgui_impl_opengl3.h"
#include "include/glad/gl.h"
#include "imgui_render.h"
#include "network_monitor.h"

typedef BOOL(WINAPI* SwapBuffers_t)(HDC);
SwapBuffers_t oSwapBuffers = nullptr;

HWND g_hWnd = nullptr;
WNDPROC oWndProc = nullptr;

std::atomic_bool g_Initialized{false};
std::atomic_bool g_HookInstalled{false};

// Set this to false to completely disable UI rendering and avoid ImGui issues
bool g_EnableUI = true;

extern IMGUI_IMPL_API LRESULT ImGui_ImplWin32_WndProcHandler(HWND hWnd, UINT msg, WPARAM wParam, LPARAM lParam);

extern bool showMenu;
extern bool showExitDialog;

// Global variable to store the partial window title to search for
const char* mcTitles[] = {
    "Minecraft",
    "Feather Client",
    "Lunar Client",
    "Badlion Client",
    "PVP Lounge",
    "CheatBreaker"
};

LRESULT CALLBACK hkWndProc(HWND hWnd, UINT msg, WPARAM wParam, LPARAM lParam) {
    if (showMenu || showExitDialog) {
        if (ImGui::GetCurrentContext() && ImGui_ImplWin32_WndProcHandler(hWnd, msg, wParam, lParam))
            return true;

        switch (msg) {
        case WM_MOUSEMOVE:
        case WM_LBUTTONDOWN:
        case WM_LBUTTONUP:
        case WM_RBUTTONDOWN:
        case WM_RBUTTONUP:
        case WM_MBUTTONDOWN:
        case WM_MBUTTONUP:
        case WM_XBUTTONDOWN:
        case WM_XBUTTONUP:
        case WM_MOUSEWHEEL:
        case WM_MOUSEHWHEEL:
        case WM_KEYDOWN:
        case WM_KEYUP:
        case WM_CHAR:
            return true; // eat input, prevent Minecraft from handling it
        }

    }
    return CallWindowProc(oWndProc, hWnd, msg, wParam, lParam);
}

typedef BOOL(WINAPI* SetCursorPos_t)(int X, int Y);
SetCursorPos_t oSetCursorPos = nullptr;

BOOL WINAPI hkSetCursorPos(int X, int Y) {
    if (showMenu || showExitDialog) {
        return TRUE; // Prevent Minecraft from changing the cursor position when the menu or dialog is open
    }
    return oSetCursorPos(X, Y);
}


void HookSetCursorPos() {
    HMODULE hUser32 = GetModuleHandleA("user32.dll");
    void* target = GetProcAddress(hUser32, "SetCursorPos");

    MH_CreateHook(target, &hkSetCursorPos, reinterpret_cast<void**>(&oSetCursorPos));

    MH_EnableHook(target);
}

void SetCursorVisibility(bool visible) {
    if (visible) {
        while (ShowCursor(TRUE) < 0) {} // Ensure cursor is visible
    }
    else {
        while (ShowCursor(FALSE) >= 0) {} // Ensure cursor is hidden
    }
}

// Callback function to be used with EnumWindows
BOOL CALLBACK EnumWindowsProc(HWND hWnd, LPARAM lParam) {
    char windowTitle[256];
    GetWindowTextA(hWnd, windowTitle, sizeof(windowTitle));
    std::string title = windowTitle;

    for (const char* targetTitlePart : mcTitles) {
        if (title.find(targetTitlePart) != std::string::npos) {
            g_hWnd = hWnd;
            return FALSE; // Found it, stop the enumeration
        }
    }
    return TRUE; // Continue enumerating
}

BOOL WINAPI hkSwapBuffers(HDC hdc) {
    static thread_local bool in_hook = false;
    if (in_hook) return oSwapBuffers(hdc);
    in_hook = true;

    // Skip all UI logic if UI is disabled
    if (!g_EnableUI) {
        in_hook = false;
        return oSwapBuffers(hdc);
    }

    if (!g_Initialized.load()) {
        // Find the Minecraft window using the EnumWindows callback
        EnumWindows(EnumWindowsProc, 0);
        if (g_hWnd && !oWndProc) {
            oWndProc = (WNDPROC)SetWindowLongPtr(g_hWnd, GWLP_WNDPROC, (LONG_PTR)hkWndProc);
        }
        if (g_hWnd) {
            IMGUI_CHECKVERSION();
            ImGui::CreateContext();
            ImGuiIO& io = ImGui::GetIO();
            ImGui_ImplWin32_Init(g_hWnd);
            if (!gladLoaderLoadGL()) {
                in_hook = false;
                return oSwapBuffers(hdc);
            }
            ImGui_ImplOpenGL3_Init("#version 120");
            
            // Initialize FPS tracking
            InitializeFPS();
            NetworkMonitor_Initialize();
            
            g_Initialized = true;
        }
        else {
            // No window found yet. Let the hook continue to run, but don't show an error.
            in_hook = false;
            return oSwapBuffers(hdc);
        }
    }

    if (g_Initialized.load()) {
        if ((GetAsyncKeyState(VK_INSERT) & 1) != 0) {
            showMenu = !showMenu;
        }

        // Always render ImGui to show FPS overlay, but only capture input when menu is open
        ImGui_ImplOpenGL3_NewFrame();
        ImGui_ImplWin32_NewFrame();
        ImGui::NewFrame();

        RenderImGui();

        ImGui::Render();
        
        GLint last_program = 0, last_active_texture = 0, last_texture = 0;
        GLint last_array_buffer = 0, last_vertex_array = 0, last_viewport[4];
        GLboolean last_blend = glIsEnabled(GL_BLEND);
        GLboolean last_depth_test = glIsEnabled(GL_DEPTH_TEST);
        GLboolean last_scissor_test = glIsEnabled(GL_SCISSOR_TEST);

        glGetIntegerv(GL_CURRENT_PROGRAM, &last_program);
        glGetIntegerv(GL_ACTIVE_TEXTURE, &last_active_texture);
        glGetIntegerv(GL_TEXTURE_BINDING_2D, &last_texture);
        glGetIntegerv(GL_ARRAY_BUFFER_BINDING, &last_array_buffer);
        glGetIntegerv(GL_VERTEX_ARRAY_BINDING, &last_vertex_array);
        glGetIntegerv(GL_VIEWPORT, last_viewport);

        ImGui_ImplOpenGL3_RenderDrawData(ImGui::GetDrawData());

        glUseProgram(last_program);
        glActiveTexture(last_active_texture);
        glBindTexture(GL_TEXTURE_2D, last_texture);
        glBindBuffer(GL_ARRAY_BUFFER, last_array_buffer);
        glBindVertexArray(last_vertex_array);
        glViewport(last_viewport[0], last_viewport[1], last_viewport[2], last_viewport[3]);

        if (last_blend) glEnable(GL_BLEND); else glDisable(GL_BLEND);
        if (last_depth_test) glEnable(GL_DEPTH_TEST); else glDisable(GL_DEPTH_TEST);
        if (last_scissor_test) glEnable(GL_SCISSOR_TEST); else glDisable(GL_SCISSOR_TEST);
    }
    if (showMenu || showExitDialog) {
        SetCursorVisibility(true);
        ClipCursor(nullptr);
    }
    in_hook = false;
    return oSwapBuffers(hdc);
}

void HookSwapBuffers() {
    if (!g_HookInstalled.load()) {
        if (MH_Initialize() != MH_OK) {
            return;
        }

        HMODULE hGL = GetModuleHandleA("opengl32.dll");
        void* target = GetProcAddress(hGL, "wglSwapBuffers");
        MH_CreateHook(target, &hkSwapBuffers, reinterpret_cast<void**>(&oSwapBuffers));
        if (MH_EnableHook(target) != MH_OK) {
            return;
        }
        g_HookInstalled = true;
    }
}

void UnhookSwapBuffers() {
    // Only shutdown ImGui if it was properly initialized
    if (g_Initialized.load()) {
        ImGui_ImplOpenGL3_Shutdown();
        ImGui_ImplWin32_Shutdown();
        ImGui::DestroyContext();
        g_Initialized = false;
    }

    if (g_hWnd && oWndProc) {
        SetWindowLongPtr(g_hWnd, GWLP_WNDPROC, (LONG_PTR)oWndProc);
    }

    MH_DisableHook(MH_ALL_HOOKS);
    MH_Uninitialize();

    NetworkMonitor_Shutdown();
}