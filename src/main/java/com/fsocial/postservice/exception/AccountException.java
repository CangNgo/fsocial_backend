package com.fsocial.postservice.exception;

import com.fsocial.postservice.enums.AccountErrorCode;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AccountException extends RuntimeException {
    final AccountErrorCode code;

    public AccountException(AccountErrorCode code) {
        super(code.getMessage());
        this.code = code;
    }
}
