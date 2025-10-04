#include "pch.h"
#include "logger.h"
#include <iomanip>
#include <codecvt>
#include <locale>

// Static member definitions
std::unique_ptr<Logger> Logger::instance = nullptr;
std::mutex Logger::instanceMutex;

Logger::Logger() : minLogLevel(LOG_INFO), initialized(false), shuttingDown(false) {
    // Constructor is private, called only by GetInstance()
}

Logger::~Logger() {
    Shutdown();
}

Logger& Logger::GetInstance() {
    std::lock_guard<std::mutex> lock(instanceMutex);
    if (!instance) {
        instance = std::unique_ptr<Logger>(new Logger());
    }
    return *instance;
}

bool Logger::Initialize() {
    return Initialize(LOG_INFO);
}

bool Logger::Initialize(LogLevel minLevel) {
    try {
        std::lock_guard<std::mutex> lock(logMutex);
        
        if (initialized) {
            return true; // Already initialized
        }
        
        minLogLevel = minLevel;
        
        // Get user AppData path
        std::string appDataPath = GetUserAppDataPath();
        if (appDataPath.empty()) {
            return false;
        }
        
        // Ensure log directory exists
        if (!EnsureLogDirectoryExists()) {
            return false;
        }
        
        // Create log file path with timestamp
        auto now = std::chrono::system_clock::now();
        auto time_t = std::chrono::system_clock::to_time_t(now);
        std::tm tm;
        localtime_s(&tm, &time_t);
        
        std::stringstream ss;
        ss << appDataPath << "\\logs\\kestrelclient_"
           << std::put_time(&tm, "%Y%m%d_%H%M%S") << ".log";
        
        logFilePath = ss.str();
        
        // Open log file
        logFile.open(logFilePath, std::ios::out | std::ios::app);
        if (!logFile.is_open()) {
            return false;
        }
        
        initialized = true;
        
        // Log initial message (but don't use the Log methods to avoid recursion)
        std::string timestamp = GetCurrentTimestamp();
        std::string logEntry = "[" + timestamp + "] [INFO] Logger initialized successfully";
        logFile << logEntry << std::endl;
        logEntry = "[" + timestamp + "] [INFO] Log file: " + logFilePath;
        logFile << logEntry << std::endl;
        logFile.flush();
        
        return true;
    } catch (...) {
        // If initialization fails, return false but don't crash
        return false;
    }
}

void Logger::Log(LogLevel level, const std::string& message) {
    // Early return if not initialized, shutting down, or level too low
    if (!initialized || shuttingDown || level < minLogLevel) {
        return;
    }
    
    // Use try-catch to prevent crashes during logging
    try {
        std::lock_guard<std::mutex> lock(logMutex);
        
        // Double-check initialization after acquiring lock
        if (!initialized) {
            return;
        }
        
        std::string timestamp = GetCurrentTimestamp();
        std::string levelStr = GetLogLevelString(level);
        
        std::string logEntry = "[" + timestamp + "] [" + levelStr + "] " + message;
        
        // Write to file with error checking
        if (logFile.is_open() && logFile.good()) {
            logFile << logEntry << std::endl;
            logFile.flush();
            
            // Check if write was successful
            if (logFile.fail()) {
                // If write failed, try to reopen the file
                logFile.close();
                logFile.open(logFilePath, std::ios::out | std::ios::app);
            }
        }
        
        // Also output to console if available (for debugging)
        #ifdef _DEBUG
        try {
            std::cout << logEntry << std::endl;
        } catch (...) {
            // Ignore console output errors
        }
        #endif
    } catch (...) {
        // If logging fails, just silently ignore to prevent crashes
        // This is critical for DLL environments where logging should never crash the application
    }
}

void Logger::LogDebug(const std::string& message) {
    Log(LOG_DEBUG, message);
}

void Logger::LogInfo(const std::string& message) {
    Log(LOG_INFO, message);
}

void Logger::LogWarning(const std::string& message) {
    Log(LOG_WARNING, message);
}

void Logger::LogError(const std::string& message) {
    Log(LOG_ERROR, message);
}

void Logger::SetMinLogLevel(LogLevel level) {
    std::lock_guard<std::mutex> lock(logMutex);
    minLogLevel = level;
}

