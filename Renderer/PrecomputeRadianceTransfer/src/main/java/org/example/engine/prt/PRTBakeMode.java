package org.example.engine.prt;

import java.util.Locale;

public enum PRTBakeMode {
    UNSHADOW("unshadow"),
    SHADOW("shadow");

    private final String cacheKey;

    PRTBakeMode(String cacheKey) {
        this.cacheKey = cacheKey;
    }

    public String cacheKey() {
        return cacheKey;
    }

    public static PRTBakeMode parse(String value) {
        if (value == null || value.trim().isEmpty()) {
            return UNSHADOW;
        }

        String normalized = value.trim().toUpperCase(Locale.ROOT);
        for (PRTBakeMode mode : values()) {
            if (mode.name().equals(normalized) || mode.cacheKey.equalsIgnoreCase(value.trim())) {
                return mode;
            }
        }

        throw new IllegalArgumentException("[PRTBakeMode] Unknown bake mode: " + value);
    }
}
