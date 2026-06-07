package com.haal.backend.auth.service;

import com.haal.backend.auth.dto.*;

public interface AuthServicePort {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    AuthResponse refresh(RefreshTokenRequest request);
    void logout(String refreshToken);
}
