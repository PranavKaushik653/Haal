package com.haal.backend.shared.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class HaalException extends RuntimeException{

    private final ErrorCode errorCode;
    private final HttpStatus status;
    public HaalException(ErrorCode errorCode, HttpStatus status) {
        this.errorCode = errorCode;
        this.status = status;
    }
    public static HaalException notFound(ErrorCode code) {
        return new HaalException(code, HttpStatus.NOT_FOUND);
    }

    public static HaalException conflict(ErrorCode code) {
        return new HaalException(code, HttpStatus.CONFLICT);
    }

    public static HaalException unauthorized(ErrorCode code) {
        return new HaalException(code, HttpStatus.UNAUTHORIZED);
    }

    public static HaalException forbidden() {
        return new HaalException(ErrorCode.FORBIDDEN, HttpStatus.FORBIDDEN);
    }

    public static HaalException badRequest(ErrorCode code) {
        return new HaalException(code, HttpStatus.BAD_REQUEST);
    }

}
