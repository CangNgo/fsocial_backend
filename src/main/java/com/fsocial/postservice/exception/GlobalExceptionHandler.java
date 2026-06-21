package com.fsocial.postservice.exception;

import com.fsocial.postservice.dto.Response;
import com.fsocial.postservice.enums.AccountValidErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.Objects;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value = Exception.class)
    ResponseEntity<Response> handlingRuntimeException(RuntimeException exception) {

        return ResponseEntity.badRequest().body(Response.builder()
                .statusCode(StatusCode.UNCATEGORIZED_EXCEPTION.getCode())
                .message(exception.getMessage())
                .dateTime(LocalDateTime.now())
                .data(null)
                .build());
    }

    @ExceptionHandler(value = AccessDeniedException.class)
    ResponseEntity<Response> handlingAccessDeniedException(AccessDeniedException exception) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Response.builder()
                .statusCode(HttpStatus.FORBIDDEN.value())
                .message("Bạn không có quyền thực hiện hành động này")
                .dateTime(LocalDateTime.now())
                .build());
    }

    @ExceptionHandler(value = AccountException.class)
    ResponseEntity<Response> handleAccountException(AccountException exception) {
        var code = exception.getCode();
        return ResponseEntity.status(code.getHttpStatusCode())
                .body(Response.builder()
                        .statusCode(code.getCode())
                        .message(code.getMessage())
                        .dateTime(LocalDateTime.now())
                        .build());
    }

    @ExceptionHandler(value = AccountCheckedException.class)
    ResponseEntity<Response> handleAccountCheckedException(AccountCheckedException exception) {
        var code = exception.getStatus();
        return ResponseEntity.status(code.getHttpStatusCode())
                .body(Response.builder()
                        .statusCode(code.getCode())
                        .message(code.getMessage())
                        .dateTime(LocalDateTime.now())
                        .build());
    }

    @ExceptionHandler(value = AppCheckedException.class)
    ResponseEntity<Response> handlingAppCheckedException(AppCheckedException exception) {
        return ResponseEntity.badRequest().body(Response.builder()
                .statusCode(exception.getStatus().getCode())
                .message(exception.getMessage()).dateTime(LocalDateTime.now())
                .build());
    }

    @ExceptionHandler(value = NoResourceFoundException.class)
    ResponseEntity<Response> handlingNotFoundException(NoResourceFoundException exception) {
        return ResponseEntity.badRequest().body(Response.builder()
                .statusCode(StatusCode.ENPOINTMENT_NOT_FOUND.getCode())
                .message("Không tìm thấy enpoint: " + exception.getResourcePath())
                .dateTime(LocalDateTime.now())
                .build());
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    ResponseEntity<Response> handlingMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
        String enumKey = Objects.requireNonNull(exception.getFieldError()).getDefaultMessage();
        try {
            AccountValidErrorCode errorCode = AccountValidErrorCode.valueOf(enumKey);
            return ResponseEntity.badRequest().body(Response.builder()
                    .statusCode(errorCode.getCode())
                    .message(errorCode.getMessage())
                    .dateTime(LocalDateTime.now())
                    .build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Response.builder()
                    .statusCode(StatusCode.PARAMATER_NOT_FOUND.getCode())
                    .message(enumKey)
                    .dateTime(LocalDateTime.now())
                    .build());
        }
    }

    @ExceptionHandler(value = MissingServletRequestParameterException.class)
    ResponseEntity<Response> handlingMethodIllegalStateException(MissingServletRequestParameterException exception) {
        return ResponseEntity.badRequest().body(Response.builder()
                .statusCode(StatusCode.METHOD_NOT_INSTALLED.getCode())
                .message("Không tìm thấy tham số: " + exception.getParameterName())
                .dateTime(LocalDateTime.now())
                .build());
    }

    @ExceptionHandler(value = AppUnCheckedException.class)
    ResponseEntity<Response> handleAppException(AppUnCheckedException exception) {
        StatusCode code = exception.getStatus();
        if (code == null) throw new IllegalArgumentException("Đối tượng không được rỗng.");
        return buildResponse(code);
    }

    private ResponseEntity<Response> buildResponse(StatusCode errorCode) {
        return ResponseEntity.status(errorCode.getCode())
                .body(Response.builder()
                        .statusCode(errorCode.getCode())
                        .message(errorCode.getMessage())
                        .dateTime(LocalDateTime.now())
                        .build());
    }
}
