#pragma once

#include <string>
#include <fstream>
#include <mutex>
#include <memory>
#include <chrono>
#include <sstream>
#include <iostream>
#include <windows.h>
#include <shlobj.h>

enum LogLevel {
    LOG_DEBUG = 0,
    LOG_INFO = 1,
    LOG_WARNING = 2,
    LOG_ERROR = 3
};

class Logger {
private:
    static std::unique_ptr<Logger> instance;
    static std::mutex instanceMutex;
    
    std::string logFilePath;
    std::ofstream logFile;
    std::mutex logMutex;
    LogLevel minLogLevel;
    bool initialized;
    bool shuttingDown;
    
    Logger();
    
public:
    ~Logger();
    
private:
    
    std::string GetCurrentTimestamp();
    std::string GetLogLevelString(LogLevel level);
    std::string GetUserAppDataPath();
    bool EnsureLogDirectoryExists();
    
public:
    // Singleton pattern
    static Logger& GetInstance();
    
    // Initialize logger with default settings
    bool Initialize();
    
    // Initialize logger with custom log level
    bool Initialize(LogLevel minLevel);
    
    // Log messages
    void Log(LogLevel level, const std::string& message);
    void LogDebug(const std::string& message);
    void LogInfo(const std::string& message);
    void LogWarning(const std::string& message);
    void LogError(const std::string& message);
    
    // Set minimum log level
    void SetMinLogLevel(LogLevel level);
    
    // Check if logger is initialized
    bool IsInitialized() const { return initialized; }
    
    // Check if logger is safe to use (not shutting down)
    bool IsSafeToUse() const { return initialized && !shuttingDown; }
    
    // Shutdown logger
    void Shutdown();
    
    // Copy constructor and assignment operator are deleted (singleton)
    Logger(const Logger&) = delete;
    Logger& operator=(const Logger&) = delete;
};

// Safe convenience macros for logging with crash protection
#define LOG_DEBUG(msg) \
    do { \
        try { \
            if (Logger::GetInstance().IsSafeToUse()) { \
                Logger::GetInstance().LogDebug(msg); \
            } \
        } catch (...) { \
            /* Ignore logging errors to prevent crashes */ \
        } \
    } while(0)

#define LOG_INFO(msg) \
    do { \
        try { \
            if (Logger::GetInstance().IsSafeToUse()) { \
                Logger::GetInstance().LogInfo(msg); \
            } \
        } catch (...) { \
            /* Ignore logging errors to prevent crashes */ \
        } \
    } while(0)

#define LOG_WARNING(msg) \
    do { \
        try { \
            if (Logger::GetInstance().IsSafeToUse()) { \
                Logger::GetInstance().LogWarning(msg); \
            } \
        } catch (...) { \
            /* Ignore logging errors to prevent crashes */ \
        } \
    } while(0)

#define LOG_ERROR(msg) \
    do { \
        try { \
            if (Logger::GetInstance().IsSafeToUse()) { \
                Logger::GetInstance().LogError(msg); \
            } \
        } catch (...) { \
            /* Ignore logging errors to prevent crashes */ \
        } \
    } while(0)

// Formatted logging macros with crash protection
#define LOG_DEBUGF(format, ...) \
    do { \
        try { \
            if (Logger::GetInstance().IsSafeToUse()) { \
                char buffer[1024]; \
                snprintf(buffer, sizeof(buffer), format, __VA_ARGS__); \
                Logger::GetInstance().LogDebug(buffer); \
            } \
        } catch (...) { \
            /* Ignore logging errors to prevent crashes */ \
        } \
    } while(0)

#define LOG_INFOF(format, ...) \
    do { \
        try { \
            if (Logger::GetInstance().IsSafeToUse()) { \
                char buffer[1024]; \
                snprintf(buffer, sizeof(buffer), format, __VA_ARGS__); \
                Logger::GetInstance().LogInfo(buffer); \
            } \
        } catch (...) { \
            /* Ignore logging errors to prevent crashes */ \
        } \
    } while(0)

#define LOG_WARNINGF(format, ...) \
    do { \
        try { \
            if (Logger::GetInstance().IsSafeToUse()) { \
                char buffer[1024]; \
                snprintf(buffer, sizeof(buffer), format, __VA_ARGS__); \
                Logger::GetInstance().LogWarning(buffer); \
            } \
        } catch (...) { \
            /* Ignore logging errors to prevent crashes */ \
        } \
    } while(0)

#define LOG_ERRORF(format, ...) \
    do { \
        try { \
            if (Logger::GetInstance().IsSafeToUse()) { \
                char buffer[1024]; \
                snprintf(buffer, sizeof(buffer), format, __VA_ARGS__); \
                Logger::GetInstance().LogError(buffer); \
            } \
        } catch (...) { \
            /* Ignore logging errors to prevent crashes */ \
        } \
    } while(0)

// Safe initialization function for early startup
void InitializeLoggerSafe();
