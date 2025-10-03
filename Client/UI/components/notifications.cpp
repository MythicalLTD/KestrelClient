#include "notifications.h"
#include "../../include/imgui.h"
#include <algorithm>
#include <chrono>

// Notification storage
static std::vector<Notification> notifications;

void RenderNotifications() {
    if (notifications.empty()) return;
    
    auto currentTime = std::chrono::steady_clock::now();
    
    // Remove expired notifications
    notifications.erase(
        std::remove_if(notifications.begin(), notifications.end(),
            [currentTime](const Notification& notif) {
                auto elapsed = std::chrono::duration_cast<std::chrono::milliseconds>(currentTime - notif.createdTime).count() / 1000.0f;
                return elapsed >= notif.duration;
            }),
        notifications.end()
    );
    
    if (notifications.empty()) return;
    
    // Set up notification area (bottom-right corner, closer to borders)
    ImGuiViewport* viewport = ImGui::GetMainViewport();
    float startX = viewport->Size.x - 300; // Even closer to right edge
    float startY = viewport->Size.y - 150; // Start from bottom, accounting for notification height
    
    // Render notifications from top to bottom
    for (size_t i = 0; i < notifications.size(); ++i) {
        Notification& notif = notifications[i];
        
        // Calculate position for this notification (stack upward from bottom)
        float yPos = startY - (i * 65); // 65px spacing, stack upward from bottom
        ImGui::SetNextWindowPos(ImVec2(startX, yPos), ImGuiCond_Always);
        ImGui::SetNextWindowBgAlpha(0.65f); // Even more transparent
        
        // Determine colors based on type
        ImVec4 bgColor, borderColor, iconColor;
        const char* icon = "";
        
        switch (notif.type) {
            case NotificationType::INFO:
                bgColor = ImVec4(0.2f, 0.6f, 1.0f, 0.6f); // Blue - even more transparent
                borderColor = ImVec4(0.1f, 0.5f, 0.9f, 0.7f);
                iconColor = ImVec4(0.9f, 0.95f, 1.0f, 0.85f);
                icon = "ℹ";
                break;
            case NotificationType::SUCCESS:
                bgColor = ImVec4(0.2f, 0.8f, 0.3f, 0.6f); // Green - even more transparent
                borderColor = ImVec4(0.1f, 0.7f, 0.2f, 0.7f);
                iconColor = ImVec4(0.9f, 1.0f, 0.9f, 0.85f);
                icon = "✓";
                break;
            case NotificationType::WARNING:
                bgColor = ImVec4(1.0f, 0.7f, 0.2f, 0.6f); // Orange - even more transparent
                borderColor = ImVec4(0.9f, 0.6f, 0.1f, 0.7f);
                iconColor = ImVec4(1.0f, 0.95f, 0.8f, 0.85f);
                icon = "⚠";
                break;
            case NotificationType::ALERT:
                bgColor = ImVec4(0.9f, 0.2f, 0.2f, 0.6f); // Red - even more transparent
                borderColor = ImVec4(0.8f, 0.1f, 0.1f, 0.7f);
                iconColor = ImVec4(1.0f, 0.9f, 0.9f, 0.85f);
                icon = "🚨";
                break;
        }
        
        // Calculate fade out effect (last 0.5 seconds)
        auto elapsed = std::chrono::duration_cast<std::chrono::milliseconds>(currentTime - notif.createdTime).count() / 1000.0f;
        float fadeAlpha = 1.0f;
        if (elapsed > notif.duration - 0.5f) {
            fadeAlpha = (notif.duration - elapsed) / 0.5f;
            fadeAlpha = std::max(0.0f, fadeAlpha);
        }
        
        // Apply fade to colors (maintain transparency)
        bgColor.w *= fadeAlpha;
        borderColor.w *= fadeAlpha;
        iconColor.w *= fadeAlpha;
        
        // Window flags for notification behavior
        ImGuiWindowFlags notifFlags = ImGuiWindowFlags_NoDecoration | 
                                     ImGuiWindowFlags_AlwaysAutoResize | 
                                     ImGuiWindowFlags_NoFocusOnAppearing | 
                                     ImGuiWindowFlags_NoNav |
                                     ImGuiWindowFlags_NoMove |
                                     ImGuiWindowFlags_NoSavedSettings |
                                     ImGuiWindowFlags_NoInputs;
        
        // Set custom colors with enhanced transparency
        ImGui::PushStyleColor(ImGuiCol_WindowBg, bgColor);
        ImGui::PushStyleColor(ImGuiCol_Border, borderColor);
        ImGui::PushStyleVar(ImGuiStyleVar_WindowBorderSize, 1.0f); // Even thinner border
        ImGui::PushStyleVar(ImGuiStyleVar_WindowRounding, 5.0f); // More subtle rounded corners
        ImGui::PushStyleVar(ImGuiStyleVar_WindowPadding, ImVec2(10, 8)); // Even tighter padding
        
        // Create notification window
        std::string windowId = "Notification_" + std::to_string(i);
        if (ImGui::Begin(windowId.c_str(), nullptr, notifFlags)) {
            ImGui::PushStyleColor(ImGuiCol_Text, iconColor);
            
            // Icon and message layout
            ImGui::PushFont(nullptr); // Use default font
            ImGui::Text("%s", icon);
            ImGui::PopFont();
            
            ImGui::SameLine();
            ImGui::SetCursorPosY(ImGui::GetCursorPosY() - 2); // Slight vertical alignment
            
            // Message text
            ImGui::PushTextWrapPos(ImGui::GetCursorPosX() + 240); // Even narrower for cleaner look
            ImGui::TextWrapped("%s", notif.message.c_str());
            ImGui::PopTextWrapPos();
            
            ImGui::PopStyleColor();
            
            // Progress bar showing time remaining
            ImGui::Spacing();
            float progress = 1.0f - (elapsed / notif.duration);
            progress = std::max(0.0f, std::min(1.0f, progress));
            
            ImGui::PushStyleColor(ImGuiCol_PlotHistogram, ImVec4(1.0f, 1.0f, 1.0f, 0.5f * fadeAlpha));
            ImGui::ProgressBar(progress, ImVec2(-1, 2), ""); // Thinner progress bar
            ImGui::PopStyleColor();
        }
        ImGui::End();
        
        // Pop style colors and vars
        ImGui::PopStyleVar(3);
        ImGui::PopStyleColor(2);
    }
}

void ShowNotification(const std::string& message, NotificationType type, float duration) {
    // Limit number of notifications to prevent screen clutter
    if (notifications.size() >= 5) {
        notifications.erase(notifications.begin()); // Remove oldest
    }
    
    notifications.emplace_back(message, type, duration);
}

void ShowInfoNotification(const std::string& message, float duration) {
    ShowNotification(message, NotificationType::INFO, duration);
}

void ShowSuccessNotification(const std::string& message, float duration) {
    ShowNotification(message, NotificationType::SUCCESS, duration);
}

void ShowWarningNotification(const std::string& message, float duration) {
    ShowNotification(message, NotificationType::WARNING, duration);
}

void ShowAlertNotification(const std::string& message, float duration) {
    ShowNotification(message, NotificationType::ALERT, duration);
}

void ClearAllNotifications() {
    notifications.clear();
}
