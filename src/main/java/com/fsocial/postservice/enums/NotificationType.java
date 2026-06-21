package com.fsocial.postservice.enums;

public enum NotificationType {
    LIKE("LIKE"), COMMENT("COMMENT"), REPLY("REPLY"), FOLLOW("FOLLOW"), MENTION("MENTION"),
    MESSAGE("MESSAGE"), SYSTEM("SYSTEM"), APPOINTMENT("APPOINTMENT"), RESULT("RESULT"), PAYMENT("PAYMENT");
    final String name;
    NotificationType(String name) {
        this.name = name;
    }
}