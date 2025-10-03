#pragma once
#include <string>
#include <vector>
#include <chrono>

// Notification types
enum class NotificationType {
    INFO,
    SUCCESS,
    WARNING,
    ALERT
};

// Notification structure
struct Notification {
    std::string message;
    NotificationType type;
    std::chrono::steady_clock::time_point createdTime;
    float duration; // in seconds
    bool isVisible;
    
    Notification(const std::string& msg, NotificationType t, float dur = 3.0f) 
        : message(msg), type(t), createdTime(std::chrono::steady_clock::now()), duration(dur), isVisible(true) {}
};

// Function declarations
void RenderNotifications();
void ShowNotification(const std::string& message, NotificationType type, float duration = 3.0f);
void ShowInfoNotification(const std::string& message, float duration = 3.0f);
void ShowSuccessNotification(const std::string& message, float duration = 3.0f);
void ShowWarningNotification(const std::string& message, float duration = 3.0f);
void ShowAlertNotification(const std::string& message, float duration = 3.0f);
void ClearAllNotifications();
