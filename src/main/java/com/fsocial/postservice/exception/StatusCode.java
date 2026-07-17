package com.fsocial.postservice.exception;

import com.fsocial.postservice.enums.CodeEnum;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum StatusCode implements CodeEnum {
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    OK(200, "OK", HttpStatus.OK),
    HTTPMETHOD_NOT_SUPPORTED(231, "HTTP method not supported", HttpStatus.METHOD_NOT_ALLOWED),
    REGISTER_FAILED(101, "Register failed", HttpStatus.BAD_REQUEST),
    CREATE_POST_SUCCESS(201, "Create post success", HttpStatus.CREATED),
    CREATE_POST_FAILED(216, "Create post failed", HttpStatus.BAD_REQUEST),
    UPDATE_POST_SUCCESS(217, "Update post success", HttpStatus.OK),
    DELETE_POST_SUCCESS(213, "Delete post success", HttpStatus.OK),
    FILE_NOT_FOUND(202, "File not found", HttpStatus.NOT_FOUND),
    UPLOAD_FILE_SUCCESS(203, "Upload file success", HttpStatus.OK),
    UPLOAD_FILE_FAILED(204, "Upload file failed", HttpStatus.BAD_REQUEST),
    CREATE_COMMENT_SUCCESS(205, "Create comment success", HttpStatus.CREATED),
    CREATE_COMMENT_FAILED(206, "Create comment failed", HttpStatus.BAD_REQUEST),
    USER_NOT_FOUND(208, "Client not found", HttpStatus.NOT_FOUND),
    POST_NOT_FOUND(209, "Post not found", HttpStatus.NOT_FOUND),
    ENPOINTMENT_NOT_FOUND(210, "Enpointment Not Found", HttpStatus.NOT_FOUND),
    PARAMATER_NOT_FOUND(220, "Paramater Not Found", HttpStatus.BAD_REQUEST),
    METHOD_NOT_INSTALLED(230, "Method Not installed", HttpStatus.BAD_REQUEST),
    INTERNAL_SERVER_ERROR(300, "Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR),
    UNSUPPORTED_MEDIA_TYPE(304, "Unsupported Media Type", HttpStatus.UNSUPPORTED_MEDIA_TYPE),
    FILE_TOO_LARGE(308, "File size exceeds the allowed limit", HttpStatus.PAYLOAD_TOO_LARGE),
    UNAUTHENTICATED(468, "Tài khoản chưa được xác thực.", HttpStatus.UNAUTHORIZED),
    UPLOAD_MEDIA_FAILED(218, "Upload media failed", HttpStatus.BAD_REQUEST),
    NOT_CONTENT(211, "Not Content", HttpStatus.BAD_REQUEST),
    COMMENT_NOT_FOUND(212, "Comment Not Found", HttpStatus.NOT_FOUND),
    TERM_OF_SERVICE_NOT_FOUND(305, "Term of service not found", HttpStatus.NOT_FOUND),
    REPLY_COMMENT_NOT_FOUND(306, "Reply Comment Not Found", HttpStatus.NOT_FOUND),
    COMPLAIN_NOT_FOUND(307, "Complain Not Found", HttpStatus.NOT_FOUND),
    SEND_MAIL_FAIL(500, "Send mail fail", HttpStatus.INTERNAL_SERVER_ERROR),
    UPLOAD_AVATAR_FAIL(312, "Upload avatar fail", HttpStatus.BAD_REQUEST),
    NOT_FOUND(404, "NOT FOUND", HttpStatus.NOT_FOUND),
    FEED_EMPTY(214, "Feed is empty", HttpStatus.OK),
    INTEREST_NOT_FOUND(215, "User interest data not found", HttpStatus.NOT_FOUND),
    IOEXCEPTION(311, "IOException", HttpStatus.INTERNAL_SERVER_ERROR),
    EMAIL_IS_NOT_VERIFY(309, "Email mail is not verify", HttpStatus.BAD_REQUEST),
    INVALID_GOOGLE_TOKEN(403, "Invalid or expired Google token", HttpStatus.FORBIDDEN),
    ROLE_NOT_FOUND(310, "Role not found", HttpStatus.NOT_FOUND),
    ;
    private final int code;
    private final String message;
    private final HttpStatusCode httpStatusCode;

    StatusCode(int code, String message, HttpStatusCode httpStatusCode) {
        this.code = code;
        this.message = message;
        this.httpStatusCode = httpStatusCode;
    }
}
