package com.haal.backend.shared.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    USER_NOT_FOUND("AUTH_001", "User not found"),
    USER_ALREADY_EXISTS("AUTH_002", "User with this phone already exists"),
    ALIAS_TAKEN("AUTH_003", "Anonymous alias is already taken"),
    INVALID_CREDENTIALS("AUTH_004", "Invalid credentials"),
    TOKEN_EXPIRED("AUTH_005", "Token has expired"),
    TOKEN_INVALID("AUTH_006", "Token is invalid"),
    ACCOUNT_DISABLED("AUTH_007", "Account is disabled"),

    // Safety
    CHECKIN_TOO_SOON("SAFETY_001", "Check-in window not yet open"),
    SOS_ALREADY_ACTIVE("SAFETY_002", "An active SOS event already exists"),

    // General
    VALIDATION_FAILED("GEN_001", "Validation failed"),
    INTERNAL_ERROR("GEN_002", "An internal error occurred"),
    FORBIDDEN("GEN_003", "Access denied");

    private final String code;
    private final String message;
}
