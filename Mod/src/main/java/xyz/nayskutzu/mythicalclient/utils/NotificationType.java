package xyz.nayskutzu.mythicalclient.utils;

public enum NotificationType {
    CHAT(1, "Chat"),
    ACTION_BAR(2, "ActionBar"),
    TITLE_BAR(3, "TitleBar");

    private final int id;
    private final String displayName;

    NotificationType(int id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    public int getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static NotificationType fromId(int id) {
        for (NotificationType type : values()) {
            if (type.id == id) {
                return type;
            }
        }
        return CHAT; // Default fallback
    }

    public static NotificationType fromDisplayName(String displayName) {
        for (NotificationType type : values()) {
            if (type.displayName.equalsIgnoreCase(displayName)) {
                return type;
            }
        }
        return CHAT; // Default fallback
    }
} 