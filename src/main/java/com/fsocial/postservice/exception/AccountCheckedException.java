package com.fsocial.postservice.exception;

import com.fsocial.postservice.enums.AccountErrorCode;
import lombok.Getter;

@Getter
public class AccountCheckedException extends Exception {
    private final AccountErrorCode status;

    public AccountCheckedException(AccountErrorCode status) {
        super(status.getMessage());
        this.status = status;
    }
}