void Logger::Shutdown() {
    try {
        std::lock_guard<std::mutex> lock(logMutex);
        
        // Mark as shutting down to prevent new log entries
        shuttingDown = true;
        
        if (initialized) {
            // Write shutdown message directly without using Log methods
            try {
                std::string timestamp = GetCurrentTimestamp();
                std::string logEntry = "[" + timestamp + "] [INFO] Logger shutting down";
                if (logFile.is_open() && logFile.good()) {
                    logFile << logEntry << std::endl;
                    logFile.flush();
                }
            } catch (...) {
                // Ignore errors during shutdown logging
            }
            
            if (logFile.is_open()) {
                logFile.close();
            }
            initialized = false;
        }
    } catch (...) {
        // If shutdown fails, just mark as not initialized
        initialized = false;
        shuttingDown = false;
    }
}

std::string Logger::GetCurrentTimestamp() {
    try {
        auto now = std::chrono::system_clock::now();
        auto time_t = std::chrono::system_clock::to_time_t(now);
        auto ms = std::chrono::duration_cast<std::chrono::milliseconds>(
            now.time_since_epoch()) % 1000;
        
        std::tm tm;
        if (localtime_s(&tm, &time_t) != 0) {
            // If localtime_s fails, return a simple timestamp
            return "1970-01-01 00:00:00.000";
        }
        
        std::stringstream ss;
        ss << std::put_time(&tm, "%Y-%m-%d %H:%M:%S");
        ss << '.' << std::setfill('0') << std::setw(3) << ms.count();
        
        return ss.str();
    } catch (...) {
        // If timestamp generation fails, return a fallback
        return "1970-01-01 00:00:00.000";
    }
}

std::string Logger::GetLogLevelString(LogLevel level) {
    switch (level) {
        case LOG_DEBUG:   return "DEBUG";
        case LOG_INFO:    return "INFO";
        case LOG_WARNING: return "WARNING";
        case LOG_ERROR:   return "ERROR";
        default:          return "UNKNOWN";
    }
}

std::string Logger::GetUserAppDataPath() {
    try {
        char* appDataPath = nullptr;
        HRESULT hr = SHGetKnownFolderPath(FOLDERID_RoamingAppData, 0, NULL, (PWSTR*)&appDataPath);
        
        if (SUCCEEDED(hr) && appDataPath) {
            // Convert wide char to string
            int size = WideCharToMultiByte(CP_UTF8, 0, (LPCWCH)appDataPath, -1, NULL, 0, NULL, NULL);
            if (size > 0) {
                std::string path(size - 1, 0);
                WideCharToMultiByte(CP_UTF8, 0, (LPCWCH)appDataPath, -1, &path[0], size, NULL, NULL);
                
                // Free the allocated memory
                CoTaskMemFree(appDataPath);
                
                // Append .kestrelclient
                path += "\\.kestrelclient";
                
                return path;
            }
            
            // Free the allocated memory if conversion failed
            CoTaskMemFree(appDataPath);
        }
        
        // Fallback to a simple path if SHGetKnownFolderPath fails
        char* username = nullptr;
        size_t len = 0;
        if (_dupenv_s(&username, &len, "USERNAME") == 0 && username) {
            std::string path = "C:\\Users\\" + std::string(username) + "\\AppData\\Roaming\\.kestrelclient";
            free(username);
            return path;
        }
        
        // Final fallback
        return "C:\\Users\\Default\\AppData\\Roaming\\.kestrelclient";
    } catch (...) {
        // If everything fails, return a safe fallback
        return "C:\\Users\\Default\\AppData\\Roaming\\.kestrelclient";
    }
}

bool Logger::EnsureLogDirectoryExists() {
    try {
        std::string logDir = GetUserAppDataPath() + "\\logs";
        
        // Create directory using Windows API
        std::wstring wLogDir(logDir.begin(), logDir.end());
        if (CreateDirectoryW(wLogDir.c_str(), NULL) || GetLastError() == ERROR_ALREADY_EXISTS) {
            return true;
        }
        
        // If CreateDirectoryW failed, try to check if directory exists
        DWORD attrs = GetFileAttributesW(wLogDir.c_str());
        return (attrs != INVALID_FILE_ATTRIBUTES && (attrs & FILE_ATTRIBUTE_DIRECTORY));
    } catch (...) {
        return false;
    }
}

// Safe initialization function for early startup
void InitializeLoggerSafe() {
    try {
        Logger::GetInstance().Initialize(LOG_DEBUG);
    } catch (...) {
        // Ignore all exceptions during initialization
        // This prevents crashes during early startup
        // The logger will remain uninitialized, but the application won't crash
    }
}
