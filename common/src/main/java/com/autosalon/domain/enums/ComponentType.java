package com.autosalon.domain.enums;

public enum ComponentType {
    WHEELS("Wheels"),
    TRANSMISSION("Transmission"),
    STEERING_WHEEL("Steering wheel"),
    INTERIOR("Interior");

    private final String displayName;

    ComponentType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
