package com.fsocial.postservice.enums;

import java.util.Locale;

public enum MediaType {
    IMAGE("image"),
    VIDEO("video");

    private final String value;

    MediaType(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public static MediaType fromValue(String value) {
        if (value == null) {
            return null;
        }
        return valueOf(value.toUpperCase(Locale.ROOT));
    }
}
