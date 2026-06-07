package com.haal.backend.auth.service;

import com.haal.backend.auth.entity.*;
import com.haal.backend.auth.dto.*;
import com.haal.backend.auth.repository.RefreshTokenRepository;
import com.haal.backend.auth.repository.UserRepository;
import com.haal.backend.auth.security.JwtService;
import com.haal.backend.shared.config.AppProperties;
import com.haal.backend.shared.config.PiiEncryptionService;
import com.haal.backend.shared.exception.ErrorCode;
import com.haal.backend.shared.exception.HaalException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService implements AuthServicePort {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder           passwordEncoder;
    private final PiiEncryptionService      piiEncryption;
    private final JwtService                jwtService;
    private final AppProperties             appProperties;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // 1. Validate uniqueness
        String phoneHash = passwordEncoder.encode(request.getPhone());

        if (userRepository.existsByPhoneHash(phoneHash)) {
            // Note: BCrypt is not deterministic — we need a deterministic check
            // This is handled by the login flow. Registration uses SHA-256 for
            // existence check and BCrypt for the stored hash.
        }

        // Deterministic SHA-256 for existence check
        String phoneSha = sha256(request.getPhone());
        if (userRepository.existsByPhoneHash(phoneSha)) {
            throw HaalException.conflict(ErrorCode.USER_ALREADY_EXISTS);
        }

        if (userRepository.existsByAnonymousAlias(request.getAnonymousAlias())) {
            throw HaalException.conflict(ErrorCode.ALIAS_TAKEN);
        }

        // 2. Build user with encrypted PII
        User user = User.builder()
                .anonymousAlias(request.getAnonymousAlias())
                .phoneHash(phoneSha)             // SHA-256 deterministic hash
                .phoneEncrypted(piiEncryption.encrypt(request.getPhone()))
                .ecNameEncrypted(piiEncryption.encrypt(
                        request.getEmergencyContactName()))
                .ecPhoneEncrypted(piiEncryption.encrypt(
                        request.getEmergencyContactPhone()))
                .build();

        user = userRepository.save(user);
        log.info("New user registered: alias={}", user.getAnonymousAlias());

        return buildAuthResponse(user);
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        String phoneSha = sha256(request.getPhone());

        User user = userRepository.findByPhoneHash(phoneSha)
                .orElseThrow(() ->
                        HaalException.unauthorized(ErrorCode.INVALID_CREDENTIALS));

        if (!user.isActive()) {
            throw HaalException.unauthorized(ErrorCode.ACCOUNT_DISABLED);
        }

        log.info("User logged in: alias={}", user.getAnonymousAlias());
        return buildAuthResponse(user);
    }

    @Override
    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request) {
        String tokenHash = sha256(request.getRefreshToken());

        RefreshToken stored = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() ->
                        HaalException.unauthorized(ErrorCode.TOKEN_INVALID));

        if (!stored.isValid()) {
            // Revoke all tokens for this user on suspicious activity
            refreshTokenRepository.revokeAllByUserId(stored.getUser().getId());
            throw HaalException.unauthorized(ErrorCode.TOKEN_EXPIRED);
        }

        // Rotate: revoke old, issue new
        stored.setRevoked(true);
        refreshTokenRepository.save(stored);

        return buildAuthResponse(stored.getUser());
    }

    @Override
    @Transactional
    public void logout(String refreshToken) {
        String tokenHash = sha256(refreshToken);
        refreshTokenRepository.findByTokenHash(tokenHash)
                .ifPresent(rt -> {
                    rt.setRevoked(true);
                    refreshTokenRepository.save(rt);
                });
    }

    // ── Private helpers ──────────────────────────────────────────────────────

    private AuthResponse buildAuthResponse(User user) {
        String accessToken = jwtService.generateAccessToken(
                user.getId(),
                user.getAnonymousAlias(),
                user.getRole().name());

        String rawRefreshToken = UUID.randomUUID().toString();
        persistRefreshToken(user, rawRefreshToken);

        long expiresIn = appProperties.getJwt().getExpirationMs() / 1000;

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(rawRefreshToken)
                .tokenType("Bearer")
                .expiresIn(expiresIn)
                .user(toProfileResponse(user))
                .build();
    }

    private void persistRefreshToken(User user, String rawToken) {
        Instant expiry = Instant.now().plusMillis(
                appProperties.getJwt().getRefreshExpirationMs());

        RefreshToken rt = RefreshToken.builder()
                .user(user)
                .tokenHash(sha256(rawToken))
                .expiresAt(expiry)
                .build();

        refreshTokenRepository.save(rt);
    }

    private UserProfileResponse toProfileResponse(User user) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .anonymousAlias(user.getAnonymousAlias())
                .createdAt(user.getCreatedAt())
                .active(user.isActive())
                .build();
    }

    /**
     * Deterministic SHA-256 — used for phone existence checks and
     * refresh token storage. Never use for password storage.
     */
    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Hashing failed", e);
        }
    }
}
