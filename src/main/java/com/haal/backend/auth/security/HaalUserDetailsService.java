package com.haal.backend.auth.security;


import com.haal.backend.auth.repository.UserRepository;
import com.haal.backend.shared.exception.ErrorCode;
import com.haal.backend.shared.exception.HaalException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HaalUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String userId)
            throws UsernameNotFoundException {
        return userRepository.findById(UUID.fromString(userId))
                .map(HaalUserDetails::new)
                .orElseThrow(() -> HaalException.notFound(ErrorCode.USER_NOT_FOUND));
    }
}