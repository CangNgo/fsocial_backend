package com.fsocial.repository;

import com.fsocial.entity.AuthProviderCredential;
import com.fsocial.enums.AuthProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthProviderRepository extends JpaRepository<AuthProviderCredential, String> {
    Optional<AuthProviderCredential> findByAccount_IdAndProvider(String accountId, AuthProvider provider);
    Optional<AuthProviderCredential> findByProviderAndProviderUserId(AuthProvider provider, String providerUserId);
}
