package com.fsocial.postservice.exception;

import com.fsocial.postservice.enums.CodeEnum;
import lombok.Getter;

@Getter
public class AppException extends RuntimeException {
    private final CodeEnum errorCode;

    public AppException(CodeEnum errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public AppException(String message, CodeEnum errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
}
