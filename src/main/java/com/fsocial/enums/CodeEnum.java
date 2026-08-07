package com.fsocial.enums;

import org.springframework.http.HttpStatusCode;

public interface CodeEnum {
    int getCode();
    String getMessage();
    HttpStatusCode getHttpStatusCode();
}
