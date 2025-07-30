package Project.PENBOT.ChatAPI.Entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ChatRole {
    USER("user"),
    MODEL("model");

    private final String value;

    ChatRole(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static ChatRole forValue(String value) {
        for (ChatRole chatRole : ChatRole.values()) {
            if (chatRole.value.equalsIgnoreCase(value)) {
                return chatRole;
            }
        }
        throw new IllegalArgumentException("Unknown chatRole: " + value);
    }
}
