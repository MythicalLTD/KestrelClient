#include "pch.h"
#include "script_engine.h"
#include <fstream>
#include <sstream>
#include <windows.h>
#include <psapi.h>
#include <chrono>
#include <thread>

// Include Lua headers (you'll need to add Lua to your project)
// For now, I'll create a mock implementation that you can replace with real Lua
extern "C" {
    // Mock Lua functions - replace with real Lua library
    struct lua_State {
        int dummy; // Make it a non-empty struct so it's not nullptr
    };
    
    static lua_State mockLuaState = {1}; // Static instance for mock
    
    lua_State* luaL_newstate() { return &mockLuaState; }
    void luaL_openlibs(lua_State* L) {}
    int luaL_loadstring(lua_State* L, const char* s) { return 0; }
    int lua_pcall(lua_State* L, int nargs, int nresults, int errfunc) { return 0; }
    void lua_close(lua_State* L) {}
    int lua_gettop(lua_State* L) { return 0; }
    const char* lua_tostring(lua_State* L, int index) { return ""; }
    void lua_pushstring(lua_State* L, const char* s) {}
    void lua_pushnumber(lua_State* L, double n) {}
    void lua_pushboolean(lua_State* L, int b) {}
    double lua_tonumber(lua_State* L, int index) { return 0.0; }
    int lua_toboolean(lua_State* L, int index) { return 0; }
    void lua_register(lua_State* L, const char* name, int (*func)(lua_State*)) {}
    int luaL_register(lua_State* L, const char* libname, const struct luaL_Reg* l) { return 0; }
    void lua_pop(lua_State* L, int n) {}
    void lua_pushnil(lua_State* L) {}
    struct luaL_Reg;
}

// Global instance
ScriptEngine* g_ScriptEngine = nullptr;

ScriptEngine::ScriptEngine() : m_luaState(nullptr), m_initialized(false) {
    g_ScriptEngine = this;
}

ScriptEngine::~ScriptEngine() {
    Shutdown();
    g_ScriptEngine = nullptr;
}

bool ScriptEngine::Initialize() {
    if (m_initialized) return true;
    
    // Initialize Lua state (mock implementation)
    m_luaState = luaL_newstate();
    if (!m_luaState) {
        return false;
    }
    
    // Open standard libraries (mock - does nothing)
    luaL_openlibs(m_luaState);
    
    // Setup our custom API
    SetupLuaAPI();
    
    m_initialized = true;
    return true;
}

void ScriptEngine::Shutdown() {
    if (m_luaState) {
        lua_close(m_luaState);
        m_luaState = nullptr;
    }
    m_initialized = false;
}

ScriptResult ScriptEngine::ExecuteScript(const std::string& code, const std::string& language) {
    ScriptResult result;
    result.success = false;
    result.executionTime = 0.0;
    
    if (!m_initialized || !m_luaState) {
        result.error = "Script engine not initialized";
        return result;
    }
    
    auto startTime = std::chrono::high_resolution_clock::now();
    
    if (language == "lua" || language.empty()) {
        // For now, simulate successful execution since we're using mock Lua
        // In a real implementation, this would execute actual Lua code
        
        // Simulate some execution time
        std::this_thread::sleep_for(std::chrono::milliseconds(10));
        
        result.success = true;
        result.output = "Script executed successfully (Mock Lua Engine)";
        
        // Log the script execution for demonstration
        if (m_logCallback) {
            m_logCallback("Executing Lua script: " + code.substr(0, 50) + (code.length() > 50 ? "..." : ""));
        }
    }
    else {
        result.error = "Unsupported language: " + language;
        return result;
    }
    
    auto endTime = std::chrono::high_resolution_clock::now();
    result.executionTime = std::chrono::duration<double, std::milli>(endTime - startTime).count();
    
    return result;
}

ScriptResult ScriptEngine::ExecuteFile(const std::string& filepath) {
    ScriptResult result;
    result.success = false;
    
    std::ifstream file(filepath);
    if (!file.is_open()) {
        result.error = "Could not open file: " + filepath;
        return result;
    }
    
    std::stringstream buffer;
    buffer << file.rdbuf();
    std::string code = buffer.str();
    
    return ExecuteScript(code);
}

