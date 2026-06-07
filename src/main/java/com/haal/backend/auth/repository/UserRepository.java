package com.haal.backend.auth.repository;

import com.haal.backend.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByPhoneHash(String phoneHash);

    boolean existsByAnonymousAlias(String anonymousAlias);

    boolean existsByPhoneHash(String phoneHash);

    Optional<User> findByAnonymousAlias(String alias);
}
