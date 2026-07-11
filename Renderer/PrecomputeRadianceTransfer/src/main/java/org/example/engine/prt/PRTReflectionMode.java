package org.example.engine.prt;

import java.util.Locale;

public enum PRTReflectionMode {
    DIFFUSE("diffuse"),
    GLOSSY("glossy_v2"),
    GLOSSY_MATRIX("glossy_matrix_v2");

    private final String cacheKey;

    PRTReflectionMode(String cacheKey) {
        this.cacheKey = cacheKey;
    }

    public String cacheKey() {
        return cacheKey;
    }

    public static PRTReflectionMode parse(String value) {
        if (value == null || value.trim().isEmpty()) {
            return DIFFUSE;
        }

        String normalized = value.trim().toUpperCase(Locale.ROOT);
        for (PRTReflectionMode mode : values()) {
            if (mode.name().equals(normalized) || mode.cacheKey.equalsIgnoreCase(value.trim())) {
                return mode;
            }
        }

        throw new IllegalArgumentException("[PRTReflectionMode] Unknown reflection mode: " + value);
    }
}