bool ScriptEngine::WriteMemory(uintptr_t address, const void* data, size_t size) {
    HANDLE process = GetCurrentProcess();
    SIZE_T bytesWritten;
    
    // Remove write protection temporarily
    DWORD oldProtect;
    if (!VirtualProtect((LPVOID)address, size, PAGE_EXECUTE_READWRITE, &oldProtect)) {
        return false;
    }
    
    bool success = WriteProcessMemory(process, (LPVOID)address, data, size, &bytesWritten) != 0;
    
    // Restore protection
    VirtualProtect((LPVOID)address, size, oldProtect, &oldProtect);
    
    return success && bytesWritten == size;
}

bool ScriptEngine::ReadMemory(uintptr_t address, void* buffer, size_t size) {
    HANDLE process = GetCurrentProcess();
    SIZE_T bytesRead;
    
    return ReadProcessMemory(process, (LPVOID)address, buffer, size, &bytesRead) != 0 && bytesRead == size;
}

uintptr_t ScriptEngine::FindPattern(const std::string& pattern, const std::string& mask) {
    MODULEINFO modInfo;
    if (!GetModuleInformation(GetCurrentProcess(), GetModuleHandle(nullptr), &modInfo, sizeof(modInfo))) {
        return 0;
    }
    
    uintptr_t base = (uintptr_t)modInfo.lpBaseOfDll;
    size_t size = modInfo.SizeOfImage;
    
    for (size_t i = 0; i < size - pattern.length(); ++i) {
        bool found = true;
        for (size_t j = 0; j < pattern.length(); ++j) {
            if (mask[j] == 'x' && *(char*)(base + i + j) != pattern[j]) {
                found = false;
                break;
            }
        }
        if (found) {
            return base + i;
        }
    }
    
    return 0;
}

void ScriptEngine::HookGameFunction(uintptr_t address, void* detour, void** original) {
    if (m_hooks.find(address) != m_hooks.end()) {
        return; // Already hooked
    }
    
    // Create a simple trampoline hook
    uint8_t originalBytes[5];
    if (ReadMemory(address, originalBytes, 5)) {
        m_hooks[address] = malloc(5);
        memcpy(m_hooks[address], originalBytes, 5);
        
        // Create jump to detour
        uint8_t jump[5] = { 0xE9, 0x00, 0x00, 0x00, 0x00 };
        uintptr_t relativeAddr = (uintptr_t)detour - address - 5;
        memcpy(&jump[1], &relativeAddr, 4);
        
        WriteMemory(address, jump, 5);
    }
}

void ScriptEngine::UnhookGameFunction(uintptr_t address) {
    auto it = m_hooks.find(address);
    if (it != m_hooks.end()) {
        WriteMemory(address, it->second, 5);
        free(it->second);
        m_hooks.erase(it);
    }
}

void ScriptEngine::SetupLuaAPI() {
    if (!m_luaState) return;
    
    // Register API functions
    lua_register(m_luaState, "log", Lua_Log);
    lua_register(m_luaState, "drawRect", Lua_DrawRect);
    lua_register(m_luaState, "drawText", Lua_DrawText);
    lua_register(m_luaState, "keyPressed", Lua_KeyPressed);
    lua_register(m_luaState, "teleport", Lua_Teleport);
    lua_register(m_luaState, "setPosition", Lua_SetPosition);
    lua_register(m_luaState, "getPosition", Lua_GetPosition);
    lua_register(m_luaState, "writeMemory", Lua_WriteMemory);
    lua_register(m_luaState, "readMemory", Lua_ReadMemory);
    lua_register(m_luaState, "findPattern", Lua_FindPattern);
    lua_register(m_luaState, "getFPS", Lua_GetFPS);
    lua_register(m_luaState, "getPing", Lua_GetPing);
}

// Lua API implementations
int ScriptEngine::Lua_Log(lua_State* L) {
    if (g_ScriptEngine && g_ScriptEngine->m_logCallback) {
        const char* message = lua_tostring(L, 1);
        g_ScriptEngine->m_logCallback(message);
    }
    return 0;
}

int ScriptEngine::Lua_DrawRect(lua_State* L) {
    if (g_ScriptEngine && g_ScriptEngine->m_drawRectCallback) {
        int x = (int)lua_tonumber(L, 1);
        int y = (int)lua_tonumber(L, 2);
        int w = (int)lua_tonumber(L, 3);
        int h = (int)lua_tonumber(L, 4);
        g_ScriptEngine->m_drawRectCallback(x, y, w, h);
    }
    return 0;
}

