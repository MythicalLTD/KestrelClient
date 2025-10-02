#pragma once
#include <string>
#include <vector>
#include <functional>
#include <map>

// Forward declarations
struct lua_State;

// Script execution result
struct ScriptResult {
    bool success;
    std::string output;
    std::string error;
    double executionTime;
};

// Game API function types
typedef std::function<void(const std::string&)> LogFunction;
typedef std::function<void(int, int, int, int)> DrawRectFunction;
typedef std::function<void(const std::string&, int, int, int, int)> DrawTextFunction;
typedef std::function<bool(const std::string&)> KeyPressedFunction;
typedef std::function<void()> TeleportFunction;
typedef std::function<void(float, float, float)> SetPositionFunction;
typedef std::function<void(float, float, float)> GetPositionFunction;

// Script Engine class
class ScriptEngine {
public:
    ScriptEngine();
    ~ScriptEngine();

    // Core functionality
    bool Initialize();
    void Shutdown();
    
    // Script execution
    ScriptResult ExecuteScript(const std::string& code, const std::string& language = "lua");
    ScriptResult ExecuteFile(const std::string& filepath);
    
    // Memory manipulation
    bool WriteMemory(uintptr_t address, const void* data, size_t size);
    bool ReadMemory(uintptr_t address, void* buffer, size_t size);
    uintptr_t FindPattern(const std::string& pattern, const std::string& mask);
    
    // Game API registration
    void RegisterGameAPI();
    
    // Script management
    void LoadScripts();
    void SaveScript(const std::string& name, const std::string& code);
    std::vector<std::string> GetAvailableScripts();
    
    // Lua specific
    lua_State* GetLuaState() const { return m_luaState; }
    
    // Game hooks and functions
    void HookGameFunction(uintptr_t address, void* detour, void** original);
    void UnhookGameFunction(uintptr_t address);
    
    // API Functions
    void SetLogCallback(LogFunction callback) { m_logCallback = callback; }
    void SetDrawRectCallback(DrawRectFunction callback) { m_drawRectCallback = callback; }
    void SetDrawTextCallback(DrawTextFunction callback) { m_drawTextCallback = callback; }
    void SetKeyPressedCallback(KeyPressedFunction callback) { m_keyPressedCallback = callback; }
    void SetTeleportCallback(TeleportFunction callback) { m_teleportCallback = callback; }
    void SetPositionCallbacks(SetPositionFunction setPos, GetPositionFunction getPos) { 
        m_setPositionCallback = setPos; 
        m_getPositionCallback = getPos; 
    }

private:
    lua_State* m_luaState;
    bool m_initialized;
    
    // Callbacks for game API
    LogFunction m_logCallback;
    DrawRectFunction m_drawRectCallback;
    DrawTextFunction m_drawTextCallback;
    KeyPressedFunction m_keyPressedCallback;
    TeleportFunction m_teleportCallback;
    SetPositionFunction m_setPositionCallback;
    GetPositionFunction m_getPositionCallback;
    
    // Memory management
    std::map<uintptr_t, void*> m_hooks;
    
    // Lua API functions
    static int Lua_Log(lua_State* L);
    static int Lua_DrawRect(lua_State* L);
    static int Lua_DrawText(lua_State* L);
    static int Lua_KeyPressed(lua_State* L);
    static int Lua_Teleport(lua_State* L);
    static int Lua_SetPosition(lua_State* L);
    static int Lua_GetPosition(lua_State* L);
    static int Lua_WriteMemory(lua_State* L);
    static int Lua_ReadMemory(lua_State* L);
    static int Lua_FindPattern(lua_State* L);
    static int Lua_GetFPS(lua_State* L);
    static int Lua_GetPing(lua_State* L);
    
    // Helper functions
    void SetupLuaAPI();
    std::string GetScriptsDirectory();
};

// Global script engine instance
extern ScriptEngine* g_ScriptEngine;
