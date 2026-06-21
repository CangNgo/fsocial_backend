package com.fsocial.postservice.enums;

import org.springframework.http.HttpStatusCode;

public interface CodeEnum {
    int getCode();
    String getMessage();
    HttpStatusCode getHttpStatusCode();
}