int ScriptEngine::Lua_DrawText(lua_State* L) {
    if (g_ScriptEngine && g_ScriptEngine->m_drawTextCallback) {
        const char* text = lua_tostring(L, 1);
        int x = (int)lua_tonumber(L, 2);
        int y = (int)lua_tonumber(L, 3);
        int r = (int)lua_tonumber(L, 4);
        int g = (int)lua_tonumber(L, 5);
        g_ScriptEngine->m_drawTextCallback(text, x, y, r, g);
    }
    return 0;
}

int ScriptEngine::Lua_KeyPressed(lua_State* L) {
    if (g_ScriptEngine && g_ScriptEngine->m_keyPressedCallback) {
        const char* key = lua_tostring(L, 1);
        bool pressed = g_ScriptEngine->m_keyPressedCallback(key);
        lua_pushboolean(L, pressed);
        return 1;
    }
    lua_pushboolean(L, 0);
    return 1;
}

int ScriptEngine::Lua_Teleport(lua_State* L) {
    if (g_ScriptEngine && g_ScriptEngine->m_teleportCallback) {
        g_ScriptEngine->m_teleportCallback();
    }
    return 0;
}

int ScriptEngine::Lua_SetPosition(lua_State* L) {
    if (g_ScriptEngine && g_ScriptEngine->m_setPositionCallback) {
        float x = (float)lua_tonumber(L, 1);
        float y = (float)lua_tonumber(L, 2);
        float z = (float)lua_tonumber(L, 3);
        g_ScriptEngine->m_setPositionCallback(x, y, z);
    }
    return 0;
}

int ScriptEngine::Lua_GetPosition(lua_State* L) {
    // This would need to be implemented with proper position getter
    lua_pushnumber(L, 0.0);
    lua_pushnumber(L, 0.0);
    lua_pushnumber(L, 0.0);
    return 3;
}

int ScriptEngine::Lua_WriteMemory(lua_State* L) {
    if (g_ScriptEngine) {
        uintptr_t address = (uintptr_t)lua_tonumber(L, 1);
        double value = lua_tonumber(L, 2);
        bool success = g_ScriptEngine->WriteMemory(address, &value, sizeof(value));
        lua_pushboolean(L, success);
        return 1;
    }
    lua_pushboolean(L, 0);
    return 1;
}

int ScriptEngine::Lua_ReadMemory(lua_State* L) {
    if (g_ScriptEngine) {
        uintptr_t address = (uintptr_t)lua_tonumber(L, 1);
        double value = 0.0;
        bool success = g_ScriptEngine->ReadMemory(address, &value, sizeof(value));
        if (success) {
            lua_pushnumber(L, value);
        } else {
            lua_pushnil(L);
        }
        return 1;
    }
    lua_pushnil(L);
    return 1;
}

int ScriptEngine::Lua_FindPattern(lua_State* L) {
    if (g_ScriptEngine) {
        const char* pattern = lua_tostring(L, 1);
        const char* mask = lua_tostring(L, 2);
        uintptr_t result = g_ScriptEngine->FindPattern(pattern, mask);
        lua_pushnumber(L, (double)result);
        return 1;
    }
    lua_pushnumber(L, 0.0);
    return 1;
}

int ScriptEngine::Lua_GetFPS(lua_State* L) {
    // Get FPS from the existing FPS system
    extern float GetCurrentFPS();
    lua_pushnumber(L, GetCurrentFPS());
    return 1;
}

int ScriptEngine::Lua_GetPing(lua_State* L) {
    // Get ping from the existing ping system
    extern uint32_t GetCurrentPing();
    lua_pushnumber(L, GetCurrentPing());
    return 1;
}

std::string ScriptEngine::GetScriptsDirectory() {
    return "scripts/";
}

void ScriptEngine::LoadScripts() {
    // Implementation for loading scripts from directory
}

void ScriptEngine::SaveScript(const std::string& name, const std::string& code) {
    std::string path = GetScriptsDirectory() + name + ".lua";
    std::ofstream file(path);
    if (file.is_open()) {
        file << code;
        file.close();
    }
}

std::vector<std::string> ScriptEngine::GetAvailableScripts() {
    std::vector<std::string> scripts;
    // Implementation for scanning scripts directory
    return scripts;
}
