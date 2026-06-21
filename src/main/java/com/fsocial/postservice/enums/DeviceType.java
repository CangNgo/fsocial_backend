package com.fsocial.postservice.enums;

public enum DeviceType {
        ANDROID9("ANDROID"), IOS("IOS"), WEB("WEB");
        final String name;
        DeviceType(String name) {
                this.name  = name;
        }
}
